import { Link, useNavigate } from 'react-router';
import { useCallback, useEffect, useState } from 'react';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { isAuthenticated, removeToken } from '../utils/auth';
import { loadNnovNewsPage, type NnovNewsItem } from '../utils/nnovNewsFromPage';
import { ExternalLink, LogIn, LogOut, Newspaper } from 'lucide-react';

const HSE_NN_NEWS_URL = 'https://nnov.hse.ru/news/';

export function NewsPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState<NnovNewsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const mergeByLink = useCallback((prev: NnovNewsItem[], batch: NnovNewsItem[]) => {
    const seen = new Set(prev.map((x) => x.link));
    const add = batch.filter((x) => !seen.has(x.link));
    return [...prev, ...add];
  }, []);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    loadNnovNewsPage(1)
      .then(({ items: batch, rawPostCount }) => {
        if (!cancelled) {
          setItems(batch);
          setHasMore(rawPostCount > 0);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setError(
            'Не удалось загрузить страницу новостей. В dev без бэкенда Vite проксирует HTML сам; в продакшене нужен сервер с прокси или nginx.'
          );
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const loadMore = useCallback(async () => {
    const next = page + 1;
    setLoadingMore(true);
    setError(null);
    try {
      const { items: batch, rawPostCount } = await loadNnovNewsPage(next);
      setItems((prev) => mergeByLink(prev, batch));
      setPage(next);
      if (rawPostCount === 0) {
        setHasMore(false);
      }
    } catch {
      setError('Не удалось подгрузить следующую страницу.');
    } finally {
      setLoadingMore(false);
    }
  }, [mergeByLink, page]);

  const handleLogout = () => {
    removeToken();
    navigate('/login');
  };

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--bg)' }}>
      <header className="border-b" style={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)' }}>
        <div className="container mx-auto flex flex-col gap-3 px-4 py-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="flex items-center gap-2 text-lg font-semibold sm:text-xl" style={{ color: 'var(--text-primary)' }}>
              <Newspaper className="h-6 w-6 shrink-0" aria-hidden />
              Новости кампуса
            </h1>
            <p className="mt-0.5 text-sm" style={{ color: 'var(--text-secondary)' }}>
              Только блоки новостей с nnov.hse.ru — заголовок, описание и фото со страницы раздела
            </p>
          </div>
          <div className="flex shrink-0 flex-wrap items-center justify-end gap-2">
            <Button variant="outline" asChild>
              <Link to="/map">Карта</Link>
            </Button>
            {isAuthenticated() ? (
              <Button variant="outline" onClick={handleLogout}>
                <LogOut className="mr-2 h-4 w-4" />
                Выйти
              </Button>
            ) : (
              <>
                <Button variant="outline" asChild>
                  <Link to="/register">Регистрация</Link>
                </Button>
                <Button variant="default" asChild>
                  <Link to="/login">
                    <LogIn className="mr-2 h-4 w-4" />
                    Войти
                  </Link>
                </Button>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="container mx-auto space-y-4 px-4 py-8">
        <Card className="border-dashed" style={{ borderColor: 'var(--border)', backgroundColor: 'var(--surface)' }}>
          <CardContent className="py-4 text-sm leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
            Страница{' '}
            <a href={HSE_NN_NEWS_URL} target="_blank" rel="noopener noreferrer" style={{ color: 'var(--primary)' }}>
              nnov.hse.ru/news
            </a>{' '}
            подгружается через сервер приложения (обход CORS). В браузере из HTML выбираются блоки списка новостей; в ленту попадают только материалы со ссылкой на домен nnov.hse.ru.
          </CardContent>
        </Card>

        {loading && (
          <div className="space-y-3">
            {Array.from({ length: 4 }).map((_, i) => (
              <Card key={i} className="animate-pulse overflow-hidden" style={{ backgroundColor: 'var(--surface)' }}>
                <div className="flex flex-col gap-4 p-4 sm:flex-row">
                  <div className="aspect-video w-full shrink-0 rounded-lg bg-[color-mix(in_srgb,var(--text-secondary)_12%,transparent)] sm:h-40 sm:w-56 sm:aspect-auto" />
                  <div className="flex-1 space-y-3">
                    <div className="h-5 w-3/4 rounded bg-[color-mix(in_srgb,var(--text-secondary)_20%,transparent)]" />
                    <div className="h-3 w-full rounded bg-[color-mix(in_srgb,var(--text-secondary)_12%,transparent)]" />
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}

        {!loading && error && (
          <Card style={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)' }}>
            <CardHeader>
              <CardTitle style={{ color: 'var(--text-primary)' }}>Ошибка загрузки</CardTitle>
              <CardDescription style={{ color: 'var(--text-secondary)' }}>{error}</CardDescription>
            </CardHeader>
            <CardContent>
              <Button variant="default" asChild>
                <a href={HSE_NN_NEWS_URL} target="_blank" rel="noopener noreferrer">
                  Открыть nnov.hse.ru/news
                  <ExternalLink className="ml-2 h-4 w-4" aria-hidden />
                </a>
              </Button>
            </CardContent>
          </Card>
        )}

        {!loading && !error && items.length === 0 && (
          <p className="text-center text-sm" style={{ color: 'var(--text-secondary)' }}>
            На странице не найдено новостей со ссылками только на nnov.hse.ru.
          </p>
        )}

        {!loading && !error && items.length > 0 && (
          <div className="space-y-6">
            {items.map((item) => (
              <Card
                key={item.link}
                className="overflow-hidden transition-shadow hover:shadow-md"
                style={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)' }}
              >
                <div className="flex flex-col gap-4 p-4 sm:flex-row sm:items-stretch">
                  {item.imageUrl ? (
                    <a
                      href={item.link}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="relative block shrink-0 overflow-hidden rounded-lg sm:h-44 sm:w-64"
                      style={{ backgroundColor: 'var(--bg)' }}
                    >
                      <img
                        src={item.imageUrl}
                        alt=""
                        className="h-full w-full object-cover"
                        loading="lazy"
                        decoding="async"
                      />
                    </a>
                  ) : null}
                  <div className="flex min-w-0 flex-1 flex-col gap-2">
                    <div className="flex flex-wrap items-baseline gap-2">
                      {item.dateLabel ? (
                        <span className="text-xs tabular-nums" style={{ color: 'var(--text-secondary)' }}>
                          {item.dateLabel}
                        </span>
                      ) : null}
                      {item.category ? (
                        <span
                          className="rounded-full px-2 py-0.5 text-xs font-medium"
                          style={{
                            backgroundColor: 'color-mix(in srgb, var(--primary) 12%, transparent)',
                            color: 'var(--primary)',
                          }}
                        >
                          {item.category}
                        </span>
                      ) : null}
                    </div>
                    <CardTitle className="text-lg leading-snug" style={{ color: 'var(--text-primary)' }}>
                      <a
                        href={item.link}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="hover:underline"
                        style={{ color: 'var(--primary)' }}
                      >
                        {item.title}
                      </a>
                    </CardTitle>
                    {item.description ? (
                      <CardDescription className="text-sm leading-relaxed sm:text-base" style={{ color: 'var(--text-secondary)' }}>
                        {item.description}
                      </CardDescription>
                    ) : null}
                    <div className="mt-auto pt-1">
                      <a
                        href={item.link}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-1 text-sm font-medium hover:underline"
                        style={{ color: 'var(--primary)' }}
                      >
                        Читать на nnov.hse.ru
                        <ExternalLink className="h-3.5 w-3.5" aria-hidden />
                      </a>
                    </div>
                  </div>
                </div>
              </Card>
            ))}

            {hasMore ? (
              <div className="flex justify-center pt-2">
                <Button type="button" variant="outline" disabled={loadingMore} onClick={() => void loadMore()}>
                  {loadingMore ? 'Загрузка…' : `Загрузить ещё (стр. ${page + 1})`}
                </Button>
              </div>
            ) : (
              <p className="text-center text-xs" style={{ color: 'var(--text-secondary)' }}>
                Дальше страниц нет или пришёл пустой ответ.
              </p>
            )}
          </div>
        )}

        <p className="text-center text-xs" style={{ color: 'var(--text-secondary)' }}>
          Источник разметки —{' '}
          <a href={HSE_NN_NEWS_URL} target="_blank" rel="noopener noreferrer" style={{ color: 'var(--primary)' }}>
            nnov.hse.ru/news
          </a>
          .
        </p>
      </main>
    </div>
  );
}
