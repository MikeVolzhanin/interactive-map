package ru.volzhanin.applicantsservice.dto.news;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Новость для страницы карты")
public record NewsItemDto(
        @Schema(description = "Идентификатор новости", example = "1147018975")
        Long id,
        @Schema(description = "Дата публикации", example = "2026-05-08")
        LocalDate date,
        @Schema(description = "Заголовок новости", example = "Открыта регистрация на летние консультации")
        String title,
        @Schema(description = "Краткий текст новости",
                example = "Абитуриенты могут выбрать направление и формат участия.")
        String text,
        @Schema(description = "Ссылка на новость", example = "https://nnov.hse.ru/news/1147018975.html")
        String link
) {
}
