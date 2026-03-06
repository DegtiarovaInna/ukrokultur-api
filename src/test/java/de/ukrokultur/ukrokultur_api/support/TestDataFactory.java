package de.ukrokultur.ukrokultur_api.support;

import de.ukrokultur.ukrokultur_api.auth.Role;
import de.ukrokultur.ukrokultur_api.auth.User;
import de.ukrokultur.ukrokultur_api.auth.UserRepository;
import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.about.AboutIntroUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.about.AboutMemberUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.home.HomeUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectPartnerDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectUpsertRequestDto;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

public final class TestDataFactory {
    private TestDataFactory() {}

    public static I18nText tr(String en, String de, String uk) {
        return new I18nText(en, de, uk);
    }

    public static NewsUpsertRequestDto newsReq(String slug) {
        return new NewsUpsertRequestDto(
                slug,
                LocalDate.of(2025, 6, 21),
                LocalDate.of(2025, 6, 21),
                tr("en title", "de title", "uk title"),
                tr("en text", "de text", "uk text"),
                List.of("https://cdn.example/a.jpg", "https://cdn.example/b.jpg"),
                List.of(),
                true
        );
    }

    public static ProjectUpsertRequestDto projectReq(String slug) {
        return new ProjectUpsertRequestDto(
                slug,
                tr("en title", "de title", "uk title"),
                tr("en sub", "de sub", "uk sub"),
                "https://cdn.example/cover.jpg",
                List.of("https://cdn.example/g1.jpg", "https://cdn.example/g2.jpg"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                tr("en desc", "de desc", "uk desc"),
                List.of(tr("g1", "g1", "g1")),
                List.of(tr("a1", "a1", "a1")),
                List.of(new ProjectPartnerDto(tr("DE", "DE", "DE"), "Org")),
                true,
                null
        );
    }

    public static AboutIntroUpsertRequestDto aboutIntroReq() {
        return new AboutIntroUpsertRequestDto(
                "https://cdn.example/about.jpg",
                tr("t1", "t2", "t3"),
                tr("x1", "x2", "x3"),
                true
        );
    }

    public static AboutMemberUpsertRequestDto aboutMemberReq(String slug, String name) {
        return new AboutMemberUpsertRequestDto(
                slug,
                name,
                "https://cdn.example/m.jpg",
                null,
                true,
                "https://instagram.com/test",
                tr("role", "role", "role"),
                tr("bio", "bio", "bio")
        );
    }

    public static HomeUpsertRequestDto homeReq() {
        return new HomeUpsertRequestDto(
                new HomeUpsertRequestDto.HomeHeroUpsertDto(
                        "https://cdn.example/hero.jpg",
                        tr("h1", "h2", "h3"),
                        tr("s1", "s2", "s3"),
                        true
                ),
                new HomeUpsertRequestDto.HomeMissionUpsertDto(
                        "https://cdn.example/m.jpg",
                        tr("m1", "m2", "m3"),
                        tr("tx1", "tx2", "tx3"),
                        true
                ),
                new HomeUpsertRequestDto.HomeWorkFieldsUpsertDto(
                        true,
                        List.of(
                                new HomeUpsertRequestDto.HomeWorkFieldItemUpsertDto(
                                        null,
                                        "field-1",
                                        true,
                                        tr("a", "b", "c"),
                                        tr("d", "e", "f")
                                )
                        )
                )
        );
    }

    public static User createAdmin(UserRepository repo, PasswordEncoder encoder, String email, String rawPassword) {
        User u = new User(email, encoder.encode(rawPassword), Role.ADMIN);
        return repo.save(u);
    }
}