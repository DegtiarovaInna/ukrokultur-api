package de.ukrokultur.ukrokultur_api.web.about;

import tools.jackson.databind.ObjectMapper;
import de.ukrokultur.ukrokultur_api.about.AboutController;
import de.ukrokultur.ukrokultur_api.about.AboutService;
import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.about.AboutIntroDto;
import de.ukrokultur.ukrokultur_api.common.dto.about.AboutIntroUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.about.AboutResponseDto;
import de.ukrokultur.ukrokultur_api.common.web.MultipartJsonReader;
import de.ukrokultur.ukrokultur_api.contact.ContactRateLimitFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AboutController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ContactRateLimitFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AboutControllerWebTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean AboutService aboutService;
    @MockitoBean MultipartJsonReader jsonReader;

    @Test
    void getPublic_returns200() throws Exception {
        when(aboutService.getPublic()).thenReturn(
                new AboutResponseDto(
                        new AboutIntroDto(
                                "img",
                                new I18nText("en", "de", "uk"),
                                new I18nText("en", "de", "uk"),
                                true
                        ),
                        List.of(),
                        null
                )
        );

        mvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intro").exists());

        verify(aboutService).getPublic();
    }

    @Test
    void updateIntro_json_callsService() throws Exception {
        var req = new AboutIntroUpsertRequestDto(
                "img",
                new I18nText("t1", "t2", "t3"),
                new I18nText("x1", "x2", "x3"),
                true
        );

        when(aboutService.updateIntro(any())).thenReturn(
                new AboutIntroDto("img", req.title(), req.text(), true)
        );

        mvc.perform(put("/admin/about/intro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("img"));

        verify(aboutService).updateIntro(any());
    }
}