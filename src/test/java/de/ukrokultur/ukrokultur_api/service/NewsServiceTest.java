package de.ukrokultur.ukrokultur_api.service;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.media.UploadResponseDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsVideoDto;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.media.MediaService;
import de.ukrokultur.ukrokultur_api.news.News;
import de.ukrokultur.ukrokultur_api.news.NewsImage;
import de.ukrokultur.ukrokultur_api.news.NewsRepository;
import de.ukrokultur.ukrokultur_api.news.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NewsServiceTest {

    NewsRepository repo = mock(NewsRepository.class);
    MediaService mediaService = mock(MediaService.class);

    NewsService service;

    @BeforeEach
    void setUp() {
        service = new NewsService(repo, mediaService);
    }

    @Test
    void create_slugIsNormalized() {
        when(repo.existsBySlug(anyString())).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = baseReq("  My News  ");
        var dto = service.create(req);

        assertThat(dto.slug()).isNotBlank();
        assertThat(dto.slug()).isNotEqualTo("  My News  ");
    }

    @Test
    void create_duplicateSlug_throws400() {
        when(repo.existsBySlug(anyString())).thenReturn(true);

        var req = baseReq("My News");

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(400);
                    assertThat(ae.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                });
    }

    @Test
    void create_setsPublishedAtUtcStartOfDay() {
        when(repo.existsBySlug(anyString())).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDate d = LocalDate.of(2025, 6, 21);
        var req = new NewsUpsertRequestDto(
                null,
                d,
                null,
                new I18nText("en", "de", "uk"),
                new I18nText("en", "de", "uk"),
                List.of(),
                List.of(),
                true
        );

        service.create(req);

        ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
        verify(repo).save(captor.capture());

        OffsetDateTime expected = d.atStartOfDay().atOffset(ZoneOffset.UTC);
        assertThat(captor.getValue().getPublishedAt()).isEqualTo(expected);
    }

    @Test
    void reorderImages_firstBecomesCover() {
        News n = new News();
        n.setPublicId(UUID.randomUUID());
        n.setPublished(true);
        n.setSlug("s");

        NewsImage i1 = new NewsImage();
        i1.setUrl("u1");
        i1.setSortOrder(0);
        i1.setCover(true);

        NewsImage i2 = new NewsImage();
        i2.setUrl("u2");
        i2.setSortOrder(1);
        i2.setCover(false);

        n.addImage(i1);
        n.addImage(i2);

        when(repo.findByPublicId(n.getPublicId())).thenReturn(Optional.of(n));

        var dto = service.reorderImages(n.getPublicId(), List.of("u2", "u1"));

        assertThat(dto.images()).containsExactly("u2", "u1");
        assertThat(n.getImages().get(0).getUrl()).isEqualTo("u2");
        assertThat(n.getImages().get(0).isCover()).isTrue();
        assertThat(n.getImages().stream().filter(NewsImage::isCover).count()).isEqualTo(1);
    }

    @Test
    void deleteOneImage_notFound_throws404() {
        News n = new News();
        n.setPublicId(UUID.randomUUID());

        NewsImage i1 = new NewsImage();
        i1.setUrl("u1");
        i1.setSortOrder(0);
        i1.setCover(true);
        n.addImage(i1);

        when(repo.findByPublicId(n.getPublicId())).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.deleteOneImage(n.getPublicId(), "uX"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(404);
                    assertThat(ae.getCode()).isEqualTo(ErrorCode.NOT_FOUND);
                });
    }

    @Test
    void updateMultipart_withNewImages_deletesOldAfterSuccess() {
        News n = new News();
        n.setPublicId(UUID.randomUUID());
        n.setSlug("s");
        n.setPublished(true);
        n.setPublishedAt(OffsetDateTime.now());

        NewsImage old1 = new NewsImage();
        old1.setUrl("old1");
        old1.setSortOrder(0);
        old1.setCover(true);
        n.addImage(old1);

        when(repo.findByPublicId(n.getPublicId())).thenReturn(Optional.of(n));
        when(repo.existsBySlug(anyString())).thenReturn(false);

        when(mediaService.uploadMany(any(), eq("news")))
                .thenReturn(List.of(new UploadResponseDto("x", "new1")));

        var req = new NewsUpsertRequestDto(
                null,
                LocalDate.of(2025, 6, 21),
                null,
                new I18nText("en", "de", "uk"),
                new I18nText("en", "de", "uk"),
                null,
                List.of(),
                true
        );

        var file = new MockMultipartFile("images", "a.png", "image/png", "png".getBytes());
        service.updateMultipart(n.getPublicId(), req, List.of(file), null);

        verify(mediaService).deleteManyByPublicUrlsQuietly(List.of("old1"));
    }

    @Test
    void createMultipart_withVideoFile_uploadsVideoAndReturnsMp4() {
        when(repo.existsBySlug(anyString())).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mediaService.upload(any(MultipartFile.class), eq("news")))
                .thenReturn(new UploadResponseDto("news/video.mp4", "https://cdn/news/video.mp4"));

        var req = new NewsUpsertRequestDto(
                null,
                LocalDate.of(2025, 6, 21),
                null,
                new I18nText("en", "de", "uk"),
                new I18nText("en", "de", "uk"),
                List.of(),
                List.of(new NewsVideoDto("mp4", "", "Watch video")),
                true
        );

        var video = new MockMultipartFile("video", "video.mp4", "video/mp4", "mp4".getBytes());

        NewsItemDto dto = service.createMultipart(req, null, video);

        assertThat(dto.videos()).hasSize(1);
        assertThat(dto.videos().get(0).type()).isEqualTo("mp4");
        assertThat(dto.videos().get(0).url()).isEqualTo("https://cdn/news/video.mp4");
        assertThat(dto.videos().get(0).label()).isEqualTo("Watch video");
    }

    @Test
    void updateMultipart_withNewVideo_deletesOldManagedVideoAfterSuccess() {
        News n = new News();
        n.setPublicId(UUID.randomUUID());
        n.setSlug("s");
        n.setPublished(true);
        n.setPublishedAt(OffsetDateTime.now());
        n.setVideoType("mp4");
        n.setVideoUrl("https://cdn/news/old-video.mp4");
        n.setVideoLabel("Old");

        when(repo.findByPublicId(n.getPublicId())).thenReturn(Optional.of(n));
        when(repo.existsBySlug(anyString())).thenReturn(false);
        when(mediaService.upload(any(MultipartFile.class), eq("news")))
                .thenReturn(new UploadResponseDto("news/new-video.mp4", "https://cdn/news/new-video.mp4"));
        when(mediaService.isManagedPublicUrl("https://cdn/news/old-video.mp4")).thenReturn(true);

        var req = new NewsUpsertRequestDto(
                null,
                LocalDate.of(2025, 6, 21),
                null,
                new I18nText("en", "de", "uk"),
                new I18nText("en", "de", "uk"),
                null,
                List.of(new NewsVideoDto("mp4", "", "New")),
                true
        );

        var video = new MockMultipartFile("video", "video.mp4", "video/mp4", "mp4".getBytes());

        service.updateMultipart(n.getPublicId(), req, null, video);

        verify(mediaService).deleteByPublicUrlQuietly("https://cdn/news/old-video.mp4");
    }

    @Test
    void updateMultipart_withVideosEmpty_deletesOldManagedVideoAfterSuccess() {
        News n = new News();
        n.setPublicId(UUID.randomUUID());
        n.setSlug("s");
        n.setPublished(true);
        n.setPublishedAt(OffsetDateTime.now());
        n.setVideoType("mp4");
        n.setVideoUrl("https://cdn/news/old-video.mp4");
        n.setVideoLabel("Old");

        when(repo.findByPublicId(n.getPublicId())).thenReturn(Optional.of(n));
        when(repo.existsBySlug(anyString())).thenReturn(false);
        when(mediaService.isManagedPublicUrl("https://cdn/news/old-video.mp4")).thenReturn(true);

        var req = new NewsUpsertRequestDto(
                null,
                LocalDate.of(2025, 6, 21),
                null,
                new I18nText("en", "de", "uk"),
                new I18nText("en", "de", "uk"),
                null,
                List.of(),
                true
        );

        NewsItemDto dto = service.updateMultipart(n.getPublicId(), req, null, null);

        assertThat(dto.videos()).isEmpty();
        verify(mediaService).deleteByPublicUrlQuietly("https://cdn/news/old-video.mp4");
    }

    @Test
    void delete_withManagedVideo_deletesVideoFromStorage() {
        News n = new News();
        n.setPublicId(UUID.randomUUID());
        n.setVideoUrl("https://cdn/news/old-video.mp4");

        when(repo.findByPublicId(n.getPublicId())).thenReturn(Optional.of(n));
        when(mediaService.isManagedPublicUrl("https://cdn/news/old-video.mp4")).thenReturn(true);

        service.delete(n.getPublicId());

        verify(repo).delete(n);
        verify(mediaService).deleteByPublicUrlQuietly("https://cdn/news/old-video.mp4");
    }

    private static NewsUpsertRequestDto baseReq(String slug) {
        return new NewsUpsertRequestDto(
                slug,
                LocalDate.of(2025, 6, 21),
                LocalDate.of(2025, 6, 21),
                new I18nText("en", "de", "uk"),
                new I18nText("en", "de", "uk"),
                List.of(),
                List.of(),
                true
        );
    }
}