// Mock data для регионов России
export const RUSSIA_REGIONS = [
  { id: '1', name: 'Москва', code: 'MOW' },
  { id: '2', name: 'Санкт-Петербург', code: 'SPE' },
  { id: '3', name: 'Московская область', code: 'MOS' },
  { id: '4', name: 'Краснодарский край', code: 'KDA' },
  { id: '5', name: 'Свердловская область', code: 'SVE' },
  { id: '6', name: 'Республика Татарстан', code: 'TA' },
  { id: '7', name: 'Нижегородская область', code: 'NIZ' },
  { id: '8', name: 'Новосибирская область', code: 'NVS' },
  { id: '9', name: 'Ростовская область', code: 'ROS' },
  { id: '10', name: 'Челябинская область', code: 'CHE' },
  { id: '11', name: 'Самарская область', code: 'SAM' },
  { id: '12', name: 'Республика Башкортостан', code: 'BA' },
  { id: '13', name: 'Красноярский край', code: 'KYA' },
  { id: '14', name: 'Пермский край', code: 'PER' },
  { id: '15', name: 'Волгоградская область', code: 'VGG' },
];

// Mock данные для статистики
export const getMockMapData = () => {
  return RUSSIA_REGIONS.map(region => ({
    regionId: region.id,
    count: Math.floor(Math.random() * 1000),
  }));
};
