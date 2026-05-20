#!/usr/bin/env python3
"""Генерирует пример XLSX для импорта конкурсов (название, статус, дата окончания + доп. столбцы)."""
from datetime import date
from pathlib import Path

from openpyxl import Workbook

wb = Workbook()
ws = wb.active
ws.title = "Contests"
ws.append(
    [
        "название",
        "статус",
        "дата окончания",
        "Описание",
        "Формат",
    ]
)
ws.append(
    [
        "Олимпиада школьников",
        "Прием заявок",
        date(2026, 5, 20),
        "Очный этап, математика и информатика",
        "Очно",
    ]
)
ws.append(
    [
        "Конкурс проектных работ",
        "Идет отбор",
        date(2026, 5, 28),
        "Командный проект по анализу данных",
        "Смешанный",
    ]
)
ws.append(
    [
        "Региональный трек",
        "Скоро старт",
        date(2026, 6, 1),
        "Отборочный этап",
        "Дистанционно",
    ]
)
ws.append(
    [
        "Портфолио абитуриента",
        "Открыт",
        date(2026, 6, 15),
        "Загрузка материалов",
        "Онлайн",
    ]
)

root = Path(__file__).resolve().parents[2]
out = root / "frontend" / "public" / "samples" / "contests-import-sample.xlsx"
out.parent.mkdir(parents=True, exist_ok=True)
wb.save(out)
print("written:", out)
