package ru.volzhanin.applicantsservice.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.volzhanin.applicantsservice.dto.news.NewsItemDto;
import ru.volzhanin.applicantsservice.exception.ExternalServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

@Service
public class NewsService {
    private static final URI NEWS_URI = URI.create("https://nnov.hse.ru/news/");
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);
    private static final Pattern NEWS_ID_PATTERN = Pattern.compile("/news/(\\d+)\\.html(?:$|\\?)");
    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("янв", 1),
            Map.entry("января", 1),
            Map.entry("фев", 2),
            Map.entry("февраля", 2),
            Map.entry("мар", 3),
            Map.entry("марта", 3),
            Map.entry("апр", 4),
            Map.entry("апреля", 4),
            Map.entry("май", 5),
            Map.entry("мая", 5),
            Map.entry("июн", 6),
            Map.entry("июня", 6),
            Map.entry("июл", 7),
            Map.entry("июля", 7),
            Map.entry("авг", 8),
            Map.entry("августа", 8),
            Map.entry("сен", 9),
            Map.entry("сентября", 9),
            Map.entry("окт", 10),
            Map.entry("октября", 10),
            Map.entry("ноя", 11),
            Map.entry("ноября", 11),
            Map.entry("дек", 12),
            Map.entry("декабря", 12)
    );

    private final HttpClient httpClient;
    private final Clock clock;
    private final AtomicReference<CachedNews> cache = new AtomicReference<>();

    @Autowired
    public NewsService(HttpClient httpClient) {
        this(httpClient, Clock.systemUTC());
    }

    NewsService(HttpClient httpClient, Clock clock) {
        this.httpClient = httpClient;
        this.clock = clock;
    }

    public List<NewsItemDto> getNews() {
        CachedNews cachedNews = cache.get();
        Instant now = clock.instant();
        if (cachedNews != null && cachedNews.expiresAt().isAfter(now)) {
            return cachedNews.items();
        }

        List<NewsItemDto> items = loadNews();
        cache.set(new CachedNews(items, now.plus(CACHE_TTL)));
        return items;
    }

    private List<NewsItemDto> loadNews() {
        HttpRequest request = HttpRequest.newBuilder(NEWS_URI)
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "interactive-map-applicants-service/1.0")
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ExternalServiceException("Не удалось загрузить новости", ex);
        }

        if (response.statusCode() != 200) {
            throw new ExternalServiceException("Источник новостей вернул статус " + response.statusCode());
        }

        return parseNews(response.body());
    }

    List<NewsItemDto> parseNews(String html) {
        Document document = Jsoup.parse(html, NEWS_URI.toString());
        return document.select("div.post")
                .stream()
                .map(this::parseNewsItem)
                .sorted(Comparator.comparing(NewsItemDto::date).reversed()
                        .thenComparing(NewsItemDto::id, Comparator.reverseOrder()))
                .toList();
    }

    private NewsItemDto parseNewsItem(Element post) {
        Element titleLink = post.selectFirst("h2 a[href]");
        if (titleLink == null) {
            throw new ExternalServiceException("Не удалось разобрать новость: отсутствует ссылка");
        }

        String link = titleLink.attr("abs:href").trim();
        String title = normalizeText(titleLink.text());
        String text = normalizeText(post.select("div.post__text").text());
        LocalDate date = parseDate(post);

        return new NewsItemDto(resolveNewsId(link), date, title, text, link);
    }

    private LocalDate parseDate(Element post) {
        Element dayElement = post.selectFirst(".post-meta__day");
        Element monthElement = post.selectFirst(".post-meta__month");
        Element yearElement = post.selectFirst(".post-meta__year");
        if (dayElement == null || monthElement == null || yearElement == null) {
            throw new ExternalServiceException("Не удалось разобрать дату новости");
        }

        int day = Integer.parseInt(normalizeText(dayElement.text()));
        String monthText = normalizeText(monthElement.text()).toLowerCase(Locale.ROOT);
        Integer month = MONTHS.get(monthText);
        if (month == null) {
            throw new ExternalServiceException("Неизвестный месяц в новости: " + monthText);
        }

        int year = Integer.parseInt(normalizeText(yearElement.text()));
        return LocalDate.of(year, month, day);
    }

    private long resolveNewsId(String link) {
        Matcher matcher = NEWS_ID_PATTERN.matcher(link);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }

        CRC32 crc32 = new CRC32();
        crc32.update(link.getBytes(StandardCharsets.UTF_8));
        return crc32.getValue();
    }

    private String normalizeText(String value) {
        return value.replace('\u00A0', ' ').trim().replaceAll("\\s+", " ");
    }

    private record CachedNews(List<NewsItemDto> items, Instant expiresAt) {
    }
}
