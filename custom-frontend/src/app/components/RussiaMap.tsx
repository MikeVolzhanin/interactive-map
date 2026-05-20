import { useMemo, useState } from 'react';
import { ComposableMap, Geographies, Geography, ZoomableGroup } from 'react-simple-maps';
import { findRegionIdByGeoName } from '../utils/regionGeoMatch';

const GEO_URL = '/geo/russia.geojson';

interface RegionData {
  regionId: string;
  count: number;
}

interface RussiaMapProps {
  data: RegionData[];
  regions: { id: string; name: string; code: string }[];
}

export function RussiaMap({ data, regions }: RussiaMapProps) {
  const [tooltip, setTooltip] = useState<{
    x: number;
    y: number;
    name: string;
    count: number;
  } | null>(null);

  const countByRegionId = useMemo(() => {
    const m = new Map<string, number>();
    for (const d of data) {
      m.set(String(d.regionId), d.count);
    }
    return m;
  }, [data]);

  const regionList = useMemo(
    () => regions.map((r) => ({ id: Number(r.id), name: r.name })),
    [regions],
  );

  const getRegionColor = (count: number): string => {
    if (count === 0) return 'var(--map-0)';
    if (count < 200) return 'var(--map-1)';
    if (count < 400) return 'var(--map-2)';
    if (count < 700) return 'var(--map-3)';
    return 'var(--map-4)';
  };

  return (
    <div className="relative w-full">
      <ComposableMap
        width={1000}
        height={520}
        projection="geoEqualEarth"
        projectionConfig={{
          scale: 1600,
          center: [100, 62],
        }}
        className="max-h-[min(70vh,560px)] w-full [&_svg]:h-auto"
      >
        <ZoomableGroup center={[100, 62]} zoom={0.85} minZoom={0.4} maxZoom={12}>
          <Geographies geography={GEO_URL}>
            {({ geographies }) =>
              geographies.map((geo) => {
                const geoName = String(geo.properties?.name ?? '');
                const rid = findRegionIdByGeoName(geoName, regionList);
                const count = rid !== undefined ? (countByRegionId.get(rid) ?? 0) : 0;
                const fill = getRegionColor(count);
                const isZero = count === 0;

                return (
                  <Geography
                    key={geo.rsmKey}
                    geography={geo}
                    onMouseEnter={(e) => {
                      setTooltip({
                        x: e.clientX,
                        y: e.clientY,
                        name: geoName,
                        count,
                      });
                    }}
                    onMouseMove={(e) => {
                      setTooltip((prev) =>
                        prev
                          ? { ...prev, x: e.clientX, y: e.clientY }
                          : {
                              x: e.clientX,
                              y: e.clientY,
                              name: geoName,
                              count,
                            },
                      );
                    }}
                    onMouseLeave={() => setTooltip(null)}
                    style={{
                      default: {
                        fill,
                        stroke: isZero ? 'var(--border)' : 'var(--surface)',
                        strokeWidth: isZero ? 0.6 : 0.35,
                        outline: 'none',
                      },
                      hover: {
                        fill,
                        stroke: 'var(--primary)',
                        strokeWidth: 1.2,
                        outline: 'none',
                        filter: 'brightness(1.05)',
                      },
                      pressed: {
                        fill,
                        outline: 'none',
                      },
                    }}
                  />
                );
              })
            }
          </Geographies>
        </ZoomableGroup>
      </ComposableMap>

      {regions.length > 0 && (
        <div className="mt-4 border-t pt-4" style={{ borderColor: 'var(--border)' }}>
          <p className="mb-2 text-center text-xs font-medium" style={{ color: 'var(--text-secondary)' }}>
            Регионы из справочника (подписи к данным)
          </p>
          <div className="max-h-40 overflow-y-auto">
            <div className="flex flex-wrap justify-center gap-2">
              {regions.map((r) => (
                <span
                  key={r.id}
                  className="rounded border px-2 py-1 text-xs"
                  style={{
                    borderColor: 'var(--border)',
                    backgroundColor: 'var(--surface)',
                    color: 'var(--text-primary)',
                  }}
                >
                  {r.name}
                </span>
              ))}
            </div>
          </div>
        </div>
      )}

      {tooltip && (
        <div
          className="fixed z-50 max-w-xs rounded px-3 py-2 text-sm shadow-lg pointer-events-none"
          style={{
            backgroundColor: 'var(--surface)',
            border: '1px solid var(--border)',
            left: tooltip.x + 12,
            top: tooltip.y + 12,
            color: 'var(--text-primary)',
          }}
        >
          <div className="font-medium">{tooltip.name}</div>
          <div style={{ color: 'var(--text-secondary)' }}>
            Регистраций: {tooltip.count}
            {tooltip.count === 0 && <span className="ml-1 text-xs">(нет данных)</span>}
          </div>
        </div>
      )}
    </div>
  );
}
