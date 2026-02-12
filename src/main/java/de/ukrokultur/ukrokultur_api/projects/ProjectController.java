package de.ukrokultur.ukrokultur_api.projects;

import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Projects", description = "Public projects and admin CRUD.")
@RestController
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @Operation(summary = "Get projects", description = "Returns projects ordered by creation date (newest first).")
    @GetMapping("/projects")
    public List<ProjectItemDto> getAll(@RequestParam(defaultValue = "true") boolean publishedOnly) {
        return service.getAll(publishedOnly);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create project")
    @PostMapping("/admin/projects")
    public ProjectItemDto create(@RequestBody @Valid ProjectUpsertRequestDto req) {
        return service.create(req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update project")
    @PutMapping("/admin/projects/{id}")
    public ProjectItemDto update(@PathVariable String id, @RequestBody @Valid ProjectUpsertRequestDto req) {
        return service.update(id, req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Delete project")
    @DeleteMapping("/admin/projects/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
