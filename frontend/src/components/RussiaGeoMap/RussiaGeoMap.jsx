import { useMemo, useState } from 'react'
import { ComposableMap, Geographies, Geography, ZoomableGroup } from 'react-simple-maps'
import { findRegionIdByGeoName } from '../../utils/regionGeoMatch.js'
import styles from './RussiaGeoMap.module.css'

const GEO_URL = '/geo/russia.geojson'

const MAP_CENTER = [100, 62]
const DEFAULT_ZOOM = 0.85
const MIN_ZOOM = 0.4
const MAX_ZOOM = 12

const INTENSITY_FILL = {
  0: '#edf0f5',
  1: '#dce7f8',
  2: '#b8cdef',
  3: '#7fa5df',
  4: '#3f73bd',
  5: '#0f2d69',
}

/** Вертикальный ползунок: вверх — крупнее, вниз — мельче. */
function zoomToSliderValue(zoom) {
  const ratio = (zoom - MIN_ZOOM) / (MAX_ZOOM - MIN_ZOOM)
  return Math.round((1 - ratio) * 100)
}

function sliderValueToZoom(value) {
  const ratio = Number(value) / 100
  return MAX_ZOOM - ratio * (MAX_ZOOM - MIN_ZOOM)
}

export default function RussiaGeoMap({
  regions,
  catalogIds,
  selectedId,
  onSelect,
  onRegionHover,
  onRegionLeave,
}) {
  const [zoom, setZoom] = useState(DEFAULT_ZOOM)
  const sliderValue = zoomToSliderValue(zoom)

  const regionsById = useMemo(
    () => new Map(regions.map(region => [region.id, region])),
    [regions],
  )

  const regionList = useMemo(
    () => regions.map(region => ({ id: region.id, name: region.name })),
    [regions],
  )

  function handleSliderChange(event) {
    setZoom(sliderValueToZoom(event.target.value))
  }

  function handleMoveEnd(position) {
    if (typeof position?.zoom === 'number') {
      setZoom(Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, position.zoom)))
    }
  }

  function nudgeZoom(delta) {
    setZoom(current => Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, current + delta)))
  }

  return (
    <div className={styles.wrap} aria-label="Географическая карта регионов Российской Федерации">
      <div className={styles.mapArea}>
        <ComposableMap
          width={1000}
          height={520}
          projection="geoEqualEarth"
          projectionConfig={{ scale: 1600, center: MAP_CENTER }}
          className={styles.composableMap}
        >
          <ZoomableGroup
            center={MAP_CENTER}
            zoom={zoom}
            minZoom={MIN_ZOOM}
            maxZoom={MAX_ZOOM}
            onMoveEnd={handleMoveEnd}
          >
            <Geographies geography={GEO_URL}>
              {({ geographies }) =>
                geographies.map(geo => {
                  const geoName = String(geo.properties?.name ?? '')
                  const regionId = findRegionIdByGeoName(geoName, regionList)
                  if (regionId === undefined || !catalogIds.has(regionId)) {
                    return null
                  }

                  const region = regionsById.get(regionId)
                  if (!region) return null

                  const fill = INTENSITY_FILL[region.intensity] ?? INTENSITY_FILL[0]
                  const isSelected = selectedId === regionId

                  return (
                    <Geography
                      key={geo.rsmKey}
                      geography={geo}
                      onClick={() => onSelect(regionId)}
                      onMouseEnter={event => onRegionHover(region, event)}
                      onMouseLeave={onRegionLeave}
                      onFocus={event => onRegionHover(region, event)}
                      onBlur={onRegionLeave}
                      style={{
                        default: {
                          fill,
                          stroke: isSelected ? '#0f2d69' : '#ffffff',
                          strokeWidth: isSelected ? 1.4 : 0.35,
                          outline: 'none',
                          cursor: 'pointer',
                        },
                        hover: {
                          fill,
                          stroke: '#0f2d69',
                          strokeWidth: 1.2,
                          outline: 'none',
                          filter: 'brightness(1.05)',
                          cursor: 'pointer',
                        },
                        pressed: {
                          fill,
                          outline: 'none',
                          cursor: 'pointer',
                        },
                      }}
                    />
                  )
                })
              }
            </Geographies>
          </ZoomableGroup>
        </ComposableMap>
      </div>

      <div className={styles.zoomControl} aria-label="Масштаб карты">
        <button
          type="button"
          className={styles.zoomBtn}
          onClick={() => nudgeZoom(0.35)}
          disabled={zoom >= MAX_ZOOM - 0.01}
          aria-label="Увеличить"
        >
          +
        </button>
        <div className={styles.zoomTrack}>
          <input
            type="range"
            className={styles.zoomSlider}
            min={0}
            max={100}
            step={1}
            value={sliderValue}
            onChange={handleSliderChange}
            aria-valuemin={MIN_ZOOM}
            aria-valuemax={MAX_ZOOM}
            aria-valuenow={Number(zoom.toFixed(2))}
            aria-label="Масштаб"
          />
        </div>
        <button
          type="button"
          className={styles.zoomBtn}
          onClick={() => nudgeZoom(-0.35)}
          disabled={zoom <= MIN_ZOOM + 0.01}
          aria-label="Уменьшить"
        >
          −
        </button>
      </div>
    </div>
  )
}
