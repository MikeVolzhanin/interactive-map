import { useState } from 'react';
import { useNavigate, Link } from 'react-router';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card } from '../components/ui/card';
import { setToken } from '../utils/auth';
import { api } from '../api';
import { Loader2 } from 'lucide-react';

export function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.login({ email, password });
      const token = response.accessToken || response.token;
      if (!token) {
        throw new Error('Сервер не вернул токен авторизации');
      }
      setToken(token);
      if (response.refreshToken) {
        localStorage.setItem('refreshToken', response.refreshToken);
      }
      navigate('/map', { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Ошибка входа. Проверьте данные.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-4" style={{ backgroundColor: 'var(--bg)' }}>
      <div className="mb-4 w-full max-w-md text-center">
        <Link
          to="/map"
          className="text-sm hover:underline"
          style={{ color: 'var(--primary)' }}
        >
          ← На главную (карта)
        </Link>
      </div>
      <Card className="w-full max-w-md p-8">
        <div className="mb-8 text-center">
          <h1 className="mb-2" style={{ color: 'var(--text-primary)' }}>Вход в систему</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Введите ваши учетные данные</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              placeholder="example@hse.ru"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Пароль</Label>
            <Input
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          {error && (
            <div className="p-3 rounded" style={{ backgroundColor: '#fee', color: 'var(--destructive)' }}>
              {error}
            </div>
          )}

          <Button type="submit" className="w-full" disabled={loading}>
            {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Войти
          </Button>

          <div className="flex flex-col gap-2 text-center">
            <Link 
              to="/forgot-password" 
              className="hover:underline"
              style={{ color: 'var(--primary)' }}
            >
              Забыли пароль?
            </Link>
            <div style={{ color: 'var(--text-secondary)' }}>
              Нет аккаунта?{' '}
              <Link 
                to="/register" 
                className="hover:underline"
                style={{ color: 'var(--primary)' }}
              >
                Зарегистрироваться
              </Link>
            </div>
          </div>
        </form>
      </Card>
    </div>
  );
}
