package de.ukrokultur.ukrokultur_api.web.projects;

import tools.jackson.databind.ObjectMapper;
import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.media.OrderUrlsRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.media.UrlRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.web.MultipartJsonReader;
import de.ukrokultur.ukrokultur_api.contact.ContactRateLimitFilter;
import de.ukrokultur.ukrokultur_api.projects.ProjectController;
import de.ukrokultur.ukrokultur_api.projects.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProjectController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ContactRateLimitFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerWebTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean ProjectService projectService;
    @MockitoBean MultipartJsonReader jsonReader;

    @Test
    void createMultipart_readsJsonAndCallsService() throws Exception {
        var dto = new ProjectUpsertRequestDto(
                "p1",
                new I18nText("en", "de", "uk"),
                new I18nText("en", "de", "uk"),
                null,
                List.of(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                new I18nText("en", "de", "uk"),
                List.of(),
                List.of(),
                List.of(),
                true,
                null
        );

        when(jsonReader.read(anyString(), eq(ProjectUpsertRequestDto.class))).thenReturn(dto);
        when(projectService.createMultipart(eq(dto), any(), any())).thenReturn(
                new ProjectItemDto(
                        "id", "p1", dto.title(), dto.subtitle(), null, List.of(),
                        dto.startDate(), dto.endDate(), dto.description(), List.of(), List.of(), List.of(),
                        true, 0, null, null
                )
        );

        var dataPart = new MockMultipartFile(
                "data", "", "application/json", om.writeValueAsBytes(dto)
        );

        mvc.perform(multipart("/admin/projects/multipart")
                        .file(dataPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("p1"));
    }

    @Test
    void updateMultipart_readsJsonAndCallsService() throws Exception {
        UUID id = UUID.randomUUID();

        var dto = new ProjectUpsertRequestDto(
                null,
                new I18nText("en", "de", "uk"),
                null,
                null,
                List.of(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                new I18nText("en", "de", "uk"),
                List.of(),
                List.of(),
                List.of(),
                true,
                null
        );

        when(jsonReader.read(anyString(), eq(ProjectUpsertRequestDto.class))).thenReturn(dto);
        when(projectService.updateMultipart(eq(id), eq(dto), any(), any())).thenReturn(
                new ProjectItemDto(
                        id.toString(), "p1", dto.title(), dto.subtitle(),
                        null, List.of(), dto.startDate(), dto.endDate(),
                        dto.description(), List.of(), List.of(), List.of(),
                        true, 0, null, null
                )
        );

        var dataPart = new MockMultipartFile("data", "", "application/json", om.writeValueAsBytes(dto));

        mvc.perform(multipart("/admin/projects/{id}/multipart", id)
                        .file(dataPart)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void deleteOneGalleryImage_callsService() throws Exception {
        UUID id = UUID.randomUUID();

        when(projectService.deleteOneGalleryImage(eq(id), eq("u1")))
                .thenReturn(new ProjectItemDto(
                        id.toString(), "p1",
                        new I18nText("en", "de", "uk"), null,
                        null, List.of("u2"), null, null,
                        new I18nText("en", "de", "uk"),
                        List.of(), List.of(), List.of(),
                        true, 0, null, null
                ));

        mvc.perform(delete("/admin/projects/{id}/gallery", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new UrlRequestDto("u1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.galleryImages").isArray());
    }

    @Test
    void reorderGallery_callsService() throws Exception {
        UUID id = UUID.randomUUID();

        when(projectService.reorderGallery(eq(id), eq(List.of("u2", "u1"))))
                .thenReturn(new ProjectItemDto(
                        id.toString(), "p1",
                        new I18nText("en", "de", "uk"), null,
                        null, List.of("u2", "u1"), null, null,
                        new I18nText("en", "de", "uk"),
                        List.of(), List.of(), List.of(),
                        true, 0, null, null
                ));

        mvc.perform(patch("/admin/projects/{id}/gallery/order", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new OrderUrlsRequestDto(List.of("u2", "u1")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.galleryImages[0]").value("u2"));
    }
}