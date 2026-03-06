package de.ukrokultur.ukrokultur_api.service;

import de.ukrokultur.ukrokultur_api.about.*;
import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.about.AboutIntroUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.about.AboutMemberUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.media.UploadResponseDto;
import de.ukrokultur.ukrokultur_api.media.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class AboutServiceTest {

    AboutIntroRepository introRepo = mock(AboutIntroRepository.class);
    AboutMemberRepository memberRepo = mock(AboutMemberRepository.class);
    MediaService mediaService = mock(MediaService.class);

    AboutService service;

    @BeforeEach
    void setUp() {
        service = new AboutService(introRepo, memberRepo, mediaService);
    }

    @Test
    void updateIntroMultipart_withFile_deletesOldAfterSuccess() {
        AboutIntro intro = new AboutIntro();
        intro.setImage("old-url");
        intro.setTitle(new I18nEmbeddable("a","a","a"));
        intro.setText(new I18nEmbeddable("b","b","b"));
        intro.setPublished(true);

        when(introRepo.findAll()).thenReturn(List.of(intro));

        var uploadRes = new de.ukrokultur.ukrokultur_api.common.dto.media.UploadResponseDto("x", "new-url");
        when(mediaService.upload(any(), eq("about"))).thenReturn(uploadRes);

        var data = new AboutIntroUpsertRequestDto(
                null,
                new I18nText("t1","t2","t3"),
                new I18nText("x1","x2","x3"),
                true
        );

        var file = new MockMultipartFile("image", "a.png", "image/png", "png".getBytes());

        service.updateIntroMultipart(data, file);

        verify(mediaService).deleteByPublicUrlQuietly("old-url");
        verify(mediaService, never()).deleteByPublicUrlQuietly("new-url"); // новый не удаляем при success
    }

    @Test
    void updateIntroMultipart_whenUpdateThrows_deletesUploaded_only() {
        AboutIntro intro = new AboutIntro();
        intro.setImage("old-url");
        intro.setTitle(new I18nEmbeddable("a","a","a"));
        intro.setText(new I18nEmbeddable("b","b","b"));
        intro.setPublished(true);

        when(introRepo.findAll()).thenReturn(List.of(intro));

        var uploadRes = new de.ukrokultur.ukrokultur_api.common.dto.media.UploadResponseDto("x", "new-url");
        when(mediaService.upload(any(), eq("about"))).thenReturn(uploadRes);

        when(introRepo.save(any())).thenThrow(new RuntimeException("db down"));

        var data = new AboutIntroUpsertRequestDto(
                null,
                new I18nText("t1","t2","t3"),
                new I18nText("x1","x2","x3"),
                true
        );

        var file = new MockMultipartFile("image", "a.png", "image/png", "png".getBytes());

        try {
            service.updateIntroMultipart(data, file);
        } catch (RuntimeException ignored) {}

        verify(mediaService).deleteByPublicUrlQuietly("new-url");
        verify(mediaService, never()).deleteByPublicUrlQuietly("old-url");
    }
    @Test
    void createMember_setsSortOrderMaxPlusOne() {
        when(memberRepo.findMaxSortOrder()).thenReturn(5);
        when(memberRepo.existsBySlug(anyString())).thenReturn(false);
        when(memberRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new AboutMemberUpsertRequestDto(
                "member-1",
                "Name",
                "img",
                null,
                true,
                null,
                new I18nText("role", "role", "role"),
                new I18nText("bio", "bio", "bio")
        );

        service.createMember(req);


        var captor = org.mockito.ArgumentCaptor.forClass(AboutMember.class);
        verify(memberRepo).save(captor.capture());
        assertThat(captor.getValue().getSortOrder()).isEqualTo(6);
    }


    @Test
    void updateMember_withOrder_movesAndRecalculatesSortOrder() {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        UUID idC = UUID.randomUUID();

        AboutMember a = baseMember("a", 0);
        AboutMember b = baseMember("b", 1);
        AboutMember c = baseMember("c", 2);

        setId(b, idB);
        setId(a, idA);
        setId(c, idC);

        when(memberRepo.findById(idB)).thenReturn(Optional.of(b));


        when(memberRepo.findAllOrdered()).thenReturn(new ArrayList<>(List.of(a, b, c)));

        when(memberRepo.existsBySlug(anyString())).thenReturn(false);
        when(memberRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(memberRepo.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new AboutMemberUpsertRequestDto(
                "b",
                "B",
                b.getImage(),
                0,
                true,
                null,
                new I18nText("r", "r", "r"),
                new I18nText("bio", "bio", "bio")
        );

        service.updateMember(idB, req);

        var saveAllCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(memberRepo).saveAll(saveAllCaptor.capture());


        List<AboutMember> saved = saveAllCaptor.getValue();

        assertThat(saved).extracting(AboutMember::getSlug).containsExactly("b", "a", "c");
        assertThat(saved).extracting(AboutMember::getSortOrder).containsExactly(0, 1, 2);
    }
    private static void setId(AboutMember m, UUID id) {
        try {
            Field f = AboutMember.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(m, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void createMemberMultipart_whenSaveThrows_deletesUploaded() {
        when(memberRepo.findMaxSortOrder()).thenReturn(-1);
        when(memberRepo.existsBySlug(anyString())).thenReturn(false);

        when(mediaService.upload(any(), eq("about")))
                .thenReturn(new UploadResponseDto("x", "new-url"));

        when(memberRepo.save(any())).thenThrow(new RuntimeException("db down"));

        var data = new AboutMemberUpsertRequestDto(
                "m1",
                "Name",
                null,
                null,
                true,
                null,
                new I18nText("role", "role", "role"),
                new I18nText("bio", "bio", "bio")
        );

        var file = new MockMultipartFile("image", "a.png", "image/png", "png".getBytes());

        assertThatThrownBy(() -> service.createMemberMultipart(data, file))
                .isInstanceOf(RuntimeException.class);

        verify(mediaService).deleteByPublicUrlQuietly("new-url");
    }

    private static AboutMember baseMember(String slug, int sort) {
        AboutMember m = new AboutMember();
        m.setSlug(slug);
        m.setName(slug.toUpperCase());
        m.setImage("img");
        m.setSortOrder(sort);
        m.setPublished(true);
        m.setInstagramUrl(null);
        m.setRole(new I18nEmbeddable("r", "r", "r"));
        m.setBiography(new I18nEmbeddable("b", "b", "b"));
        return m;
    }
}