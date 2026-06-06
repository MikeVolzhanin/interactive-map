import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card } from '../components/ui/card';
import { api } from '../api';
import { Loader2 } from 'lucide-react';

export function ResetPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const email = searchParams.get('email') || '';
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('Пароли не совпадают');
      return;
    }

    if (password.length < 6) {
      setError('Пароль должен содержать минимум 6 символов');
      return;
    }

    setLoading(true);

    try {
      await api.resetPassword({ email, password });
      navigate('/login');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Ошибка сброса пароля. Попробуйте снова.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4" style={{ backgroundColor: 'var(--bg)' }}>
      <Card className="w-full max-w-md p-8">
        <div className="mb-8 text-center">
          <h1 className="mb-2" style={{ color: 'var(--text-primary)' }}>Новый пароль</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Введите новый пароль для вашего аккаунта</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {!email && (
            <div className="p-3 rounded" style={{ backgroundColor: '#fee', color: 'var(--destructive)' }}>
              В ссылке отсутствует email. Передайте параметр `email` в URL.
            </div>
          )}
          <div className="space-y-2">
            <Label htmlFor="password">Новый пароль</Label>
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

          <div className="space-y-2">
            <Label htmlFor="confirmPassword">Подтвердите пароль</Label>
            <Input
              id="confirmPassword"
              type="password"
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          {error && (
            <div className="p-3 rounded" style={{ backgroundColor: '#fee', color: 'var(--destructive)' }}>
              {error}
            </div>
          )}

          <Button type="submit" className="w-full" disabled={loading || !email}>
            {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Сбросить пароль
          </Button>
        </form>
      </Card>
    </div>
  );
}
