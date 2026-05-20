import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router';
import { Button } from '../components/ui/button';
import { Card } from '../components/ui/card';
import { InputOTP, InputOTPGroup, InputOTPSlot } from '../components/ui/input-otp';
import { setToken } from '../utils/auth';
import { api } from '../api';
import { Loader2 } from 'lucide-react';

export function VerifyOtpPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const email = location.state?.email || '';
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (code.length !== 6) {
      setError('Введите 6-значный код');
      return;
    }

    setLoading(true);

    try {
      const response = await api.verifyOtp({
        email,
        verificationCode: code,
      });

      const token = response.accessToken || response.token;
      if (token) {
        setToken(token);
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken);
        }
      }

      navigate('/login');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Неверный код. Попробуйте снова.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!email) {
      setError('Не удалось определить email. Вернитесь на страницу регистрации.');
      return;
    }

    setLoading(true);
    setError('');
    try {
      await api.resendVerificationCode(email);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Не удалось отправить код повторно.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4" style={{ backgroundColor: 'var(--bg)' }}>
      <Card className="w-full max-w-md p-8">
        <div className="mb-8 text-center">
          <h1 className="mb-2" style={{ color: 'var(--text-primary)' }}>Подтверждение</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Введите код, отправленный на<br />
            <strong>{email}</strong>
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="flex justify-center">
            <InputOTP maxLength={6} value={code} onChange={setCode}>
              <InputOTPGroup>
                <InputOTPSlot index={0} />
                <InputOTPSlot index={1} />
                <InputOTPSlot index={2} />
                <InputOTPSlot index={3} />
                <InputOTPSlot index={4} />
                <InputOTPSlot index={5} />
              </InputOTPGroup>
            </InputOTP>
          </div>

          {error && (
            <div className="p-3 rounded" style={{ backgroundColor: '#fee', color: 'var(--destructive)' }}>
              {error}
            </div>
          )}

          <Button type="submit" className="w-full" disabled={loading || code.length !== 6}>
            {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Подтвердить
          </Button>

          <div className="text-center">
            <button 
              type="button"
              className="hover:underline"
              style={{ color: 'var(--primary)' }}
              onClick={handleResend}
            >
              Отправить код повторно
            </button>
          </div>

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
