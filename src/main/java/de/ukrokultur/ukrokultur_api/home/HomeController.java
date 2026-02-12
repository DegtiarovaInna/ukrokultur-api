package de.ukrokultur.ukrokultur_api.home;

import de.ukrokultur.ukrokultur_api.common.dto.home.HomeResponseDto;
import de.ukrokultur.ukrokultur_api.common.dto.home.HomeUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Home", description = "Public home page content and admin update.")
@RestController
public class HomeController {

    private final HomeService service;

    public HomeController(HomeService service) {
        this.service = service;
    }

    @Operation(summary = "Get Home page (public)", description = "Returns Home page content (hero, mission, workFields) + updatedAt.")
    @GetMapping("/home")
    public HomeResponseDto getPublic() {
        return service.getPublic();
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Get Home page (admin)", description = "Returns Home page content including unpublished items.")
    @GetMapping("/admin/home")
    public HomeResponseDto getAdmin() {
        return service.getAdmin();
    }

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Update Home page (admin)", description = "Updates hero + mission + workFields (including items) in one request.")
    @PutMapping("/admin/home")
    public HomeResponseDto upsert(@RequestBody @Valid HomeUpsertRequestDto req) {
        return service.upsert(req);
    }
}
