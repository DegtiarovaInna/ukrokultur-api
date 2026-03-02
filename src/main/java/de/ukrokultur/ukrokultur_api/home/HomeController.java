package de.ukrokultur.ukrokultur_api.home;

import de.ukrokultur.ukrokultur_api.common.dto.home.HomeResponseDto;
import de.ukrokultur.ukrokultur_api.common.dto.home.HomeUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.web.MultipartJsonReader;
import de.ukrokultur.ukrokultur_api.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Home", description = "Public home page content and admin update.")
@RestController
public class HomeController {

    private final HomeService service;
    private final MultipartJsonReader jsonReader;

    public HomeController(HomeService service, MultipartJsonReader jsonReader) {
        this.service = service;
        this.jsonReader = jsonReader;
    }

    @Operation(summary = "Get Home page (public)")
    @GetMapping("/home")
    public HomeResponseDto getPublic() {
        return service.getPublic();
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Get Home page (admin)")
    @GetMapping("/admin/home")
    public HomeResponseDto getAdmin() {
        return service.getAdmin();
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update Home page (admin) - JSON")
    @PutMapping(value = "/admin/home", consumes = MediaType.APPLICATION_JSON_VALUE)
    public HomeResponseDto upsert(@RequestBody @Valid HomeUpsertRequestDto req) {
        return service.upsert(req);
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update Home page (admin) - multipart")
    @PutMapping(value = "/admin/home/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HomeResponseDto upsertMultipart(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "heroImage", required = false) MultipartFile heroImage,
            @RequestPart(value = "missionImage", required = false) MultipartFile missionImage
    ) {
        HomeUpsertRequestDto data = jsonReader.read(dataJson, HomeUpsertRequestDto.class);
        return service.upsertMultipart(data, heroImage, missionImage);
    }
}