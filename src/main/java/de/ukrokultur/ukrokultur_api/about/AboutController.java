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
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.List;

@Tag(name = "About")
@RestController
@RequestMapping({"/about", "/admin/about"})
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
    @GetMapping
    public AboutResponseDto getPublic() {
        return aboutService.getPublic();
    }


    // Admin
    @Operation(summary = "Get about intro (admin)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/intro")
    public AboutIntroDto getIntroAdmin() {
        return aboutService.getIntroAdmin();
    }

    @Operation(summary = "Update about intro (admin)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/intro")
    public AboutIntroDto updateIntro(@Valid @RequestBody AboutIntroUpsertRequestDto req) {
        return aboutService.updateIntro(req);
    }

    @Operation(summary = "List members (admin)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/members")
    public List<AboutMemberDto> listMembersAdmin() {
        return aboutService.getMembersAdmin();
    }

    @Operation(summary = "Create member (admin)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/members")
    public AboutMemberDto createMember(@Valid @RequestBody AboutMemberUpsertRequestDto req) {
        return aboutService.createMember(req);
    }

    @Operation(summary = "Update member (admin)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/members/{id}")
    public AboutMemberDto updateMember(
            @PathVariable UUID id,
            @Valid @RequestBody AboutMemberUpsertRequestDto req
    ) {
        return aboutService.updateMember(id, req);
    }

    @Operation(summary = "Delete member (admin)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/members/{id}")
    public void deleteMember(@PathVariable UUID id) {
        aboutService.deleteMember(id);
    }
}
