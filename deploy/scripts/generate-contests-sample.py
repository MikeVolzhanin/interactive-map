#!/usr/bin/env python3
from pathlib import Path
from openpyxl import Workbook

wb = Workbook()
ws = wb.active
ws.title = "Contests"
ws.append(["email", "конкурсы", "Описание", "Статус", "Срок подачи"])
ws.append([
    "aabaurina@edu.hse.ru",
    "Олимпиада школьников ВШЭ",
    "Очный этап, математика и информатика",
    "Участник",
    "2026-03-15",
])
ws.append([
    "aabaurina@edu.hse.ru",
    "Конкурс проектных работ",
    "Командный проект по анализу данных",
    "Заявка подана",
    "2026-04-01",
])
ws.append([
    "aabaurina@edu.hse.ru",
    "Региональный трек",
    "Отборочный этап в Нижнем Новгороде",
    "Участник",
    "2026-02-28",
])
ws.append([
    "neznakomyy.abiturient@example.com",
    "Конкурс проектных работ",
    "Индивидуальная заявка",
    "На рассмотрении",
    "2026-04-01",
])
ws.append([
    "drugoy.chuzhak@mail.ru",
    "Региональный трек",
    "Дистанционный формат",
    "Участник",
    "2026-02-28",
])

root = Path(__file__).resolve().parents[2]
out = root / "frontend" / "public" / "samples" / "contests-import-sample.xlsx"
out.parent.mkdir(parents=True, exist_ok=True)
wb.save(out)
print("written:", out)
