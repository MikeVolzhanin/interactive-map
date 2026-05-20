import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card } from '../components/ui/card';
import { RussiaMap } from '../components/RussiaMap';
import { MapLegend } from '../components/MapLegend';
import { LoadingState } from '../components/LoadingState';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { removeToken } from '../utils/auth';
import { RUSSIA_REGIONS, getMockMapData } from '../constants/regions';
import { LogOut } from 'lucide-react';

type ViewState = 'loading' | 'loaded' | 'empty' | 'error';

export function MapPage() {
  const navigate = useNavigate();
  const [viewState, setViewState] = useState<ViewState>('loading');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [mapData, setMapData] = useState<{ regionId: string; count: number }[]>([]);

  const loadData = async () => {
    setViewState('loading');
    try {
      // Mock API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      const mockData = getMockMapData();
      
      // Check if data is empty
      const hasData = mockData.some(d => d.count > 0);
      if (!hasData) {
        setViewState('empty');
      } else {
        setMapData(mockData);
        setViewState('loaded');
      }
    } catch (error) {
      setViewState('error');
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleApplyFilters = () => {
    loadData();
  };

  const handleResetFilters = () => {
    setDateFrom('');
    setDateTo('');
    loadData();
  };

  const handleLogout = () => {
    removeToken();
    navigate('/login');
  };

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--bg)' }}>
      {/* Header */}
      <header className="border-b" style={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)' }}>
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <h1 style={{ color: 'var(--text-primary)' }}>Статистика регистраций по регионам России</h1>
          <Button variant="outline" onClick={handleLogout}>
            <LogOut className="mr-2 h-4 w-4" />
            Выйти
          </Button>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        <div className="space-y-6">
          {/* Filters */}
          <Card className="p-6">
            <div className="space-y-4">
              <h2 style={{ color: 'var(--text-primary)' }}>Фильтры</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="dateFrom">Дата от</Label>
                  <Input
                    id="dateFrom"
                    type="date"
                    value={dateFrom}
                    onChange={(e) => setDateFrom(e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="dateTo">Дата до</Label>
                  <Input
                    id="dateTo"
                    type="date"
                    value={dateTo}
                    onChange={(e) => setDateTo(e.target.value)}
                  />
                </div>
                <div className="flex items-end gap-2">
                  <Button onClick={handleApplyFilters}>
                    Применить
                  </Button>
                  <Button variant="outline" onClick={handleResetFilters}>
                    Сбросить
                  </Button>
                </div>
              </div>
            </div>
          </Card>

          {/* Map */}
          <Card className="p-6">
            {viewState === 'loading' && <LoadingState message="Загрузка данных карты..." />}
            
            {viewState === 'empty' && (
              <EmptyState message="Нет данных за выбранный период" />
            )}
            
            {viewState === 'error' && (
              <ErrorState 
                message="Ошибка загрузки данных. Попробуйте еще раз." 
                onRetry={loadData}
              />
            )}
            
            {viewState === 'loaded' && (
              <div className="space-y-6">
                <RussiaMap data={mapData} regions={RUSSIA_REGIONS} />
                <MapLegend />
              </div>
            )}
          </Card>
        </div>
      </main>
    </div>
  );
}
