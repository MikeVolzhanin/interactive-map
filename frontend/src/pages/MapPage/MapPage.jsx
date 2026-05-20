import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchContests, fetchInterestStats, fetchNews, fetchRegionCatalog, fetchRegionStats } from '../../api/map.js'
import RussiaGeoMap from '../../components/RussiaGeoMap/RussiaGeoMap.jsx'
import {
  canShowGeoMapSwitcher,
  extractGeoFeatureNames,
  filterSchematicRegions,
} from '../../utils/regionGeoMatch.js'
import styles from './MapPage.module.css'

const MAP_MODE = {
  SCHEMATIC: 'schematic',
  GEO: 'geo',
}

const REGIONS = [
  { id: 46, code: 'КЛГ', name: 'Калининградская область', x: 1, y: 3 },
  { id: 83, code: 'СПБ', name: 'Санкт-Петербург', x: 4, y: 3 },
  { id: 53, code: 'ЛЕН', name: 'Ленинградская область', x: 3, y: 4 },
  { id: 65, code: 'ПСК', name: 'Псковская область', x: 2, y: 5 },
  { id: 59, code: 'НОВ', name: 'Новгородская область', x: 5, y: 3 },
  { id: 11, code: 'КАР', name: 'Республика Карелия', x: 4, y: 2 },
  { id: 57, code: 'МУР', name: 'Мурманская область', x: 5, y: 1 },
  { id: 41, code: 'ВОЛ', name: 'Вологодская область', x: 6, y: 3 },
  { id: 35, code: 'АРХ', name: 'Архангельская область', x: 9, y: 3 },
  { id: 86, code: 'НАО', name: 'Ненецкий автономный округ', x: 10, y: 2 },
  { id: 12, code: 'КОМ', name: 'Республика Коми', x: 10, y: 3 },
  { id: 74, code: 'ТВЕ', name: 'Тверская область', x: 4, y: 4 },
  { id: 72, code: 'СМО', name: 'Смоленская область', x: 3, y: 5 },
  { id: 82, code: 'МСК', name: 'Москва', x: 4, y: 5 },
  { id: 56, code: 'МО', name: 'Московская область', x: 5, y: 5 },
  { id: 81, code: 'ЯРО', name: 'Ярославская область', x: 5, y: 4 },
  { id: 44, code: 'ИВА', name: 'Ивановская область', x: 6, y: 4 },
  { id: 50, code: 'КОС', name: 'Костромская область', x: 7, y: 4 },
  { id: 38, code: 'БРЯ', name: 'Брянская область', x: 2, y: 6 },
  { id: 47, code: 'КАЛ', name: 'Калужская область', x: 3, y: 6 },
  { id: 76, code: 'ТУЛ', name: 'Тульская область', x: 5, y: 6 },
  { id: 67, code: 'РЯЗ', name: 'Рязанская область', x: 6, y: 6 },
  { id: 39, code: 'ВЛА', name: 'Владимирская область', x: 6, y: 5 },
  { id: 58, code: 'НИЖ', name: 'Нижегородская область', x: 7, y: 5 },
  { id: 63, code: 'ОРЛ', name: 'Орловская область', x: 4, y: 6 },
  { id: 54, code: 'ЛИП', name: 'Липецкая область', x: 5, y: 7 },
  { id: 73, code: 'ТАМ', name: 'Тамбовская область', x: 6, y: 7 },
  { id: 64, code: 'ПЕН', name: 'Пензенская область', x: 7, y: 7 },
  { id: 16, code: 'МОР', name: 'Республика Мордовия', x: 7, y: 6 },
  { id: 24, code: 'ЧУВ', name: 'Чувашская Республика', x: 8, y: 5 },
  { id: 15, code: 'МЭЛ', name: 'Республика Марий Эл', x: 8, y: 4 },
  { id: 49, code: 'КИР', name: 'Кировская область', x: 9, y: 4 },
  { id: 52, code: 'КУР', name: 'Курская область', x: 4, y: 7 },
  { id: 37, code: 'БЕЛ', name: 'Белгородская область', x: 5, y: 8 },
  { id: 42, code: 'ВОР', name: 'Воронежская область', x: 6, y: 8 },
  { id: 69, code: 'САР', name: 'Саратовская область', x: 8, y: 7 },
  { id: 78, code: 'УЛЬ', name: 'Ульяновская область', x: 8, y: 6 },
  { id: 19, code: 'ТАТ', name: 'Республика Татарстан', x: 9, y: 5 },
  { id: 21, code: 'УДМ', name: 'Удмуртская Республика', x: 10, y: 5 },
  { id: 30, code: 'ПЕР', name: 'Пермский край', x: 10, y: 4 },
  { id: 66, code: 'РОС', name: 'Ростовская область', x: 5, y: 9 },
  { id: 28, code: 'КК', name: 'Краснодарский край', x: 4, y: 9 },
  { id: 1, code: 'АДЫ', name: 'Республика Адыгея', x: 3, y: 9 },
  { id: 13, code: 'КРМ', name: 'Республика Крым', x: 2, y: 9 },
  { id: 84, code: 'СЕВ', name: 'Севастополь', x: 2, y: 10 },
  { id: 40, code: 'ВЛГ', name: 'Волгоградская область', x: 7, y: 8 },
  { id: 36, code: 'АСТ', name: 'Астраханская область', x: 7, y: 9 },
  { id: 9, code: 'КЛМ', name: 'Республика Калмыкия', x: 6, y: 9 },
  { id: 32, code: 'СТВ', name: 'Ставропольский край', x: 5, y: 10 },
  { id: 10, code: 'КЧР', name: 'Карачаево-Черкесская Республика', x: 4, y: 10 },
  { id: 8, code: 'КБР', name: 'Кабардино-Балкарская Республика', x: 4, y: 11 },
  { id: 18, code: 'СО', name: 'Республика Северная Осетия - Алания', x: 5, y: 11 },
  { id: 7, code: 'ИНГ', name: 'Республика Ингушетия', x: 6, y: 11 },
  { id: 23, code: 'ЧЕЧ', name: 'Чеченская Республика', x: 6, y: 10 },
  { id: 5, code: 'ДАГ', name: 'Республика Дагестан', x: 7, y: 10 },
  { id: 68, code: 'САМ', name: 'Самарская область', x: 9, y: 6 },
  { id: 62, code: 'ОРЕ', name: 'Оренбургская область', x: 9, y: 7 },
  { id: 3, code: 'БАШ', name: 'Республика Башкортостан', x: 10, y: 6 },
  { id: 51, code: 'КУРГ', name: 'Курганская область', x: 12, y: 5 },
  { id: 80, code: 'ЧЕЛ', name: 'Челябинская область', x: 11, y: 6 },
  { id: 71, code: 'СВР', name: 'Свердловская область', x: 11, y: 5 },
  { id: 77, code: 'ТЮМ', name: 'Тюменская область', x: 12, y: 4 },
  { id: 87, code: 'ХМАО', name: 'Ханты-Мансийский автономный округ - Югра', x: 11, y: 4 },
  { id: 88, code: 'ЯНАО', name: 'Ямало-Ненецкий автономный округ', x: 11, y: 3 },
  { id: 61, code: 'ОМС', name: 'Омская область', x: 12, y: 6 },
  { id: 60, code: 'НОС', name: 'Новосибирская область', x: 13, y: 5 },
  { id: 75, code: 'ТОМ', name: 'Томская область', x: 13, y: 4 },
  { id: 48, code: 'КЕМ', name: 'Кемеровская область - Кузбасс', x: 14, y: 4 },
  { id: 25, code: 'АЛК', name: 'Алтайский край', x: 13, y: 7 },
  { id: 2, code: 'АЛР', name: 'Республика Алтай', x: 13, y: 6 },
  { id: 22, code: 'ХАК', name: 'Республика Хакасия', x: 14, y: 5 },
  { id: 20, code: 'ТЫВ', name: 'Республика Тыва', x: 14, y: 6 },
  { id: 29, code: 'КРЯ', name: 'Красноярский край', x: 13, y: 3 },
  { id: 45, code: 'ИРК', name: 'Иркутская область', x: 15, y: 4 },
  { id: 4, code: 'БУР', name: 'Республика Бурятия', x: 15, y: 5 },
  { id: 26, code: 'ЗАБ', name: 'Забайкальский край', x: 15, y: 6 },
  { id: 17, code: 'САХ', name: 'Республика Саха (Якутия)', x: 16, y: 3 },
  { id: 34, code: 'АМУ', name: 'Амурская область', x: 16, y: 4 },
  { id: 85, code: 'ЕАО', name: 'Еврейская автономная область', x: 16, y: 5 },
  { id: 33, code: 'ХБР', name: 'Хабаровский край', x: 17, y: 4 },
  { id: 31, code: 'ПРИ', name: 'Приморский край', x: 16, y: 6 },
  { id: 55, code: 'МАГ', name: 'Магаданская область', x: 17, y: 3 },
  { id: 89, code: 'ЧУК', name: 'Чукотский автономный округ', x: 17, y: 2 },
  { id: 27, code: 'КАМ', name: 'Камчатский край', x: 18, y: 3 },
  { id: 70, code: 'САХЛ', name: 'Сахалинская область', x: 19, y: 5 },
  { id: 6, code: 'ДНР', name: 'Донецкая Народная Республика', x: 4, y: 8 },
  { id: 14, code: 'ЛНР', name: 'Луганская Народная Республика', x: 3, y: 7 },
  { id: 43, code: 'ЗАП', name: 'Запорожская область', x: 3, y: 8 },
  { id: 79, code: 'ХРС', name: 'Херсонская область', x: 2, y: 8 },
]

