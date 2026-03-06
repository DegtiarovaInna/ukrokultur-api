package de.ukrokultur.ukrokultur_api.service;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.home.HomeUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.home.*;
import de.ukrokultur.ukrokultur_api.media.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


class HomeServiceTest {

    HomePageRepository pageRepo = mock(HomePageRepository.class);
    HomeWorkFieldRepository workRepo = mock(HomeWorkFieldRepository.class);
    MediaService mediaService = mock(MediaService.class);

    HomeService service;

    @BeforeEach
    void setUp() {
        service = new HomeService(pageRepo, workRepo, mediaService);


        HomePage page = new HomePage();
        page.setHeroTitleEn(" "); page.setHeroTitleDe(" "); page.setHeroTitleUk(" ");
        page.setHeroSubtitleEn(" "); page.setHeroSubtitleDe(" "); page.setHeroSubtitleUk(" ");
        page.setMissionTitleEn(" "); page.setMissionTitleDe(" "); page.setMissionTitleUk(" ");
        page.setMissionTextEn(" "); page.setMissionTextDe(" "); page.setMissionTextUk(" ");
        page.setHeroPublished(true);
        page.setMissionPublished(true);
        page.setWorkFieldsPublished(true);

        when(pageRepo.findById(1L)).thenReturn(Optional.of(page));
    }

    @Test
    void upsert_whenItemsNull_throws400() {
        var req = reqWithItems(null);

        assertThatThrownBy(() -> service.upsert(req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(400);
                    assertThat(ae.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                });
    }

    @Test
    void upsert_whenUnknownId_throws404() {
        when(workRepo.findAllByOrderBySortOrderAsc()).thenReturn(List.of());
        String unknownId = UUID.randomUUID().toString();

        var items = List.of(new HomeUpsertRequestDto.HomeWorkFieldItemUpsertDto(
                unknownId, "slug-1", true,
                new I18nText("a","b","c"),
                new I18nText("d","e","f")
        ));

        var req = reqWithItems(items);

        assertThatThrownBy(() -> service.upsert(req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(404);
                    assertThat(ae.getCode()).isEqualTo(ErrorCode.NOT_FOUND);
                });
    }

    @Test
    void upsert_duplicateSlug_throws400() {
        when(workRepo.findAllByOrderBySortOrderAsc()).thenReturn(List.of());
        when(workRepo.existsBySlug("dup")).thenReturn(true);

        var items = List.of(new HomeUpsertRequestDto.HomeWorkFieldItemUpsertDto(
                null, "dup", true,
                new I18nText("a","b","c"),
                new I18nText("d","e","f")
        ));

        var req = reqWithItems(items);

        assertThatThrownBy(() -> service.upsert(req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException ae = (ApiException) ex;
                    assertThat(ae.getStatus()).isEqualTo(400);
                    assertThat(ae.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                });
    }

    @Test
    void upsert_reordersSortOrder_andDeletesMissing() {
        HomeWorkFieldItem a = new HomeWorkFieldItem();
        a.setPublicId(UUID.randomUUID());
        a.setSlug("a");
        a.setSortOrder(0);
        a.setPublished(true);
        a.setTitleEn("t"); a.setTitleDe("t"); a.setTitleUk("t");
        a.setDescriptionEn("d"); a.setDescriptionDe("d"); a.setDescriptionUk("d");

        HomeWorkFieldItem b = new HomeWorkFieldItem();
        b.setPublicId(UUID.randomUUID());
        b.setSlug("b");
        b.setSortOrder(1);
        b.setPublished(true);
        b.setTitleEn("t"); b.setTitleDe("t"); b.setTitleUk("t");
        b.setDescriptionEn("d"); b.setDescriptionDe("d"); b.setDescriptionUk("d");

        when(workRepo.findAllByOrderBySortOrderAsc()).thenReturn(List.of(a, b));
        when(workRepo.existsBySlug(anyString())).thenReturn(false);

        var items = List.of(new HomeUpsertRequestDto.HomeWorkFieldItemUpsertDto(
                b.getPublicId().toString(), "b", true,
                new I18nText("x","x","x"),
                new I18nText("y","y","y")
        ));

        var req = reqWithItems(items);

        service.upsert(req);

        assertThat(b.getSortOrder()).isEqualTo(0);
        verify(workRepo).delete(a);
        verify(workRepo).save(b);
    }

    private static HomeUpsertRequestDto reqWithItems(List<HomeUpsertRequestDto.HomeWorkFieldItemUpsertDto> items) {
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
                        items
                )
        );
    }
}