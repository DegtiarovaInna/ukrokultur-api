package de.ukrokultur.ukrokultur_api.config;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsVideoDto;
import de.ukrokultur.ukrokultur_api.news.NewsRepository;
import de.ukrokultur.ukrokultur_api.news.NewsService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.List;

@Configuration
@Profile({"dev", "demo"})
public class DemoDataInitializer {

    @Bean
    ApplicationRunner initDemoNews(NewsRepository newsRepository, NewsService newsService) {
        return args -> {
            if (newsRepository.count() > 0) return;

            create(newsService,
                    "ka153-accepted",
                    LocalDate.of(2025, 5, 16),
                    LocalDate.of(2025, 6, 21),
                    tr(
                            "Good News: Our KA153 Project Has Been Accepted!",
                            "Gute Nachrichten: Unser KA153-Projekt wurde angenommen!",
                            "Гарні новини: наш проєкт KA153 прийнято!"
                    ),
                    tr(
                            "We are proud to announce our Erasmus+ KA153 project has been approved.",
                            "Wir freuen uns, dass unser Erasmus+ KA153-Projekt genehmigt wurde.",
                            "Ми раді повідомити, що наш проєкт Erasmus+ KA153 було офіційно схвалено."
                    ),
                    List.of(),
                    List.of(),
                    true
            );

            create(newsService,
                    "community-lunch",
                    LocalDate.of(2025, 6, 11),
                    LocalDate.of(2025, 6, 21),
                    tr(
                            "Community Lunch & Storytelling Event",
                            "Gemeinsames Mittagessen & Storytelling",
                            "Спільний обід та сторітелінг"
                    ),
                    tr(
                            "A community gathering with shared lunch and storytelling rounds.",
                            "Ein Treffen mit gemeinsamem Mittagessen und Storytelling-Runden.",
                            "Зустріч спільноти зі спільним обідом і сторітелінгом."
                    ),
                    List.of(),
                    List.of(
                            // new NewsVideoDto("instagram", "https://www.instagram.com/reel/XXXX/", "Watch on Instagram")
                    ),
                    true
            );

            create(newsService,
                    "bread-and-salt",
                    LocalDate.of(2025, 3, 31),
                    null,
                    tr(
                            "“Bread and Salt” — Ukrainian Food-Sharing Event",
                            "„Brot und Salz“ — Ukrainisches Food-Sharing-Event",
                            "«Хліб і сіль» — український фуд-шерінг"
                    ),
                    tr(
                            "An event bringing together locals with Ukrainian dishes and music.",
                            "Eine Veranstaltung mit ukrainischen Gerichten und Musik.",
                            "Подія, що об’єднує місцевих жителів українськими стравами та музикою."
                    ),
                    List.of(),
                    List.of(),
                    true
            );
        };
    }

    private void create(
            NewsService newsService,
            String id,
            LocalDate newsDate,
            LocalDate eventDate,
            I18nText title,
            I18nText content,
            List<String> images,
            List<NewsVideoDto> videos,
            boolean published
    ) {
        NewsUpsertRequestDto req = new NewsUpsertRequestDto(
                id,
                newsDate,
                eventDate,
                title,
                content,
                images == null ? List.of() : images,
                videos == null ? List.of() : videos,
                published
        );

        newsService.create(req);
    }

    private I18nText tr(String en, String de, String uk) {
        return new I18nText(en, de, uk);
    }
}
