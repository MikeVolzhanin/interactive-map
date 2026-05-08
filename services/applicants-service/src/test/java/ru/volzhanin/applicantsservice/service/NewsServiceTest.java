package ru.volzhanin.applicantsservice.service;

import org.junit.jupiter.api.Test;
import ru.volzhanin.applicantsservice.dto.news.NewsItemDto;
import ru.volzhanin.applicantsservice.exception.ExternalServiceException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NewsServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-08T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void parseNews_returnsSortedNewsWithLinks() {
        NewsService service = new NewsService(mock(HttpClient.class), CLOCK);

        List<NewsItemDto> result = service.parseNews("""
                <div class="post">
                  <div class="post-meta__date">
                    <div class="post-meta__day">10</div>
                    <div class="post-meta__month">апр</div>
                    <div class="post-meta__year">2026</div>
                  </div>
                  <div class="post__content">
                    <h2><a href="https://nnov.hse.ru/news/1147018975.html">День открытых дверей</a></h2>
                    <div class="post__text"><p>19 апреля пройдет встреча для абитуриентов.</p></div>
                  </div>
                </div>
                <div class="post">
                  <div class="post-meta__date">
                    <div class="post-meta__day">8</div>
                    <div class="post-meta__month">мая</div>
                    <div class="post-meta__year">2026</div>
                  </div>
                  <div class="post__content">
                    <h2><a href="https://nnov.hse.ru/news/1149018975.html">Летние консультации</a></h2>
                    <div class="post__text"><p>Абитуриенты могут выбрать формат участия.</p></div>
                  </div>
                </div>
                """);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1149018975L);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 5, 8));
        assertThat(result.get(0).title()).isEqualTo("Летние консультации");
        assertThat(result.get(0).text()).isEqualTo("Абитуриенты могут выбрать формат участия.");
        assertThat(result.get(0).link()).isEqualTo("https://nnov.hse.ru/news/1149018975.html");
    }

    @Test
    void getNews_loadsAndCachesNews() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mockResponse(200, """
                <div class="post">
                  <div class="post-meta__date">
                    <div class="post-meta__day">8</div>
                    <div class="post-meta__month">мая</div>
                    <div class="post-meta__year">2026</div>
                  </div>
                  <div class="post__content">
                    <h2><a href="https://nnov.hse.ru/news/1.html">Новость</a></h2>
                    <div class="post__text"><p>Текст новости</p></div>
                  </div>
                </div>
                """);
        when(httpClient.send(any(HttpRequest.class), anyBodyHandler())).thenReturn(response);
        NewsService service = new NewsService(httpClient, CLOCK);

        List<NewsItemDto> first = service.getNews();
        List<NewsItemDto> second = service.getNews();

        assertThat(first).containsExactlyElementsOf(second);
        verify(httpClient, times(1)).send(any(HttpRequest.class), anyBodyHandler());
    }

    @Test
    void getNews_throwsExternalServiceExceptionWhenSourceFails() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mockResponse(503, "Service unavailable");
        when(httpClient.send(any(HttpRequest.class), anyBodyHandler())).thenReturn(response);
        NewsService service = new NewsService(httpClient, CLOCK);

        assertThatThrownBy(service::getNews)
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("503");
    }

    @SuppressWarnings("unchecked")
    private HttpResponse.BodyHandler<String> anyBodyHandler() {
        return any(HttpResponse.BodyHandler.class);
    }

    private HttpResponse<String> mockResponse(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }
}
