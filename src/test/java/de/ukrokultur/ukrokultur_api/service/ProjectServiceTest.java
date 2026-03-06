package de.ukrokultur.ukrokultur_api.service;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.media.UploadResponseDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.media.MediaService;
import de.ukrokultur.ukrokultur_api.projects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class ProjectServiceTest {

    ProjectRepository repo = mock(ProjectRepository.class);
    MediaService mediaService = mock(MediaService.class);

    ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService(repo, mediaService);
    }

    @Test
    void create_slugIsNormalized() {
        when(repo.findMaxSortOrder()).thenReturn(-1);
        when(repo.existsBySlug(anyString())).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = baseReq("  My Slug  ");
        var dto = service.create(req);

        assertThat(dto.slug()).isNotBlank();
        assertThat(dto.slug()).isNotEqualTo("  My Slug  ");
        assertThat(dto.slug()).contains("my");
    }

    @Test
    void create_duplicateSlug_throws400() {
        when(repo.findMaxSortOrder()).thenReturn(-1);


        when(repo.existsBySlug(anyString())).thenReturn(true);

        var req = baseReq("My Slug");

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(400);
                    assertThat(ae.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                });
    }

    @Test
    void updateMultipart_withNewCoverAndGallery_deletesOldOnlyAfterSuccess() {
        UUID id = UUID.randomUUID();

        Project p = new Project();
        p.setPublicId(id);
        p.setSlug("old-slug");
        p.setPublished(true);
        p.setCoverImage("old-cover");

        ProjectImage oldImg = new ProjectImage();
        oldImg.setUrl("old-g1");
        oldImg.setSortOrder(0);
        p.addImage(oldImg);

        when(repo.findByPublicId(id)).thenReturn(Optional.of(p));
        when(repo.existsBySlug(anyString())).thenReturn(false);

        when(mediaService.upload(any(), eq("projects")))
                .thenReturn(new UploadResponseDto("x", "new-cover"));

        when(mediaService.uploadMany(any(), eq("projects")))
                .thenReturn(List.of(
                        new UploadResponseDto("x", "new-g1"),
                        new UploadResponseDto("x", "new-g2")
                ));

        var req = new ProjectUpsertRequestDto(
                null,
                new I18nText("en", "de", "uk"),
                null,
                null,
                null,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                new I18nText("en", "de", "uk"),
                List.of(),
                List.of(),
                List.of(),
                true,
                null
        );

        var coverFile = new MockMultipartFile("coverImage", "c.png", "image/png", "png".getBytes());
        var g1 = new MockMultipartFile("galleryImages", "1.png", "image/png", "1".getBytes());
        var g2 = new MockMultipartFile("galleryImages", "2.png", "image/png", "2".getBytes());

        service.updateMultipart(id, req, coverFile, List.of(g1, g2));

        verify(mediaService).deleteByPublicUrlQuietly("old-cover");
        verify(mediaService).deleteManyByPublicUrlsQuietly(List.of("old-g1"));
        verify(mediaService, never()).deleteByPublicUrlQuietly("new-cover");
    }

    @Test
    void reorderGallery_unknownUrl_throws400() {
        Project p = new Project();
        p.setPublicId(UUID.randomUUID());
        p.setSlug("s");

        ProjectImage i1 = new ProjectImage(); i1.setUrl("u1"); i1.setSortOrder(0);
        ProjectImage i2 = new ProjectImage(); i2.setUrl("u2"); i2.setSortOrder(1);
        p.addImage(i1); p.addImage(i2);

        when(repo.findByPublicId(p.getPublicId())).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.reorderGallery(p.getPublicId(), List.of("u1", "uX")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(400);
                    assertThat(ae.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                });
    }

    @Test
    void reorderGallery_requiresAllUrls_throws400() {
        Project p = new Project();
        p.setPublicId(UUID.randomUUID());
        p.setSlug("s");

        ProjectImage i1 = new ProjectImage(); i1.setUrl("u1"); i1.setSortOrder(0);
        ProjectImage i2 = new ProjectImage(); i2.setUrl("u2"); i2.setSortOrder(1);
        p.addImage(i1); p.addImage(i2);

        when(repo.findByPublicId(p.getPublicId())).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.reorderGallery(p.getPublicId(), List.of("u1")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(400);
                    assertThat(ae.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                });
    }

    @Test
    void deleteOneGalleryImage_notFound_throws404() {
        Project p = new Project();
        p.setPublicId(UUID.randomUUID());
        p.setSlug("s");

        ProjectImage i1 = new ProjectImage(); i1.setUrl("u1"); i1.setSortOrder(0);
        p.addImage(i1);

        when(repo.findByPublicId(p.getPublicId())).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.deleteOneGalleryImage(p.getPublicId(), "uX"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(404);
                    assertThat(ae.getCode()).isEqualTo(ErrorCode.NOT_FOUND);
                });
    }

    private static ProjectUpsertRequestDto baseReq(String slug) {
        return new ProjectUpsertRequestDto(
                slug,
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
    }
}