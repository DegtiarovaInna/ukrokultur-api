package de.ukrokultur.ukrokultur_api.web.home;

import tools.jackson.databind.ObjectMapper;
import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.home.HomeResponseDto;
import de.ukrokultur.ukrokultur_api.common.dto.home.HomeUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.web.MultipartJsonReader;
import de.ukrokultur.ukrokultur_api.contact.ContactRateLimitProperties;
import de.ukrokultur.ukrokultur_api.home.HomeController;
import de.ukrokultur.ukrokultur_api.home.HomeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerWebTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean HomeService homeService;
    @MockitoBean MultipartJsonReader jsonReader;

    @MockitoBean ContactRateLimitProperties contactRateLimitProperties;

    @Test
    void getPublic_returns200() throws Exception {
        when(homeService.getPublic()).thenReturn(stubResponse());

        mvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hero").exists());
    }

    @Test
    void upsert_json_callsService() throws Exception {
        var req = stubUpsertRequest();
        when(homeService.upsert(any())).thenReturn(stubResponse());

        mvc.perform(put("/admin/home")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mission").exists());
    }

    private static HomeUpsertRequestDto stubUpsertRequest() {
        return new HomeUpsertRequestDto(
                new HomeUpsertRequestDto.HomeHeroUpsertDto(
                        "hero",
                        new I18nText("h1","h2","h3"),
                        new I18nText("s1","s2","s3"),
                        true
                ),
                new HomeUpsertRequestDto.HomeMissionUpsertDto(
                        "mission",
                        new I18nText("m1","m2","m3"),
                        new I18nText("t1","t2","t3"),
                        true
                ),
                new HomeUpsertRequestDto.HomeWorkFieldsUpsertDto(
                        true,
                        List.of(new HomeUpsertRequestDto.HomeWorkFieldItemUpsertDto(
                                null, "wf-1", true,
                                new I18nText("a","b","c"),
                                new I18nText("d","e","f")
                        ))
                )
        );
    }

    private static HomeResponseDto stubResponse() {
        return new HomeResponseDto(
                new HomeResponseDto.HomeHeroDto(
                        "hero",
                        new I18nText("h1","h2","h3"),
                        new I18nText("s1","s2","s3"),
                        true
                ),
                new HomeResponseDto.HomeMissionDto(
                        "mission",
                        new I18nText("m1","m2","m3"),
                        new I18nText("t1","t2","t3"),
                        true
                ),
                new HomeResponseDto.HomeWorkFieldsDto(true, List.of()),
                null
        );
    }
}