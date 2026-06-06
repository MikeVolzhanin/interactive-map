import type { EducationLevel, Interest, Region } from '../api/types';

/** Используются только если запрос к API справочника не удался (демо UI). */
export const STUB_REGIONS: Region[] = [
  { id: 1, name: 'Москва (демо)' },
  { id: 2, name: 'Санкт-Петербург (демо)' },
  { id: 3, name: 'Новосибирская область (демо)' },
];

export const STUB_EDUCATION_LEVELS: EducationLevel[] = [
  { id: 1, level: 'Бакалавриат (демо)' },
  { id: 2, level: 'Магистратура (демо)' },
];

export const STUB_INTERESTS: Interest[] = [
  { id: 101, name: 'Экономика и финансы' },
  { id: 102, name: 'IT и программирование' },
  { id: 103, name: 'Социология и политика' },
  { id: 104, name: 'Дизайн и медиа' },
];

/** Укороченный список для демо при ошибке API; на настоящей карте полигоны берутся из GeoJSON. */
export const DEMO_MAP_REGIONS: Region[] = [
  { id: 1, name: 'Москва' },
  { id: 2, name: 'Санкт-Петербург' },
  { id: 3, name: 'Московская область' },
  { id: 4, name: 'Краснодарский край' },
  { id: 5, name: 'Свердловская область' },
  { id: 6, name: 'Республика Татарстан' },
  { id: 7, name: 'Нижегородская область' },
  { id: 8, name: 'Новосибирская область' },
  { id: 9, name: 'Ростовская область' },
  { id: 10, name: 'Челябинская область' },
  { id: 11, name: 'Самарская область' },
  { id: 12, name: 'Республика Башкортостан' },
  { id: 13, name: 'Красноярский край' },
  { id: 14, name: 'Пермский край' },
  { id: 15, name: 'Волгоградская область' },
];

/** Условные числа 0…1000 для демонстрации (стабильные между перерисовками). */
export function buildDemoMapCounts(): { regionId: string; count: number }[] {
  return DEMO_MAP_REGIONS.map((r) => {
    const n = ((r.id * 7919) ^ 42) % 1001;
    return { regionId: String(r.id), count: n };
  });
}
