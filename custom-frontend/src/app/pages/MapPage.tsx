import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router';
import { Button } from '../components/ui/button';
import { Card } from '../components/ui/card';
import { RussiaMap } from '../components/RussiaMap';
import { MapLegend } from '../components/MapLegend';
import { LoadingState } from '../components/LoadingState';
import { EmptyState } from '../components/EmptyState';
import { isAuthenticated, removeToken } from '../utils/auth';
import { api } from '../api';
import type { Region } from '../api';
import { DEMO_MAP_REGIONS, buildDemoMapCounts } from '../constants/catalogStubs';
import { LogIn, LogOut } from 'lucide-react';

type ViewState = 'loading' | 'loaded' | 'empty';

export function MapPage() {
  const navigate = useNavigate();
  const [viewState, setViewState] = useState<ViewState>('loading');
  const [regions, setRegions] = useState<Region[]>([]);
  const [mapData, setMapData] = useState<{ regionId: string; count: number }[]>([]);
  const [demoMode, setDemoMode] = useState(false);

  const loadData = async () => {
    setViewState('loading');
    setDemoMode(false);

    try {
      const regionList = await api.getRegions();

      if (!regionList.length) {
        setRegions([]);
        setMapData([]);
        setViewState('empty');
        return;
      }

      let statsResponse: Awaited<ReturnType<typeof api.getMapStats>> = [];
      try {
        statsResponse = await api.getMapStats({});
      } catch {
        statsResponse = [];
      }

      const stats = Array.isArray(statsResponse) ? statsResponse : statsResponse.data || [];
      const normalized = regionList.map((region) => {
        const hit = stats.find((item) => String(item.regionId) === String(region.id));
        return {
          regionId: String(region.id),
          count: hit ? Number(hit.count) || 0 : 0,
        };
      });

      setRegions(regionList);
      setMapData(normalized);
      setViewState('loaded');
    } catch (error) {
      console.error(error);
      setRegions(DEMO_MAP_REGIONS);
      setMapData(buildDemoMapCounts());
      setDemoMode(true);
      setViewState('loaded');
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleLogout = () => {
    removeToken();
    navigate('/login');
  };

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--bg)' }}>
      <header className="border-b" style={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)' }}>
        <div className="container mx-auto flex flex-col gap-3 px-4 py-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-lg font-semibold sm:text-xl" style={{ color: 'var(--text-primary)' }}>
              Статистика регистраций по регионам России
            </h1>
            <p className="mt-0.5 text-sm" style={{ color: 'var(--text-secondary)' }}>
              Главная страница — карта доступна без входа
            </p>
          </div>
          {isAuthenticated() ? (
            <div className="flex shrink-0 flex-wrap items-center justify-end gap-2">
              <Button variant="outline" asChild>
                <Link to="/news">Новости</Link>
              </Button>
              <Button variant="outline" className="shrink-0 self-end sm:self-auto" onClick={handleLogout}>
                <LogOut className="mr-2 h-4 w-4" />
                Выйти
              </Button>
            </div>
          ) : (
            <div className="flex shrink-0 flex-wrap items-center justify-end gap-2">
              <Button variant="outline" asChild>
                <Link to="/news">Новости</Link>
              </Button>
              <Button variant="outline" asChild>
                <Link to="/register">Регистрация</Link>
              </Button>
              <Button variant="default" asChild>
                <Link to="/login">
                  <LogIn className="mr-2 h-4 w-4" />
                  Войти
                </Link>
              </Button>
            </div>
          )}
        </div>
      </header>

      <main className="container mx-auto space-y-4 px-4 py-8">
        {demoMode && (
          <div
            className="rounded-md border px-4 py-3 text-sm"
            style={{ borderColor: 'var(--border)', backgroundColor: 'var(--surface)', color: 'var(--text-secondary)' }}
            role="status"
          >
            <p className="font-medium" style={{ color: 'var(--text-primary)' }}>
              Демонстрационный режим
            </p>
            <p className="mt-1">
              Сервер сейчас не отдал данные — на карте показаны{' '}
              <strong>условные</strong> числа регистраций по регионам (диапазон 0–1000), чтобы было видно работу
              раскраски и легенды. Это не реальная статистика.
            </p>
            <Button variant="outline" size="sm" className="mt-3" onClick={() => loadData()}>
              Попробовать загрузить с сервера снова
            </Button>
          </div>
        )}

        <Card className="p-6">
          {viewState === 'loading' && <LoadingState message="Загрузка данных карты..." />}

          {viewState === 'empty' && (
            <EmptyState message="Справочник регионов пуст. Проверьте миграции и данные в базе." />
          )}

          {viewState === 'loaded' && (
            <div className="space-y-6">
              <RussiaMap
                data={mapData}
                regions={regions.map((region) => ({
                  id: String(region.id),
                  name: region.name,
                  code: String(region.id),
                }))}
              />
              <MapLegend />
            </div>
          )}
        </Card>
      </main>
    </div>
  );
}
