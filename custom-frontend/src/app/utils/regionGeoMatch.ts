import type { Region } from '../api/types';

/** Нормализация для сопоставления подписей GeoJSON и справочника бэкенда. */
export function normalizeRegionLabel(s: string): string {
  return s
    .toLowerCase()
    .replace(/ё/g, 'е')
    .replace(/—/g, '-')
    .replace(/\s+/g, ' ')
    .trim();
}

/** Явные соответствия коротких имён из GeoJSON → полное имя в БД. */
const GEO_TO_DB: Record<string, string> = {
  адыгея: 'Республика Адыгея',
  алтай: 'Республика Алтай',
  башкортостан: 'Республика Башкортостан',
  бурятия: 'Республика Бурятия',
  дагестан: 'Республика Дагестан',
  ингушетия: 'Республика Ингушетия',
  татарстан: 'Республика Татарстан',
  тыва: 'Республика Тыва',
  'марий эл': 'Республика Марий Эл',
  чувашия: 'Чувашская Республика',
  карелия: 'Республика Карелия',
  коми: 'Республика Коми',
  калмыкия: 'Республика Калмыкия',
  'северная осетия - алания': 'Республика Северная Осетия — Алания',
  'карачаево-черкесская республика': 'Карачаево-Черкесская Республика',
  'удмуртская республика': 'Удмуртская Республика',
  'чеченская республика': 'Чеченская Республика',
  крым: 'Республика Крым',
  севастополь: 'Севастополь',
  москва: 'Москва',
  'санкт-петербург': 'Санкт-Петербург',
};

function stripCommonPrefixes(n: string): string {
  return n
    .replace(/^республика\s+/i, '')
    .replace(/^чувашская\s+республика$/i, 'чувашия')
    .trim();
}

/**
 * Находит id региона из справочника по имени из GeoJSON.
 */
export function findRegionIdByGeoName(geoName: string, regions: Region[]): string | undefined {
  if (!regions.length) return undefined;

  const g0 = normalizeRegionLabel(geoName);
  const alias = GEO_TO_DB[g0];
  if (alias) {
    const hit = regions.find((r) => r.name === alias);
    if (hit) return String(hit.id);
  }

  for (const r of regions) {
    if (normalizeRegionLabel(r.name) === g0) return String(r.id);
  }

  const g = stripCommonPrefixes(g0);

  let best: Region | undefined;
  let bestScore = 0;

  for (const r of regions) {
    const rn = normalizeRegionLabel(r.name);
    const rs = stripCommonPrefixes(rn);

    let score = 0;
    if (rn === g0) score = 100;
    else if (rs === g) score = 95;
    else if (rn.includes(g0) || g0.includes(rn)) score = 85;
    else if (rn.includes(g) || g.includes(rs)) score = 75;
    else if (g0.length > 4 && (rn.includes(g0) || rs.includes(g))) score = 65;

    if (score > bestScore) {
      bestScore = score;
      best = r;
    }
  }

  if (best && bestScore >= 65) return String(best.id);
  return undefined;
}
