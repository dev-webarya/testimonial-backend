package com.blogapp.blog.service;

import com.blogapp.blog.dto.request.CreateBlogRequest;
import com.blogapp.blog.dto.response.ArchiveResponse;
import com.blogapp.blog.dto.response.BlogDetailResponse;
import com.blogapp.blog.dto.response.BlogSummaryResponse;
import com.blogapp.blog.entity.BlogPost;
import com.blogapp.common.dto.PageResponse;

import java.util.List;

public interface BlogService {

    PageResponse<BlogSummaryResponse> getPublishedBlogs(String search, Integer year, Integer month,
            String sort, int page, int size);

    BlogDetailResponse getBlogBySlug(String slug);

    BlogDetailResponse getBlogById(String id);

    List<ArchiveResponse> getArchive();

    BlogPost createBlog(CreateBlogRequest request, String authorName, String authorEmail, String authorMobile);

    BlogPost approveBlog(String id, String adminId);

    BlogPost rejectBlog(String id, String reason);

    BlogPost updateBlog(String id, CreateBlogRequest request);

    PageResponse<BlogDetailResponse> getAdminBlogs(String status, int page, int size);

    void incrementViewCount(String id);

    void deleteBlog(String id);

    // Submission Flow
    boolean startSubmission(String email, boolean isResend);

    boolean verifySubmission(String email, String otp);

    BlogPost finishSubmission(CreateBlogRequest request);

    List<BlogSummaryResponse> getMyBlogs(String email);
}
