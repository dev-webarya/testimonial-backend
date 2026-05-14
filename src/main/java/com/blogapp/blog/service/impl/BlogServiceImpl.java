package com.blogapp.blog.service.impl;

import com.blogapp.blog.dto.request.CreateBlogRequest;
import com.blogapp.blog.dto.response.ArchiveResponse;
import com.blogapp.blog.dto.response.BlogDetailResponse;
import com.blogapp.blog.dto.response.BlogSummaryResponse;
import com.blogapp.blog.entity.BlogPost;
import com.blogapp.blog.enums.BlogStatus;
import com.blogapp.blog.mapper.BlogMapper;
import com.blogapp.blog.repository.BlogPostRepository;
import com.blogapp.blog.service.BlogService;
import com.blogapp.blog.service.BlogSubscriptionService;
import com.blogapp.common.dto.PageResponse;
import com.blogapp.common.exception.BadRequestException;
import com.blogapp.common.exception.ResourceNotFoundException;
import com.blogapp.common.util.HtmlSanitizer;
import com.blogapp.common.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogPostRepository blogPostRepository;
    private final BlogMapper blogMapper;
    private final MongoTemplate mongoTemplate;
    private final com.blogapp.otp.service.OtpService otpService;
    private final BlogSubscriptionService blogSubscriptionService;

    @Override
    public PageResponse<BlogSummaryResponse> getPublishedBlogs(String search, Integer year, Integer month,
            String sort, int page, int size) {
        Sort sortOrder = resolveSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<BlogPost> blogPage;

        if (search != null && !search.isBlank()) {
            if (year != null && month != null) {
                blogPage = blogPostRepository.searchByTextAndYearMonth(BlogStatus.PUBLISHED, year, month, search,
                        pageable);
            } else {
                blogPage = blogPostRepository.searchByText(BlogStatus.PUBLISHED, search, pageable);
            }
        } else if (year != null && month != null) {
            blogPage = blogPostRepository.findByStatusAndYearAndMonth(BlogStatus.PUBLISHED, year, month, pageable);
        } else if (year != null) {
            blogPage = blogPostRepository.findByStatusAndYear(BlogStatus.PUBLISHED, year, pageable);
        } else {
            blogPage = blogPostRepository.findByStatus(BlogStatus.PUBLISHED, pageable);
        }

        List<BlogSummaryResponse> content = blogPage.getContent().stream()
                .map(blogMapper::toSummaryResponse)
                .collect(Collectors.toList());

        return PageResponse.<BlogSummaryResponse>builder()
                .content(content)
                .page(blogPage.getNumber())
                .size(blogPage.getSize())
                .totalElements(blogPage.getTotalElements())
                .totalPages(blogPage.getTotalPages())
                .first(blogPage.isFirst())
                .last(blogPage.isLast())
                .build();
    }

    @Override
    public BlogDetailResponse getBlogBySlug(String slug) {
        BlogPost blog = blogPostRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "slug", slug));

        if (blog.getStatus() != BlogStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Blog", "slug", slug);
        }

        return blogMapper.toDetailResponse(blog);
    }

    @Override
    public BlogDetailResponse getBlogById(String id) {
        BlogPost blog = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));
        return blogMapper.toDetailResponse(blog);
    }

    @Override
    public List<ArchiveResponse> getArchive() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is(BlogStatus.PUBLISHED.name())),
                Aggregation.group("year", "month").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "_id.year", "_id.month"));

        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(
                aggregation, "blog_posts", org.bson.Document.class);

        Map<Integer, List<ArchiveResponse.MonthCount>> yearMap = new TreeMap<>(Comparator.reverseOrder());

        for (org.bson.Document doc : results.getMappedResults()) {
            org.bson.Document idDoc = doc.get("_id", org.bson.Document.class);
            if (idDoc == null)
                continue;

            Integer year = idDoc.getInteger("year");
            Integer month = idDoc.getInteger("month");
            long count = doc.getInteger("count", 0);

            if (year != null && month != null) {
                yearMap.computeIfAbsent(year, k -> new ArrayList<>())
                        .add(ArchiveResponse.MonthCount.builder()
                                .month(month)
                                .count(count)
                                .build());
            }
        }

        return yearMap.entrySet().stream()
                .map(entry -> ArchiveResponse.builder()
                        .year(entry.getKey())
                        .months(entry.getValue().stream()
                                .sorted(Comparator.comparingInt(ArchiveResponse.MonthCount::getMonth).reversed())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public BlogPost createBlog(CreateBlogRequest request, String authorName, String authorEmail, String authorMobile) {
        BlogPost blog = blogMapper.toEntity(request);
        blog.setAuthorName(authorName);
        blog.setAuthorEmail(authorEmail);
        blog.setAuthorMobile(authorMobile);
        blog.setStatus(BlogStatus.PENDING);
        blog.setSubmittedAt(LocalDateTime.now());

        // Ensure unique slug
        String baseSlug = SlugUtil.generateSlug(request.getTitle());
        String slug = baseSlug;
        int counter = 1;
        while (blogPostRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        blog.setSlug(slug);

        log.info("Creating blog post with slug: {} by author: {}", slug, authorEmail);
        return blogPostRepository.save(blog);
    }

    @Override
    public BlogPost approveBlog(String id, String adminId) {
        BlogPost blog = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));

        if (blog.getStatus() != BlogStatus.PENDING) {
            throw new BadRequestException("Only PENDING blogs can be approved. Current status: " + blog.getStatus());
        }

        blog.setStatus(BlogStatus.PUBLISHED);
        blog.setPublishedAt(LocalDateTime.now());
        blog.setApprovedByAdminId(adminId);
        blog.setYear(blog.getPublishedAt().getYear());
        blog.setMonth(blog.getPublishedAt().getMonthValue());
        blog.setRejectionReason(null);

        log.info("Blog approved: {} by admin: {}", id, adminId);
        BlogPost savedBlog = blogPostRepository.save(blog);
        
        // Asynchronously notify all active subscribers
        blogSubscriptionService.notifySubscribersAsync(savedBlog);

        return savedBlog;
    }

    @Override
    public BlogPost rejectBlog(String id, String reason) {
        BlogPost blog = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));

        if (blog.getStatus() != BlogStatus.PENDING) {
            throw new BadRequestException("Only PENDING blogs can be rejected. Current status: " + blog.getStatus());
        }

        blog.setStatus(BlogStatus.REJECTED);
        blog.setRejectionReason(reason);

        log.info("Blog rejected: {} — reason: {}", id, reason);
        return blogPostRepository.save(blog);
    }

    @Override
    public BlogPost updateBlog(String id, CreateBlogRequest request) {
        BlogPost blog = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));

        blog.setTitle(request.getTitle());
        blog.setExcerpt(request.getExcerpt());
        blog.setContentHtml(HtmlSanitizer.sanitize(request.getContentHtml()));
        blog.setContentJson(request.getContentJson());
        blog.setFeaturedImageUrl(request.getFeaturedImageUrl());
        blog.setTags(request.getTags());

        log.info("Blog updated: {}", id);
        return blogPostRepository.save(blog);
    }

    @Override
    public PageResponse<BlogDetailResponse> getAdminBlogs(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<BlogPost> blogPage;

        if (status != null && !status.isBlank()) {
            try {
                BlogStatus blogStatus = BlogStatus.valueOf(status.toUpperCase());
                blogPage = blogPostRepository.findByStatus(blogStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status
                        + ". Valid values: DRAFT, PENDING, PUBLISHED, REJECTED");
            }
        } else {
            blogPage = blogPostRepository.findAll(pageable);
        }

        List<BlogDetailResponse> content = blogPage.getContent().stream()
                .map(blogMapper::toDetailResponse)
                .collect(Collectors.toList());

        return PageResponse.<BlogDetailResponse>builder()
                .content(content)
                .page(blogPage.getNumber())
                .size(blogPage.getSize())
                .totalElements(blogPage.getTotalElements())
                .totalPages(blogPage.getTotalPages())
                .first(blogPage.isFirst())
                .last(blogPage.isLast())
                .build();
    }

    @Override
    public void incrementViewCount(String id) {
        BlogPost blog = blogPostRepository.findById(id).orElse(null);
        if (blog != null) {
            blog.setViewsCount(blog.getViewsCount() + 1);
            blogPostRepository.save(blog);
        }
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank() || sort.equalsIgnoreCase("recent")) {
            return Sort.by(Sort.Direction.DESC, "publishedAt");
        } else if (sort.equalsIgnoreCase("popular")) {
            return Sort.by(Sort.Direction.DESC, "likesCount");
        } else if (sort.equalsIgnoreCase("oldest")) {
            return Sort.by(Sort.Direction.ASC, "publishedAt");
        } else if (sort.equalsIgnoreCase("most_commented")) {
            return Sort.by(Sort.Direction.DESC, "commentsCount");
        }
        return Sort.by(Sort.Direction.DESC, "publishedAt");
    }

    @Override
    public void deleteBlog(String id) {
        BlogPost blog = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog ", "id: ", id));
        // Note: Decoupled from comment logic as comment module is excluded in this migration
        blogPostRepository.delete(blog);
    }

    @Override
    public void startSubmission(String email, boolean isResend) {
        otpService.sendOtp(email, com.blogapp.otp.enums.OtpPurpose.BLOG_SUBMISSION, isResend);
    }

    @Override
    public boolean verifySubmission(String email, String otp) {
        return otpService.verifyOtp(email, otp, com.blogapp.otp.enums.OtpPurpose.BLOG_SUBMISSION);
    }

    @Override
    public BlogPost finishSubmission(CreateBlogRequest request) {
        if (!otpService.isEmailVerified(request.getAuthorEmail(), com.blogapp.otp.enums.OtpPurpose.BLOG_SUBMISSION)) {
            throw new BadRequestException("Email not verified for blog submission");
        }
        return createBlog(request, request.getAuthorName(), request.getAuthorEmail(), request.getAuthorMobile());
    }
}
