export function MapLegend() {
  const legendItems = [
    { label: '0 регистраций', color: 'var(--map-0)' },
    { label: '1-199', color: 'var(--map-1)' },
    { label: '200-399', color: 'var(--map-2)' },
    { label: '400-699', color: 'var(--map-3)' },
    { label: '700+', color: 'var(--map-4)' },
  ];

  return (
    <div className="flex flex-wrap gap-4 justify-center">
      {legendItems.map((item, index) => (
        <div key={index} className="flex items-center gap-2">
          <div
            className="w-6 h-6 rounded"
            style={{
              backgroundColor: item.color,
              border: '1px solid var(--border)',
            }}
          />
          <span style={{ color: 'var(--text-secondary)' }}>{item.label}</span>
        </div>
      ))}
    </div>
  );
}