function getIntensity(count, maxCount) {
  if (count === 0) return 0
  if (count < maxCount * 0.18) return 1
  if (count < maxCount * 0.36) return 2
  if (count < maxCount * 0.58) return 3
  if (count < maxCount * 0.78) return 4
  return 5
}

function formatNewsDate(date) {
  if (!date) return ''
  const parsedDate = new Date(date)
  if (Number.isNaN(parsedDate.getTime())) return date
  return parsedDate.toLocaleDateString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
  })
}

export default function MapPage() {
  const navigate = useNavigate()
  const [selectedId, setSelectedId] = useState(82)
  const [mapMode, setMapMode] = useState(MAP_MODE.SCHEMATIC)
  const [regionCatalog, setRegionCatalog] = useState(null)
  const [geoFeatureNames, setGeoFeatureNames] = useState(null)
  const [regionStats, setRegionStats] = useState([])
  const [isStatsLoading, setIsStatsLoading] = useState(true)
  const [statsError, setStatsError] = useState('')
  const [news, setNews] = useState([])
  const [isNewsLoading, setIsNewsLoading] = useState(true)
  const [newsError, setNewsError] = useState('')
  const [newsSearch, setNewsSearch] = useState('')
  const [interestStats, setInterestStats] = useState([])
  const [isInterestsLoading, setIsInterestsLoading] = useState(true)
  const [interestsError, setInterestsError] = useState('')
  const [regionInterests, setRegionInterests] = useState({})
  const [hoveredRegion, setHoveredRegion] = useState(null)
  const [contests, setContests] = useState([])
  const [isContestsLoading, setIsContestsLoading] = useState(true)
  const [contestsError, setContestsError] = useState('')

  useEffect(() => {
    let ignore = false

    fetchRegionCatalog()
      .then(data => {
        if (!ignore) setRegionCatalog(Array.isArray(data) ? data : [])
      })
      .catch(() => {
        if (!ignore) {
          setRegionCatalog(REGIONS.map(region => ({ id: region.id, name: region.name })))
        }
      })

    fetch('/geo/russia.geojson')
      .then(response => response.json())
      .then(geoJson => {
        if (!ignore) setGeoFeatureNames(extractGeoFeatureNames(geoJson))
      })
      .catch(() => {
        if (!ignore) setGeoFeatureNames([])
      })

    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => {
    let ignore = false

    fetchContests()
      .then(data => {
        if (!ignore) {
          setContests(Array.isArray(data) ? data : [])
          setContestsError('')
        }
      })
      .catch(error => {
        if (!ignore) {
          setContests([])
          setContestsError(error.message || 'Не удалось загрузить конкурсы')
        }
      })
      .finally(() => {
        if (!ignore) setIsContestsLoading(false)
      })

    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => {
    let ignore = false

    fetchRegionStats()
      .then(data => {
        if (!ignore) {
          setRegionStats(Array.isArray(data) ? data : [])
          setStatsError('')
        }
      })
      .catch(error => {
        if (!ignore) {
          setRegionStats([])
          setStatsError(error.message || 'Не удалось загрузить статистику')
        }
      })
      .finally(() => {
        if (!ignore) setIsStatsLoading(false)
      })

    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => {
    let ignore = false

    fetchNews()
      .then(data => {
        if (!ignore) {
          setNews(Array.isArray(data) ? data : [])
          setNewsError('')
        }
      })
      .catch(error => {
        if (!ignore) {
          setNews([])
          setNewsError(error.message || 'Не удалось загрузить новости')
        }
      })
      .finally(() => {
        if (!ignore) setIsNewsLoading(false)
      })

    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => {
    let ignore = false

    fetchInterestStats()
      .then(data => {
        if (!ignore) {
          setInterestStats(Array.isArray(data) ? data : [])
          setInterestsError('')
        }
      })
      .catch(error => {
        if (!ignore) {
          setInterestStats([])
          setInterestsError(error.message || 'Не удалось загрузить интересы')
        }
      })
      .finally(() => {
        if (!ignore) setIsInterestsLoading(false)
      })

    return () => {
      ignore = true
    }
  }, [])

  const schematicRegionDefs = useMemo(() => {
    if (regionCatalog === null) return REGIONS
    return filterSchematicRegions(REGIONS, regionCatalog)
  }, [regionCatalog])

  const catalogIds = useMemo(() => {
    if (!regionCatalog?.length) return new Set(schematicRegionDefs.map(region => region.id))
    return new Set(regionCatalog.map(region => Number(region.id)))
  }, [regionCatalog, schematicRegionDefs])

  const canShowGeoMap = useMemo(() => {
    if (regionCatalog === null || !geoFeatureNames?.length) return false
    if (!regionCatalog.length) return false
    return canShowGeoMapSwitcher(regionCatalog, geoFeatureNames)
  }, [regionCatalog, geoFeatureNames])

  const countsByRegion = useMemo(
    () => new Map(
      regionStats.map(item => [
        Number(item.regionId),
        Number(item.applicantsCount) || 0,
      ]),
    ),
    [regionStats],
  )

  const regions = useMemo(() => {
    const withCounts = schematicRegionDefs.map(region => ({
      ...region,
      applicants: countsByRegion.get(region.id) ?? 0,
    }))
    const maxCount = Math.max(...withCounts.map(region => region.applicants), 0)
    return withCounts.map(region => ({
      ...region,
      intensity: getIntensity(region.applicants, maxCount),
    }))
  }, [countsByRegion, schematicRegionDefs])

  const geoRegions = useMemo(() => {
    const catalog = regionCatalog?.length ? regionCatalog : schematicRegionDefs.map(region => ({
      id: region.id,
      name: region.name,
    }))
    const codeById = new Map(REGIONS.map(region => [region.id, region.code]))
    const withCounts = catalog.map(catalogRegion => ({
      id: Number(catalogRegion.id),
      name: catalogRegion.name,
      code: codeById.get(Number(catalogRegion.id)) ?? String(catalogRegion.id),
      applicants: countsByRegion.get(Number(catalogRegion.id)) ?? 0,
    }))
    const maxCount = Math.max(...withCounts.map(region => region.applicants), 0)
    return withCounts.map(region => ({
      ...region,
      intensity: getIntensity(region.applicants, maxCount),
    }))
  }, [countsByRegion, regionCatalog, schematicRegionDefs])

  useEffect(() => {
    if (!regions.length) return
    if (!regions.some(region => region.id === selectedId)) {
      setSelectedId(regions[0].id)
    }
  }, [regions, selectedId])

  useEffect(() => {
    if (!canShowGeoMap && mapMode === MAP_MODE.GEO) {
      setMapMode(MAP_MODE.SCHEMATIC)
    }
  }, [canShowGeoMap, mapMode])

  const totalApplicants = regions.reduce((sum, region) => sum + region.applicants, 0)
  const selectedRegion = useMemo(() => (
    geoRegions.find(region => region.id === selectedId)
    ?? regions.find(region => region.id === selectedId)
    ?? regions[0]
  ), [geoRegions, regions, selectedId])
  const topRegions = [...regions].sort((a, b) => b.applicants - a.applicants).slice(0, 5)
  const normalizedNewsSearch = newsSearch.trim().toLowerCase()
  const filteredNews = normalizedNewsSearch
    ? news.filter(item => `${item.date} ${item.title} ${item.text}`.toLowerCase().includes(normalizedNewsSearch))
    : news
  const maxInterestCount = Math.max(...interestStats.map(item => Number(item.applicantsCount) || 0), 0)
  const totalInterestCount = interestStats.reduce((sum, item) => sum + (Number(item.applicantsCount) || 0), 0)
  const hoveredRegionInterests = hoveredRegion ? regionInterests[hoveredRegion.id] : null
  const isRegionTooltipLoading = hoveredRegion && (!hoveredRegionInterests || hoveredRegionInterests.loading)

  function handleFormRedirect() {
    navigate('/login')
  }

  function handleRegionHover(region, event) {
    const rect = event.currentTarget.getBoundingClientRect()
    const halfTooltipWidth = Math.min(140, (window.innerWidth - 24) / 2)
    const tooltipLeft = Math.min(
      Math.max(rect.left + rect.width / 2, halfTooltipWidth + 12),
      window.innerWidth - halfTooltipWidth - 12,
    )

    setHoveredRegion({
      ...region,
      left: tooltipLeft,
      top: rect.top,
      placement: rect.top < 170 ? 'below' : 'above',
    })

    const cached = regionInterests[region.id]
    if (cached?.items || cached?.loading) return

    setRegionInterests(previous => ({
      ...previous,
      [region.id]: { items: [], loading: true, error: '' },
    }))

    fetchInterestStats(region.id)
      .then(data => {
        setRegionInterests(previous => ({
          ...previous,
          [region.id]: {
            items: Array.isArray(data) ? data.slice(0, 3) : [],
            loading: false,
            error: '',
          },
        }))
      })
      .catch(error => {
        setRegionInterests(previous => ({
          ...previous,
          [region.id]: {
            items: [],
            loading: false,
            error: error.message || 'Не удалось загрузить интересы',
          },
        }))
      })
  }

  function clearHoveredRegion() {
    setHoveredRegion(null)
  }

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <img src="/hse-logo-header.svg" alt="НИУ ВШЭ" className={styles.headerLogo} />
        <h1 className={styles.headerTitle}>Интерактивная карта</h1>
        <button className={styles.formButton} onClick={handleFormRedirect}>Заполнить форму</button>
      </header>

      <main className={styles.main}>
        <section className={styles.mapSection}>
          <div className={styles.summaryBar}>
            <div>
              <p className={styles.kicker}>География абитуриентов</p>
              <h2 className={styles.mapTitle}>Регионы Российской Федерации</h2>
            </div>
            <div className={styles.stats}>
              <div className={styles.statItem}>
                <span className={styles.statValue}>{totalApplicants.toLocaleString('ru-RU')}</span>
                <span className={styles.statLabel}>абитуриентов</span>
              </div>
              <div className={styles.statItem}>
                <span className={styles.statValue}>{regions.length}</span>
                <span className={styles.statLabel}>регионов</span>
              </div>
            </div>
            {canShowGeoMap && (
              <div className={styles.mapModeSwitch} role="group" aria-label="Тип карты">
                <button
                  type="button"
                  className={`${styles.mapModeBtn} ${mapMode === MAP_MODE.SCHEMATIC ? styles.mapModeBtnActive : ''}`}
                  onClick={() => setMapMode(MAP_MODE.SCHEMATIC)}
                >
                  Схема
                </button>
                <button
                  type="button"
                  className={`${styles.mapModeBtn} ${mapMode === MAP_MODE.GEO ? styles.mapModeBtnActive : ''}`}
                  onClick={() => setMapMode(MAP_MODE.GEO)}
                >
                  Географическая
                </button>
              </div>
            )}
          </div>

          <div
            className={`${styles.mapPanel} ${mapMode === MAP_MODE.GEO ? styles.mapPanelGeo : ''}`}
          >
            {(isStatsLoading || statsError) && (
              <div className={`${styles.mapStatus} ${statsError ? styles.mapStatusError : ''}`}>
                {statsError || 'Загружаем статистику по регионам...'}
              </div>
            )}
            {mapMode === MAP_MODE.SCHEMATIC ? (
              <div className={styles.gridMap} aria-label="Схематическая карта регионов Российской Федерации">
                {regions.map(region => (
                  <button
                    key={region.id}
                    type="button"
                    className={`${styles.regionCell} ${styles[`intensity${region.intensity}`]} ${selectedId === region.id ? styles.regionSelected : ''}`}
                    style={{ gridColumn: region.x, gridRow: region.y }}
                    onClick={() => setSelectedId(region.id)}
                    onMouseEnter={event => handleRegionHover(region, event)}
                    onMouseLeave={clearHoveredRegion}
                    onFocus={event => handleRegionHover(region, event)}
                    onBlur={clearHoveredRegion}
                    aria-label={`${region.name}: ${region.applicants} абитуриентов`}
                  >
                    {region.code}
                  </button>
                ))}
              </div>
            ) : (
              <RussiaGeoMap
                regions={geoRegions}
                catalogIds={catalogIds}
                selectedId={selectedId}
                onSelect={setSelectedId}
                onRegionHover={handleRegionHover}
                onRegionLeave={clearHoveredRegion}
              />
            )}
          </div>

          <div className={styles.mapFooter}>
            <div className={styles.selectedRegion}>
              <span className={styles.selectedName}>{selectedRegion.name}</span>
              <span className={styles.selectedCount}>{selectedRegion.applicants} абитуриентов</span>
            </div>
            <div className={styles.legend} aria-label="Легенда интенсивности">
              <span>Меньше</span>
              {[1, 2, 3, 4, 5].map(level => (
                <span key={level} className={`${styles.legendSwatch} ${styles[`intensity${level}`]}`} />
              ))}
              <span>Больше</span>
            </div>
          </div>

          <div className={styles.topList}>
            {topRegions.map((region, index) => (
              <button
                key={region.id}
                className={styles.topItem}
                onClick={() => setSelectedId(region.id)}
              >
                <span className={styles.topRank}>{index + 1}</span>
                <span className={styles.topName}>{region.name}</span>
                <span className={styles.topCount}>{region.applicants}</span>
              </button>
            ))}
          </div>

          <section className={styles.interestsPanel}>
            <div className={styles.interestsHeader}>
              <div>
                <p className={styles.kicker}>Интересы абитуриентов</p>
                <h2 className={styles.interestsTitle}>Горизонтальный рейтинг</h2>
              </div>
              <div className={styles.interestsTotal}>
                <span>{totalInterestCount.toLocaleString('ru-RU')}</span>
                <span>выборов</span>
              </div>
            </div>
            <div className={styles.interestsList}>
              {(isInterestsLoading || interestsError) && (
                <div className={`${styles.interestsStatus} ${interestsError ? styles.interestsStatusError : ''}`}>
                  {interestsError || 'Загружаем интересы...'}
                </div>
              )}
              {!isInterestsLoading && !interestsError && interestStats.length === 0 && (
                <div className={styles.interestsStatus}>Нет данных по интересам</div>
              )}
              {interestStats.map((item, index) => {
                const count = Number(item.applicantsCount) || 0
                const percent = totalInterestCount > 0 ? Math.round((count / totalInterestCount) * 100) : 0
                const width = maxInterestCount > 0 ? Math.round((count / maxInterestCount) * 100) : 0
                return (
                  <div key={item.interestId} className={styles.interestRow}>
                    <div className={styles.interestMeta}>
                      <span className={styles.interestRank}>{index + 1}</span>
                      <span className={styles.interestName}>{item.interestName}</span>
                      <span className={styles.interestCount}>{count.toLocaleString('ru-RU')}</span>
                    </div>
                    <div className={styles.interestTrack} aria-hidden="true">
                      <span className={styles.interestBar} style={{ width: `${width}%` }} />
                    </div>
                    <span className={styles.interestPercent}>{percent}%</span>
                  </div>
                )
              })}
            </div>
          </section>
        </section>

        <aside className={styles.sidebar}>
          <section className={`${styles.sidePanel} ${styles.newsPanel}`}>
            <div className={styles.sideHeader}>
              <h2 className={styles.sideTitle}>Новости</h2>
              <span className={styles.sideBadge}>{filteredNews.length}</span>
            </div>
            <input
              className={styles.newsSearch}
              type="search"
              value={newsSearch}
              onChange={event => setNewsSearch(event.target.value)}
              placeholder="Поиск по новостям"
              aria-label="Поиск по новостям"
            />
            <div className={styles.newsList}>
              {(isNewsLoading || newsError) && (
                <div className={`${styles.newsStatus} ${newsError ? styles.newsStatusError : ''}`}>
                  {newsError || 'Загружаем новости...'}
                </div>
              )}
              {!isNewsLoading && !newsError && filteredNews.length === 0 && (
                <div className={styles.newsStatus}>Новости не найдены</div>
              )}
              {filteredNews.map(item => (
                <a
                  key={item.id}
                  className={styles.newsItem}
                  href={item.link}
                  target="_blank"
                  rel="noreferrer"
                >
                  <time className={styles.newsDate} dateTime={item.date}>{formatNewsDate(item.date)}</time>
                  <h3 className={styles.newsTitle}>{item.title}</h3>
                  <p className={styles.newsText}>{item.text}</p>
                </a>
              ))}
            </div>
          </section>

          <section className={styles.sidePanel}>
            <div className={styles.sideHeader}>
              <h2 className={styles.sideTitle}>Конкурсы</h2>
              <span className={styles.sideBadge}>{contests.length}</span>
            </div>
            {(isContestsLoading || contestsError) && (
              <div className={`${styles.newsStatus} ${contestsError ? styles.newsStatusError : ''}`}>
                {contestsError || 'Загружаем конкурсы...'}
              </div>
            )}
            {!isContestsLoading && !contestsError && contests.length === 0 && (
              <div className={styles.newsStatus}>Конкурсы пока не добавлены</div>
            )}
            <ul className={styles.contestList}>
              {contests.map(item => (
                <li key={item.id} className={styles.contestItem}>
                  <span className={styles.contestTitle}>{item.title}</span>
                  <span className={styles.contestMeta}>{item.status}</span>
                  <span className={styles.contestDeadline}>{item.deadline}</span>
                </li>
              ))}
            </ul>
          </section>
        </aside>
      </main>

      {hoveredRegion && (
        <div
          className={`${styles.regionTooltip} ${hoveredRegion.placement === 'below' ? styles.regionTooltipBelow : ''}`}
          style={{ left: hoveredRegion.left, top: hoveredRegion.top }}
          role="status"
        >
          <div className={styles.tooltipTitle}>{hoveredRegion.name}</div>
          <div className={styles.tooltipCount}>{hoveredRegion.applicants.toLocaleString('ru-RU')} абитуриентов</div>
          <div className={styles.tooltipSubtitle}>Топ интересов</div>
          {isRegionTooltipLoading && (
            <div className={styles.tooltipMuted}>Загружаем...</div>
          )}
          {!isRegionTooltipLoading && hoveredRegionInterests?.error && (
            <div className={styles.tooltipError}>{hoveredRegionInterests.error}</div>
          )}
          {!isRegionTooltipLoading && !hoveredRegionInterests?.error && (
            hoveredRegionInterests?.items?.length ? (
              <ol className={styles.tooltipList}>
                {hoveredRegionInterests.items.map(item => {
                  const count = Number(item.applicantsCount) || 0
                  return (
                    <li key={item.interestId}>
                      <span>{item.interestName}</span>
                      <strong>{count.toLocaleString('ru-RU')}</strong>
                    </li>
                  )
                })}
              </ol>
            ) : (
              <div className={styles.tooltipMuted}>Нет данных</div>
            )
          )}
        </div>
      )}
    </div>
  )
}
