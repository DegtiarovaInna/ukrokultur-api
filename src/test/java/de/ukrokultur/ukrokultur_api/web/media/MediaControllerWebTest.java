package de.ukrokultur.ukrokultur_api.web.media;

import tools.jackson.databind.ObjectMapper;
import de.ukrokultur.ukrokultur_api.common.dto.media.UploadResponseDto;
import de.ukrokultur.ukrokultur_api.contact.ContactRateLimitFilter;
import de.ukrokultur.ukrokultur_api.media.MediaController;
import de.ukrokultur.ukrokultur_api.media.MediaService;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = MediaController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ContactRateLimitFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class MediaControllerWebTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean MediaService mediaService;

    private static UploadResponseDto dto(String objectPath, String publicUrl) {
        return new UploadResponseDto(objectPath, publicUrl);
    }

    @Test
    void upload_ok_returnsDto() throws Exception {
        var file = new MockMultipartFile(
                "file",
                "a.png",
                MediaType.IMAGE_PNG_VALUE,
                "png-bytes".getBytes()
        );

        when(mediaService.upload(any()))
                .thenReturn(dto("pages/a.png", "https://cdn.example/pages/a.png"));

        mvc.perform(multipart("/admin/media/upload")
                        .file(file)
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.objectPath").value("pages/a.png"))
                .andExpect(jsonPath("$.publicUrl").value("https://cdn.example/pages/a.png"));
    }

    @Test
    void uploadToFolder_ok_returnsDto() throws Exception {
        var file = new MockMultipartFile(
                "file",
                "b.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "jpg-bytes".getBytes()
        );

        when(mediaService.upload(any(), eq("news")))
                .thenReturn(dto("news/b.jpg", "https://cdn.example/news/b.jpg"));

        mvc.perform(multipart("/admin/media/upload/{folder}", "news")
                        .file(file)
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objectPath").value("news/b.jpg"))
                .andExpect(jsonPath("$.publicUrl").value("https://cdn.example/news/b.jpg"));
    }

    @Test
    void uploadBatch_ok_returnsList() throws Exception {
        var f1 = new MockMultipartFile("files", "1.png", MediaType.IMAGE_PNG_VALUE, "1".getBytes());
        var f2 = new MockMultipartFile("files", "2.png", MediaType.IMAGE_PNG_VALUE, "2".getBytes());

        when(mediaService.uploadMany(any(), eq("projects")))
                .thenReturn(List.of(
                        dto("projects/1.png", "https://cdn.example/projects/1.png"),
                        dto("projects/2.png", "https://cdn.example/projects/2.png")
                ));

        mvc.perform(multipart("/admin/media/upload/batch/{folder}", "projects")
                        .file(f1)
                        .file(f2)
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].objectPath").value("projects/1.png"))
                .andExpect(jsonPath("$[1].objectPath").value("projects/2.png"));
    }

    @Test
    void delete_ok() throws Exception {
        doNothing().when(mediaService).delete(eq("news/x.png"));

        mvc.perform(delete("/admin/media")
                        .param("objectPath", "news/x.png")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }
}