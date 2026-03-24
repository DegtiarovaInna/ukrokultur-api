package de.ukrokultur.ukrokultur_api.about;

import de.ukrokultur.ukrokultur_api.common.dto.about.*;
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

@Tag(name = "About", description = "Public about page and admin CRUD for intro/members.")
@RestController
public class AboutController {

    private final AboutService aboutService;
    private final MultipartJsonReader jsonReader;

    public AboutController(AboutService aboutService, MultipartJsonReader jsonReader) {
        this.aboutService = aboutService;
        this.jsonReader = jsonReader;
    }

    @Operation(summary = "Get about (public)")
    @GetMapping("/about")
    public AboutResponseDto getPublic() {
        return aboutService.getPublic();
    }
    @Operation(summary = "Get about member by id (public)")
    @GetMapping("/about/members/{id}")
    public AboutMemberDto getMemberPublic(@PathVariable UUID id) {
        return aboutService.getMemberPublic(id);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Get about intro (admin)")
    @GetMapping("/admin/about/intro")
    public AboutIntroDto getIntroAdmin() {
        return aboutService.getIntroAdmin();
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update about intro (admin) - JSON")
    @PutMapping(value = "/admin/about/intro", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AboutIntroDto updateIntro(@Valid @RequestBody AboutIntroUpsertRequestDto req) {
        return aboutService.updateIntro(req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update about intro (admin) - multipart")
    @PutMapping(value = "/admin/about/intro/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AboutIntroDto updateIntroMultipart(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        AboutIntroUpsertRequestDto data = jsonReader.read(dataJson, AboutIntroUpsertRequestDto.class);
        return aboutService.updateIntroMultipart(data, image);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "List members (admin)")
    @GetMapping("/admin/about/members")
    public List<AboutMemberDto> listMembersAdmin() {
        return aboutService.getMembersAdmin();
    }
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)

    @Operation(summary = "Get member by id (admin)")
    @GetMapping("/admin/about/members/{id}")
    public AboutMemberDto getMemberAdmin(@PathVariable UUID id) {
        return aboutService.getMemberAdmin(id);
    }
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create member (admin) - JSON")
    @PostMapping(value = "/admin/about/members", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AboutMemberDto createMember(@Valid @RequestBody AboutMemberUpsertRequestDto req) {
        return aboutService.createMember(req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Create member (admin) - multipart")
    @PostMapping(value = "/admin/about/members/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AboutMemberDto createMemberMultipart(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        AboutMemberUpsertRequestDto data = jsonReader.read(dataJson, AboutMemberUpsertRequestDto.class);
        return aboutService.createMemberMultipart(data, image);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update member (admin) - JSON")
    @PutMapping(value = "/admin/about/members/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AboutMemberDto updateMember(@PathVariable UUID id, @Valid @RequestBody AboutMemberUpsertRequestDto req) {
        return aboutService.updateMember(id, req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update member (admin) - multipart")
    @PutMapping(value = "/admin/about/members/{id}/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AboutMemberDto updateMemberMultipart(
            @PathVariable UUID id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        AboutMemberUpsertRequestDto data = jsonReader.read(dataJson, AboutMemberUpsertRequestDto.class);
        return aboutService.updateMemberMultipart(id, data, image);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Delete member (admin)")
    @DeleteMapping("/admin/about/members/{id}")
    public void deleteMember(@PathVariable UUID id) {
        aboutService.deleteMember(id);
    }
}