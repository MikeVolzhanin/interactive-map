import { useState } from 'react';

interface RegionData {
  regionId: string;
  count: number;
}

interface RussiaMapProps {
  data: RegionData[];
  regions: { id: string; name: string; code: string }[];
}

export function RussiaMap({ data, regions }: RussiaMapProps) {
  const [hoveredRegion, setHoveredRegion] = useState<string | null>(null);
  const [tooltipPos, setTooltipPos] = useState({ x: 0, y: 0 });

  const getRegionColor = (count: number): string => {
    if (count === 0) return 'var(--map-0)';
    if (count < 200) return 'var(--map-1)';
    if (count < 400) return 'var(--map-2)';
    if (count < 700) return 'var(--map-3)';
    return 'var(--map-4)';
  };

  const getRegionData = (regionId: string) => {
    return data.find(d => d.regionId === regionId);
  };

  const getRegionInfo = (regionId: string) => {
    const region = regions.find(r => r.id === regionId);
    const regionData = getRegionData(regionId);
    return {
      name: region?.name || 'Неизвестный регион',
      count: regionData?.count || 0,
    };
  };

  const handleMouseMove = (e: React.MouseEvent, regionId: string) => {
    setHoveredRegion(regionId);
    setTooltipPos({ x: e.clientX, y: e.clientY });
  };

  const handleMouseLeave = () => {
    setHoveredRegion(null);
  };

  // Simplified SVG map of Russia with major regions
  // Each region is represented by a simplified path
  const regionPaths = [
    { id: '1', name: 'Москва', d: 'M 380 180 L 390 175 L 400 180 L 395 190 L 385 190 Z' },
    { id: '2', name: 'Санкт-Петербург', d: 'M 350 140 L 365 135 L 370 145 L 360 155 L 345 150 Z' },
    { id: '3', name: 'Московская область', d: 'M 370 170 L 410 165 L 415 195 L 390 200 L 375 195 Z' },
    { id: '4', name: 'Краснодарский край', d: 'M 420 280 L 460 275 L 470 295 L 450 305 L 415 300 Z' },
    { id: '5', name: 'Свердловская область', d: 'M 520 150 L 570 145 L 580 170 L 540 180 L 510 165 Z' },
    { id: '6', name: 'Республика Татарстан', d: 'M 470 200 L 520 195 L 530 220 L 490 230 L 460 215 Z' },
    { id: '7', name: 'Нижегородская область', d: 'M 440 190 L 480 185 L 490 210 L 455 220 L 435 205 Z' },
    { id: '8', name: 'Новосибирская область', d: 'M 680 200 L 730 195 L 740 225 L 700 235 L 670 220 Z' },
    { id: '9', name: 'Ростовская область', d: 'M 420 260 L 465 255 L 475 280 L 440 290 L 410 275 Z' },
    { id: '10', name: 'Челябинская область', d: 'M 540 190 L 585 185 L 595 215 L 560 225 L 530 210 Z' },
    { id: '11', name: 'Самарская область', d: 'M 470 230 L 515 225 L 525 250 L 490 260 L 460 245 Z' },
    { id: '12', name: 'Республика Башкортостан', d: 'M 510 210 L 555 205 L 565 235 L 530 245 L 500 230 Z' },
    { id: '13', name: 'Красноярский край', d: 'M 700 120 L 780 110 L 800 160 L 740 175 L 690 150 Z' },
    { id: '14', name: 'Пермский край', d: 'M 530 170 L 575 165 L 585 190 L 550 200 L 520 185 Z' },
    { id: '15', name: 'Волгоградская область', d: 'M 440 240 L 485 235 L 495 265 L 460 275 L 430 260 Z' },
  ];

  return (
    <div className="relative w-full">
      <svg
        viewBox="0 0 1000 400"
        className="w-full h-auto"
        style={{ maxHeight: '500px' }}
      >
        {/* Background */}
        <rect width="1000" height="400" fill="var(--bg)" />
        
        {/* Regions */}
        {regionPaths.map((region) => {
          const regionData = getRegionData(region.id);
          const count = regionData?.count || 0;
          const color = getRegionColor(count);
          const isHovered = hoveredRegion === region.id;

          return (
            <path
              key={region.id}
              d={region.d}
              fill={color}
              stroke="var(--surface)"
              strokeWidth={isHovered ? 3 : 1.5}
              opacity={isHovered ? 0.9 : 0.8}
              onMouseMove={(e) => handleMouseMove(e, region.id)}
              onMouseLeave={handleMouseLeave}
              style={{
                cursor: 'pointer',
                transition: 'all 0.2s ease',
              }}
            />
          );
        })}
      </svg>

      {/* Tooltip */}
      {hoveredRegion && (
        <div
          className="fixed z-50 px-3 py-2 rounded shadow-lg pointer-events-none"
          style={{
            backgroundColor: 'var(--surface)',
            border: '1px solid var(--border)',
            left: tooltipPos.x + 10,
            top: tooltipPos.y + 10,
          }}
        >
          <div style={{ color: 'var(--text-primary)' }}>
            <strong>{getRegionInfo(hoveredRegion).name}</strong>
          </div>
          <div style={{ color: 'var(--text-secondary)' }}>
            Регистраций: {getRegionInfo(hoveredRegion).count}
          </div>
        </div>
      )}
    </div>
  );
}
