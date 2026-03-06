package de.ukrokultur.ukrokultur_api.web.news;

import tools.jackson.databind.ObjectMapper;
import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.web.MultipartJsonReader;
import de.ukrokultur.ukrokultur_api.contact.ContactRateLimitFilter;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsPageResultDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.news.NewsController;
import de.ukrokultur.ukrokultur_api.news.NewsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
@AutoConfigureMockMvc(addFilters = false)
class NewsControllerWebTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean NewsService newsService;
    @MockitoBean MultipartJsonReader jsonReader;

    @MockitoBean ContactRateLimitFilter contactRateLimitFilter;

    @Test
    void getPage_returns200() throws Exception {
        when(newsService.getPage(1, 10, true)).thenReturn(
                new NewsPageResultDto(List.of(), 1, 10, 0, 0)
        );

        mvc.perform(get("/news")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("publishedOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void create_json_callsService() throws Exception {
        var req = new NewsUpsertRequestDto(
                "n1",
                LocalDate.of(2025, 6, 21),
                LocalDate.of(2025, 6, 21),
                new I18nText("en", "de", "uk"),
                new I18nText("en", "de", "uk"),
                List.of(),
                List.of(),
                true
        );

        when(newsService.create(any())).thenReturn(
                new NewsItemDto(
                        "id",
                        "n1",
                        req.newsDate(),
                        req.eventDate(),
                        req.title(),
                        req.content(),
                        List.of(),
                        List.of(),
                        true,
                        null,
                        null
                )
        );

        mvc.perform(post("/admin/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("n1"));
    }

    @Test
    void createMultipart_readsJsonAndCallsService() throws Exception {
        var dto = new NewsUpsertRequestDto(
                "n1",
                LocalDate.of(2025, 6, 21),
                LocalDate.of(2025, 6, 21),
                new I18nText("en", "de", "uk"),
                new I18nText("en", "de", "uk"),
                List.of(),
                List.of(),
                true
        );

        when(jsonReader.read(anyString(), eq(NewsUpsertRequestDto.class))).thenReturn(dto);
        when(newsService.createMultipart(eq(dto), any())).thenReturn(
                new NewsItemDto(
                        "id",
                        "n1",
                        dto.newsDate(),
                        dto.eventDate(),
                        dto.title(),
                        dto.content(),
                        List.of(),
                        List.of(),
                        true,
                        null,
                        null
                )
        );

        var dataPart = new org.springframework.mock.web.MockMultipartFile(
                "data", "", "application/json", om.writeValueAsBytes(dto)
        );

        mvc.perform(multipart("/admin/news/multipart")
                        .file(dataPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("n1"));
    }

    @Test
    void updateMultipart_readsJsonAndCallsService() throws Exception {
        UUID id = UUID.randomUUID();

        var dto = new NewsUpsertRequestDto(
                null,
                LocalDate.of(2025, 6, 21),
                null,
                new I18nText("en", "de", "uk"),
                new I18nText("en", "de", "uk"),
                null,
                List.of(),
                true
        );

        when(jsonReader.read(anyString(), eq(NewsUpsertRequestDto.class))).thenReturn(dto);
        when(newsService.updateMultipart(eq(id), eq(dto), any())).thenReturn(
                new NewsItemDto(
                        id.toString(),
                        "n1",
                        dto.newsDate(),
                        dto.eventDate(),
                        dto.title(),
                        dto.content(),
                        List.of(),
                        List.of(),
                        true,
                        null,
                        null
                )
        );

        var dataPart = new org.springframework.mock.web.MockMultipartFile(
                "data", "", "application/json", om.writeValueAsBytes(dto)
        );

        mvc.perform(multipart("/admin/news/{id}/multipart", id)
                        .file(dataPart)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void deleteOneImage_callsService() throws Exception {
        UUID id = UUID.randomUUID();

        when(newsService.deleteOneImage(eq(id), eq("u1")))
                .thenReturn(new NewsItemDto(
                        id.toString(),
                        "n1",
                        LocalDate.of(2025, 6, 21),
                        null,
                        new I18nText("en", "de", "uk"),
                        new I18nText("en", "de", "uk"),
                        List.of("u2"),
                        List.of(),
                        true,
                        null,
                        null
                ));

        mvc.perform(delete("/admin/news/{id}/images", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new de.ukrokultur.ukrokultur_api.common.dto.media.UrlRequestDto("u1")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images[0]").value("u2"));
    }

    @Test
    void reorderImages_callsService() throws Exception {
        UUID id = UUID.randomUUID();

        when(newsService.reorderImages(eq(id), eq(List.of("u2", "u1"))))
                .thenReturn(new NewsItemDto(
                        id.toString(),
                        "n1",
                        LocalDate.of(2025, 6, 21),
                        null,
                        new I18nText("en", "de", "uk"),
                        new I18nText("en", "de", "uk"),
                        List.of("u2", "u1"),
                        List.of(),
                        true,
                        null,
                        null
                ));

        mvc.perform(patch("/admin/news/{id}/images/order", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new de.ukrokultur.ukrokultur_api.common.dto.media.OrderUrlsRequestDto(List.of("u2", "u1"))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images[0]").value("u2"));
    }
}