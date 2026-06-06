/** Нормализация для сопоставления подписей GeoJSON и справочника. */
export function normalizeRegionLabel(value) {
  return value
    .toLowerCase()
    .replace(/ё/g, 'е')
    .replace(/—/g, '-')
    .replace(/\s+/g, ' ')
    .trim()
}

/** Явные соответствия коротких имён из GeoJSON → полное имя в БД. */
const GEO_TO_DB = {
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
  'кемеровская область': 'Кемеровская область — Кузбасс',
  'донецкая народная республика': 'Донецкая Народная Республика',
  'луганская народная республика': 'Луганская Народная Республика',
  'запорожская область': 'Запорожская область',
  'херсонская область': 'Херсонская область',
  'республика крым': 'Республика Крым',
  'карачаево-черкесская республика': 'Карачаево-Черкесская Республика',
  'удмуртская республика': 'Удмуртская Республика',
  'чеченская республика': 'Чеченская Республика',
  крым: 'Республика Крым',
  севастополь: 'Севастополь',
  москва: 'Москва',
  'санкт-петербург': 'Санкт-Петербург',
}

function stripCommonPrefixes(value) {
  return value
    .replace(/^республика\s+/i, '')
    .replace(/^чувашская\s+республика$/i, 'чувашия')
    .trim()
}

/**
 * Находит id региона из справочника по имени из GeoJSON.
 * @returns {number|undefined}
 */
export function findRegionIdByGeoName(geoName, regions) {
  if (!regions?.length) return undefined

  const normalizedGeo = normalizeRegionLabel(geoName)
  const alias = GEO_TO_DB[normalizedGeo]
  if (alias) {
    const normalizedAlias = normalizeRegionLabel(alias)
    const hit = regions.find(region => normalizeRegionLabel(region.name) === normalizedAlias)
    if (hit) return Number(hit.id)
  }

  for (const region of regions) {
    if (normalizeRegionLabel(region.name) === normalizedGeo) return Number(region.id)
  }

  const strippedGeo = stripCommonPrefixes(normalizedGeo)
  let best
  let bestScore = 0

  for (const region of regions) {
    const normalizedRegion = normalizeRegionLabel(region.name)
    const strippedRegion = stripCommonPrefixes(normalizedRegion)

    let score = 0
    if (normalizedRegion === normalizedGeo) score = 100
    else if (strippedRegion === strippedGeo) score = 95
    else if (normalizedRegion.includes(normalizedGeo) || normalizedGeo.includes(normalizedRegion)) score = 85
    else if (normalizedRegion.includes(strippedGeo) || strippedGeo.includes(strippedRegion)) score = 75
    else if (normalizedGeo.length > 4 && (normalizedRegion.includes(strippedGeo) || strippedRegion.includes(strippedGeo))) {
      score = 65
    }

    if (score > bestScore) {
      bestScore = score
      best = region
    }
  }

  if (best && bestScore >= 65) return Number(best.id)
  return undefined
}

export function extractGeoFeatureNames(geoJson) {
  if (!geoJson?.features) return []
  return geoJson.features
    .map(feature => String(feature.properties?.name ?? ''))
    .filter(Boolean)
}

/**
 * Регионы без полигона в GeoJSON (если появятся в справочнике) — не блокируют переключатель,
 * пока список пуст. Неизвестные немаппируемые регионы скрывают географическую карту.
 */
const KNOWN_SCHEMATIC_ONLY_REGIONS = new Set([])

export function isKnownSchematicOnlyRegion(regionName) {
  return KNOWN_SCHEMATIC_ONLY_REGIONS.has(normalizeRegionLabel(regionName))
}

/** Регионы справочника, для которых нет соответствующего полигона в GeoJSON. */
export function getUnmappedCatalogRegions(catalog, geoNames) {
  if (!catalog?.length || !geoNames?.length) return catalog ?? []

  const regionList = catalog.map(region => ({
    id: Number(region.id),
    name: region.name,
  }))

  return catalog.filter(catalogRegion =>
    !geoNames.some(geoName => {
      const matchedId = findRegionIdByGeoName(geoName, regionList)
      return matchedId !== undefined && matchedId === Number(catalogRegion.id)
    }),
  )
}

/**
 * Показывать переключатель, если GeoJSON загружен, есть хотя бы один маппируемый регион
 * и все «немаппируемые» — только известные схематические (ДНР, ЛНР, Крым и т.д.).
 */
export function canShowGeoMapSwitcher(catalog, geoNames) {
  if (!catalog?.length || !geoNames?.length) return false

  const unmapped = getUnmappedCatalogRegions(catalog, geoNames)
  const mappableCount = catalog.length - unmapped.length
  if (mappableCount === 0) return false

  const unexpectedUnmapped = unmapped.filter(region => !isKnownSchematicOnlyRegion(region.name))
  return unexpectedUnmapped.length === 0
}

/** @deprecated используйте canShowGeoMapSwitcher */
export function isCatalogGeoCompatible(catalog, geoNames) {
  return canShowGeoMapSwitcher(catalog, geoNames)
}

export function filterSchematicRegions(schematicRegions, catalog) {
  if (!catalog?.length) return schematicRegions
  const catalogIds = new Set(catalog.map(region => Number(region.id)))
  return schematicRegions.filter(region => catalogIds.has(region.id))
}
