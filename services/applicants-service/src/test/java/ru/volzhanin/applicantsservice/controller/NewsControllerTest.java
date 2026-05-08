package ru.volzhanin.applicantsservice.controller;

import org.junit.jupiter.api.Test;
import ru.volzhanin.applicantsservice.dto.news.NewsItemDto;
import ru.volzhanin.applicantsservice.service.NewsService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NewsControllerTest {

    private final NewsService newsService = mock(NewsService.class);
    private final NewsController controller = new NewsController(newsService);

    @Test
    void getNews_delegatesToNewsService() {
        NewsItemDto newsItem = new NewsItemDto(
                1L,
                LocalDate.of(2026, 5, 8),
                "Title",
                "Text",
                "https://nnov.hse.ru/news/1.html"
        );
        when(newsService.getNews()).thenReturn(List.of(newsItem));

        List<NewsItemDto> result = controller.getNews();

        assertThat(result).containsExactly(newsItem);
        verify(newsService).getNews();
    }
}
