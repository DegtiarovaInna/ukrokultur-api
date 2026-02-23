package de.ukrokultur.ukrokultur_api.about;

import de.ukrokultur.ukrokultur_api.common.dto.about.*;
import de.ukrokultur.ukrokultur_api.common.error.ApiError;
import de.ukrokultur.ukrokultur_api.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "About", description = "Public about page and admin CRUD for intro/members.")
@RestController
public class AboutController {

    private final AboutService aboutService;

    public AboutController(AboutService aboutService) {
        this.aboutService = aboutService;
    }

    // Public
    @Operation(summary = "Get about (public)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/about")
    public AboutResponseDto getPublic() {
        return aboutService.getPublic();
    }

    // Admin
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Get about intro (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/admin/about/intro")
    public AboutIntroDto getIntroAdmin() {
        return aboutService.getIntroAdmin();
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update about intro (admin) - JSON")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping(value = "/admin/about/intro", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AboutIntroDto updateIntro(@Valid @RequestBody AboutIntroUpsertRequestDto req) {
        return aboutService.updateIntro(req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update about intro (admin) - multipart")
    @PutMapping(value = "/admin/about/intro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AboutIntroDto updateIntroMultipart(
            @RequestPart("data") @Valid AboutIntroUpsertRequestDto data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return aboutService.updateIntroMultipart(data, image);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "List members (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/admin/about/members")
    public List<AboutMemberDto> listMembersAdmin() {
        return aboutService.getMembersAdmin();
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create member (admin) - JSON")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(value = "/admin/about/members", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AboutMemberDto createMember(@Valid @RequestBody AboutMemberUpsertRequestDto req) {
        return aboutService.createMember(req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create member (admin) - multipart")
    @PostMapping(value = "/admin/about/members", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AboutMemberDto createMemberMultipart(
            @RequestPart("data") @Valid AboutMemberUpsertRequestDto data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return aboutService.createMemberMultipart(data, image);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update member (admin) - JSON")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping(value = "/admin/about/members/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AboutMemberDto updateMember(@PathVariable UUID id, @Valid @RequestBody AboutMemberUpsertRequestDto req) {
        return aboutService.updateMember(id, req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update member (admin) - multipart")
    @PutMapping(value = "/admin/about/members/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AboutMemberDto updateMemberMultipart(
            @PathVariable UUID id,
            @RequestPart("data") @Valid AboutMemberUpsertRequestDto data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return aboutService.updateMemberMultipart(id, data, image);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Delete member (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/admin/about/members/{id}")
    public void deleteMember(@PathVariable UUID id) {
        aboutService.deleteMember(id);
    }
}