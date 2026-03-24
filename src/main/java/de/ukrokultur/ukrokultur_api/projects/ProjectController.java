package de.ukrokultur.ukrokultur_api.projects;

import de.ukrokultur.ukrokultur_api.common.dto.media.OrderUrlsRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.media.UrlRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectPageResultDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.web.MultipartJsonReader;
import de.ukrokultur.ukrokultur_api.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Projects", description = "Public projects and admin CRUD.")
@RestController
public class ProjectController {

    private final ProjectService service;
    private final MultipartJsonReader jsonReader;

    public ProjectController(ProjectService service, MultipartJsonReader jsonReader) {
        this.service = service;
        this.jsonReader = jsonReader;
    }

    @Operation(summary = "Get projects page")
    @GetMapping("/projects")
    public ProjectPageResultDto getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "true") boolean publishedOnly
    ) {
        return service.getPage(page, pageSize, publishedOnly);
    }
    @Operation(summary = "Get project by id (public)")
    @GetMapping("/projects/{id}")
    public ProjectItemDto getByIdPublic(@PathVariable UUID id) {
        return service.getByIdPublic(id);
    }
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Get project by id (admin)")
    @GetMapping("/admin/projects/{id}")
    public ProjectItemDto getByIdAdmin(@PathVariable UUID id) {
        return service.getByIdAdmin(id);
    }
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create project (JSON)")
    @PostMapping(value = "/admin/projects", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectItemDto create(@RequestBody @Valid ProjectUpsertRequestDto req) {
        return service.create(req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create project (multipart)")
    @PostMapping(value = "/admin/projects/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectItemDto createMultipart(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "galleryImages", required = false) List<MultipartFile> galleryImages
    ) {
        ProjectUpsertRequestDto data = jsonReader.read(dataJson, ProjectUpsertRequestDto.class);
        return service.createMultipart(data, coverImage, galleryImages);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update project (JSON)")
    @PutMapping(value = "/admin/projects/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectItemDto update(@PathVariable UUID id, @RequestBody @Valid ProjectUpsertRequestDto req) {
        return service.update(id, req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update project (multipart)")
    @PutMapping(value = "/admin/projects/{id}/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectItemDto updateMultipart(
            @PathVariable UUID id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "galleryImages", required = false) List<MultipartFile> galleryImages
    ) {
        ProjectUpsertRequestDto data = jsonReader.read(dataJson, ProjectUpsertRequestDto.class);
        return service.updateMultipart(id, data, coverImage, galleryImages);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Delete project")
    @DeleteMapping("/admin/projects/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Add images to project gallery (multipart)")
    @PostMapping(value = "/admin/projects/{id}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectItemDto addGalleryImages(
            @PathVariable UUID id,
            @RequestPart("galleryImages") List<MultipartFile> galleryImages
    ) {
        return service.addGalleryImages(id, galleryImages);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Delete one image from project gallery by url")
    @DeleteMapping(value = "/admin/projects/{id}/gallery", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectItemDto deleteOneGalleryImage(
            @PathVariable UUID id,
            @RequestBody @Valid UrlRequestDto req
    ) {
        return service.deleteOneGalleryImage(id, req.url());
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Reorder project gallery images")
    @PatchMapping(value = "/admin/projects/{id}/gallery/order", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectItemDto reorderGallery(
            @PathVariable UUID id,
            @RequestBody @Valid OrderUrlsRequestDto req
    ) {
        return service.reorderGallery(id, req.urls());
    }
}