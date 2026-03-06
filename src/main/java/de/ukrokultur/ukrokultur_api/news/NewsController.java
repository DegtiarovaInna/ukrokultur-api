package de.ukrokultur.ukrokultur_api.news;

import de.ukrokultur.ukrokultur_api.common.dto.media.OrderUrlsRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.media.UrlRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsPageResultDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.web.MultipartJsonReader;
import de.ukrokultur.ukrokultur_api.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "News", description = "Public news feed and admin CRUD.")
@RestController
@Validated
public class NewsController {

    private final NewsService newsService;
    private final MultipartJsonReader jsonReader;

    public NewsController(NewsService newsService, MultipartJsonReader jsonReader) {
        this.newsService = newsService;
        this.jsonReader = jsonReader;
    }

    @Operation(summary = "Get news page", description = "Returns news ordered by publication date (newest first).")
    @GetMapping("/news")
    public NewsPageResultDto getPage(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize,
            @RequestParam(defaultValue = "true") boolean publishedOnly
    ) {
        return newsService.getPage(page, pageSize, publishedOnly);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create news (JSON)")
    @PostMapping(value = "/admin/news", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NewsItemDto create(@RequestBody @Valid NewsUpsertRequestDto req) {
        return newsService.create(req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create news (multipart)")
    @PostMapping(value = "/admin/news/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NewsItemDto createMultipart(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        NewsUpsertRequestDto data = jsonReader.read(dataJson, NewsUpsertRequestDto.class);
        return newsService.createMultipart(data, images);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update news (JSON)")
    @PutMapping(value = "/admin/news/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NewsItemDto update(@PathVariable UUID id, @RequestBody @Valid NewsUpsertRequestDto req) {
        return newsService.update(id, req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update news (multipart)")
    @PutMapping(value = "/admin/news/{id}/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NewsItemDto updateMultipart(
            @PathVariable UUID id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        NewsUpsertRequestDto data = jsonReader.read(dataJson, NewsUpsertRequestDto.class);
        return newsService.updateMultipart(id, data, images);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Delete news")
    @DeleteMapping("/admin/news/{id}")
    public void delete(@PathVariable UUID id) {
        newsService.delete(id);
    }
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Add images to news gallery (multipart)")
    @PostMapping(value = "/admin/news/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NewsItemDto addImages(
            @PathVariable UUID id,
            @RequestPart("images") List<MultipartFile> images
    ) {
        return newsService.addImages(id, images);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Delete one image from news gallery by url")
    @DeleteMapping(value = "/admin/news/{id}/images", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NewsItemDto deleteOneImage(
            @PathVariable UUID id,
            @RequestBody @Valid UrlRequestDto req
    ) {
        return newsService.deleteOneImage(id, req.url());
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Reorder news gallery images (first becomes cover)")
    @PatchMapping(value = "/admin/news/{id}/images/order", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NewsItemDto reorderImages(
            @PathVariable UUID id,
            @RequestBody @Valid OrderUrlsRequestDto req
    ) {
        return newsService.reorderImages(id, req.urls());
    }
}
