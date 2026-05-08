package ru.volzhanin.applicantsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.volzhanin.applicantsservice.dto.news.NewsItemDto;
import ru.volzhanin.applicantsservice.service.NewsService;

import java.util.List;

@Tag(name = "Новости", description = "Новости для страницы карты")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {
    private final NewsService newsService;

    @Operation(summary = "Список новостей для карты")
    @ApiResponse(responseCode = "200", description = "Новости, отсортированные от новых к старым",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = NewsItemDto.class))))
    @GetMapping
    public List<NewsItemDto> getNews() {
        return newsService.getNews();
    }
}
