package de.ukrokultur.ukrokultur_api.projects;

import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectPageResultDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectUpsertRequestDto;
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

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @Operation(summary = "Get projects page", description = "Returns projects with pagination.")
    @GetMapping("/projects")
    public ProjectPageResultDto getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "true") boolean publishedOnly
    ) {
        return service.getPage(page, pageSize, publishedOnly);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create project (JSON)")
    @PostMapping(value = "/admin/projects", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectItemDto create(@RequestBody @Valid ProjectUpsertRequestDto req) {
        return service.create(req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create project (multipart)", description = "One request: data + coverImage + galleryImages")
    @PostMapping(value = "/admin/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectItemDto createMultipart(
            @RequestPart("data") @Valid ProjectUpsertRequestDto data,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "galleryImages", required = false) List<MultipartFile> galleryImages
    ) {
        return service.createMultipart(data, coverImage, galleryImages);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update project (JSON)")
    @PutMapping(value = "/admin/projects/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectItemDto update(@PathVariable UUID id, @RequestBody @Valid ProjectUpsertRequestDto req) {
        return service.update(id, req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update project (multipart)", description = "One request: data + coverImage + galleryImages")
    @PutMapping(value = "/admin/projects/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectItemDto updateMultipart(
            @PathVariable UUID id,
            @RequestPart("data") @Valid ProjectUpsertRequestDto data,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "galleryImages", required = false) List<MultipartFile> galleryImages
    ) {
        return service.updateMultipart(id, data, coverImage, galleryImages);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Delete project")
    @DeleteMapping("/admin/projects/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}