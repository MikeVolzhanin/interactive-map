import { useState } from 'react';
import { Link } from 'react-router';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card } from '../components/ui/card';
import { Loader2, CheckCircle2 } from 'lucide-react';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Mock API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      setSent(true);
    } catch (err) {
      setError('Ошибка отправки. Попробуйте снова.');
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <div className="min-h-screen flex items-center justify-center p-4" style={{ backgroundColor: 'var(--bg)' }}>
        <Card className="w-full max-w-md p-8">
          <div className="text-center space-y-6">
            <CheckCircle2 className="h-16 w-16 mx-auto" style={{ color: 'var(--primary)' }} />
            <div>
              <h1 className="mb-2" style={{ color: 'var(--text-primary)' }}>Письмо отправлено</h1>
              <p style={{ color: 'var(--text-secondary)' }}>
                Проверьте вашу почту {email} и следуйте инструкциям для восстановления пароля
              </p>
            </div>
            <Link to="/login">
              <Button className="w-full">
                Вернуться ко входу
              </Button>
            </Link>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4" style={{ backgroundColor: 'var(--bg)' }}>
      <Card className="w-full max-w-md p-8">
        <div className="mb-8 text-center">
          <h1 className="mb-2" style={{ color: 'var(--text-primary)' }}>Восстановление пароля</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Введите email для получения инструкций</p>
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

          {error && (
            <div className="p-3 rounded" style={{ backgroundColor: '#fee', color: 'var(--destructive)' }}>
              {error}
            </div>
          )}

          <Button type="submit" className="w-full" disabled={loading}>
            {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Отправить
          </Button>

          <div className="text-center" style={{ color: 'var(--text-secondary)' }}>
            <Link 
              to="/login" 
              className="hover:underline"
              style={{ color: 'var(--primary)' }}
            >
              Вернуться ко входу
            </Link>
          </div>
        </form>
      </Card>
    </div>
  );
}
