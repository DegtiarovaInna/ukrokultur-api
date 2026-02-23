package de.ukrokultur.ukrokultur_api.news;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsPageResultDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsUpsertRequestDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.UUID;

@Tag(name = "News", description = "Public news feed and admin CRUD.")
@RestController
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @Operation(summary = "Get news page", description = "Returns news ordered by publication date (newest first).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/news")
    public NewsPageResultDto getPage(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize,
            @RequestParam(defaultValue = "true") boolean publishedOnly
    ) {
        return newsService.getPage(page, pageSize, publishedOnly);
    }

    @SecurityRequirement(name = de.ukrokultur.ukrokultur_api.config.OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create news (JSON)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class)))
    })
    @PostMapping(value = "/admin/news", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NewsItemDto create(@RequestBody @Valid NewsUpsertRequestDto req) {
        return newsService.create(req);
    }

    @SecurityRequirement(name = de.ukrokultur.ukrokultur_api.config.OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create news (multipart)")
    @PostMapping(value = "/admin/news", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NewsItemDto createMultipart(
            @RequestPart("data") @Valid NewsUpsertRequestDto data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return newsService.createMultipart(data, images);
    }

    @SecurityRequirement(name = de.ukrokultur.ukrokultur_api.config.OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update news (JSON)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class)))
    })
    @PutMapping(value = "/admin/news/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NewsItemDto update(@PathVariable UUID id, @RequestBody @Valid NewsUpsertRequestDto req) {
        return newsService.update(id, req);
    }

    @SecurityRequirement(name = de.ukrokultur.ukrokultur_api.config.OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update news (multipart)")
    @PutMapping(value = "/admin/news/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NewsItemDto updateMultipart(
            @PathVariable UUID id,
            @RequestPart("data") @Valid NewsUpsertRequestDto data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return newsService.updateMultipart(id, data, images);
    }

    @SecurityRequirement(name = de.ukrokultur.ukrokultur_api.config.OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Delete news", description = "Deletes a news item by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class)))
    })
    @DeleteMapping("/admin/news/{id}")
    public void delete(@PathVariable UUID id) {
        newsService.delete(id);
    }
}