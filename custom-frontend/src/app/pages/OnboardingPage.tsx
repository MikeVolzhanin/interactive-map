import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card } from '../components/ui/card';
import { api } from '../api';
import type { EducationLevel, Interest, Region } from '../api';
import { STUB_EDUCATION_LEVELS, STUB_INTERESTS, STUB_REGIONS } from '../constants/catalogStubs';
import { Loader2 } from 'lucide-react';

const PHONE_RE = /^\+7\d{10}$/;

function nextAdmissionYear(): number {
  return new Date().getFullYear() + 1;
}

export function OnboardingPage() {
  const navigate = useNavigate();
  const [regions, setRegions] = useState<Region[]>([]);
  const [educationLevels, setEducationLevels] = useState<EducationLevel[]>([]);
  const [interests, setInterests] = useState<Interest[]>([]);
  const [catalogDemo, setCatalogDemo] = useState(false);

  const [firstName, setFirstName] = useState('');
  const [middleName, setMiddleName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [yearOfAdmission, setYearOfAdmission] = useState(String(nextAdmissionYear()));
  const [regionId, setRegionId] = useState('');
  const [educationLevelId, setEducationLevelId] = useState('');
  const [interestIds, setInterestIds] = useState<number[]>([]);

  const [initialLoading, setInitialLoading] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    (async () => {
      const settled = await Promise.allSettled([
        api.getRegions(),
        api.getEducationLevels(),
        api.getInterests(),
      ]);

      if (cancelled) return;

      let usedStub = false;

      if (settled[0].status === 'fulfilled') {
        setRegions(settled[0].value);
      } else {
        setRegions(STUB_REGIONS);
        usedStub = true;
      }

      if (settled[1].status === 'fulfilled') {
        setEducationLevels(settled[1].value);
      } else {
        setEducationLevels(STUB_EDUCATION_LEVELS);
        usedStub = true;
      }

      if (settled[2].status === 'fulfilled') {
        setInterests(settled[2].value);
      } else {
        setInterests(STUB_INTERESTS);
        usedStub = true;
      }

      setCatalogDemo(usedStub);
      setInitialLoading(false);
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  const toggleInterest = (id: number) => {
    setInterestIds((prev) => (prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]));
  };

  const validate = (): string | null => {
    if (!firstName.trim() || !lastName.trim()) return 'Укажите имя и фамилию.';
    if (!phoneNumber.trim()) return 'Укажите телефон.';
    if (!PHONE_RE.test(phoneNumber.trim())) return 'Телефон: формат +7 и 10 цифр (+79991234567).';
    if (!regionId) return 'Выберите регион из списка.';
    if (!educationLevelId) return 'Выберите уровень образования.';
    const y = Number(yearOfAdmission);
    const current = new Date().getFullYear();
    if (!Number.isInteger(y) || y <= current || y > 2100) {
      return `Год поступления должен быть будущим: от ${current + 1} до 2100.`;
    }
    if (interestIds.length < 1) return 'Выберите хотя бы одну сферу интересов.';
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const v = validate();
    if (v) {
      setError(v);
      return;
    }

    setLoading(true);

    try {
      if (catalogDemo) {
        window.alert(
          'Каталог загружен с демо-заглушек (API недоступен). Данные на сервер не отправляются — переход на карту для просмотра интерфейса.',
        );
        navigate('/map', { replace: true });
        return;
      }

      await api.addUserInfo({
        firstName: firstName.trim(),
        middleName: middleName.trim() || null,
        lastName: lastName.trim(),
        phoneNumber: phoneNumber.trim(),
        yearOfAdmission: Number(yearOfAdmission),
        educationLevelId: Number(educationLevelId),
        regionId: Number(regionId),
        interestIds,
        profileCompleted: true,
      });
      navigate('/map', { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Ошибка сохранения данных. Попробуйте снова.');
    } finally {
      setLoading(false);
    }
  };

  const minYear = new Date().getFullYear() + 1;

  return (
    <div className="min-h-screen flex items-center justify-center p-4" style={{ backgroundColor: 'var(--bg)' }}>
      <Card className="w-full max-w-lg p-8">
        <div className="mb-8 text-center">
          <h1 className="mb-2" style={{ color: 'var(--text-primary)' }}>
            Заполните анкету
          </h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Регион, год поступления и сферы интересов обязательны
          </p>
        </div>

        {initialLoading && (
          <div className="mb-4 text-sm" style={{ color: 'var(--text-secondary)' }}>
            Загружаем справочные данные…
          </div>
        )}

        {catalogDemo && !initialLoading && (
          <div
            className="mb-4 rounded-md border p-3 text-sm"
            style={{ borderColor: 'var(--border)', color: 'var(--text-secondary)' }}
            role="status"
          >
            Справочники недоступны с сервера — показаны демо-варианты. Сохранение анкеты на сервер будет отключено
            (можно посмотреть только работу формы).
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="space-y-2 sm:col-span-2">
              <Label htmlFor="lastName">Фамилия *</Label>
              <Input
                id="lastName"
                type="text"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
                disabled={loading}
                autoComplete="family-name"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="firstName">Имя *</Label>
              <Input
                id="firstName"
                type="text"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                required
                disabled={loading}
                autoComplete="given-name"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="middleName">Отчество</Label>
              <Input
                id="middleName"
                type="text"
                value={middleName}
                onChange={(e) => setMiddleName(e.target.value)}
                disabled={loading}
                autoComplete="additional-name"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="phoneNumber">Телефон * (+7 и 10 цифр)</Label>
            <Input
              id="phoneNumber"
              type="tel"
              placeholder="+79991234567"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="regionId">Регион проживания *</Label>
            <select
              id="regionId"
              className="w-full h-10 rounded-md border px-3 text-sm"
              style={{ borderColor: 'var(--border)', backgroundColor: 'var(--surface)', color: 'var(--text-primary)' }}
              value={regionId}
              onChange={(e) => setRegionId(e.target.value)}
              required
              disabled={loading || initialLoading}
            >
              <option value="">— Выберите регион —</option>
              {regions.map((region) => (
                <option key={region.id} value={region.id}>
                  {region.name}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="yearOfAdmission">Год поступления * (только будущие годы)</Label>
            <Input
              id="yearOfAdmission"
              type="number"
              min={minYear}
              max={2100}
              step={1}
              value={yearOfAdmission}
              onChange={(e) => setYearOfAdmission(e.target.value)}
              required
              disabled={loading}
            />
            <p className="text-xs" style={{ color: 'var(--text-secondary)' }}>
              Допустимо с {minYear} по 2100
            </p>
          </div>

          <div className="space-y-2">
            <Label htmlFor="educationLevelId">Уровень образования *</Label>
            <select
              id="educationLevelId"
              className="w-full h-10 rounded-md border px-3 text-sm"
              style={{ borderColor: 'var(--border)', backgroundColor: 'var(--surface)', color: 'var(--text-primary)' }}
              value={educationLevelId}
              onChange={(e) => setEducationLevelId(e.target.value)}
              required
              disabled={loading || initialLoading}
            >
              <option value="">— Выберите уровень —</option>
              {educationLevels.map((level) => (
                <option key={level.id} value={level.id}>
                  {level.level}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <span className="text-sm font-medium" style={{ color: 'var(--text-primary)' }}>
              Сферы интересов * (минимум одна)
            </span>
            <div
              className="max-h-44 overflow-auto rounded-md border p-3"
              style={{ borderColor: 'var(--border)' }}
            >
              {interests.length === 0 ? (
                <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>
                  Нет вариантов
                </p>
              ) : (
                interests.map((interest) => (
                  <label key={interest.id} className="flex cursor-pointer items-center gap-2 py-1.5 text-sm">
                    <input
                      type="checkbox"
                      checked={interestIds.includes(interest.id)}
                      onChange={() => toggleInterest(interest.id)}
                      disabled={loading || initialLoading}
                    />
                    <span>{interest.name}</span>
                  </label>
                ))
              )}
            </div>
          </div>

          {error && (
            <div className="rounded p-3 text-sm" style={{ backgroundColor: '#fee', color: 'var(--destructive)' }}>
              {error}
            </div>
          )}

          <Button type="submit" className="w-full" disabled={loading || initialLoading}>
            {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Сохранить и перейти к карте
          </Button>
        </form>
      </Card>
    </div>
  );
}
