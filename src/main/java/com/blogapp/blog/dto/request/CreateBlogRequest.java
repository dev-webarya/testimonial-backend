package com.blogapp.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a blog post")
public class CreateBlogRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "Blog title", example = "How to Prepare for IGCSE Physics")
    private String title;

    @Size(max = 500, message = "Excerpt must not exceed 500 characters")
    @Schema(description = "Short summary/excerpt of the blog", example = "A comprehensive guide to acing IGCSE Physics exams")
    private String excerpt;

    @NotBlank(message = "Content HTML is required")
    @Schema(description = "Blog content in HTML format", example = "<p>This is a guide...</p>")
    private String contentHtml;

    @Schema(description = "Blog content in JSON format (optional, for editors like TipTap)", example = "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\"}]}")
    private String contentJson;

    @Schema(description = "URL of the featured image", example = "https://example.com/images/physics.jpg")
    private String featuredImageUrl;

    @Schema(description = "Tags for the blog", example = "[\"physics\", \"igcse\", \"exam-prep\"]")
    private List<String> tags;

    @NotBlank(message = "Author Name is required")
    @Schema(description = "Name of the blog author", example = "Jane Doe")
    private String authorName;

    @NotBlank(message = "Author Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email of the blog author", example = "jane@example.com")
    private String authorEmail;

    @Schema(description = "Mobile number of the blog author", example = "1234567890")
    private String authorMobile;
}
