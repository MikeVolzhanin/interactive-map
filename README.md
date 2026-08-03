# Interactive Map

Веб-приложение для сбора данных абитуриентов и отображения статистики на интерактивной карте России.

Проект включает React SPA, Spring Boot backend, PostgreSQL и Liquibase-миграции. Основной сценарий запуска - Docker Compose из директории `deploy`.

## Возможности

- регистрация и вход пользователей с email-верификацией;
- восстановление пароля через код подтверждения;
- заполнение профиля абитуриента: регион, уровень образования, интересы, телефон;
- публичная карта со статистикой по регионам и интересам;
- публичная лента новостей с сайта НИУ ВШЭ Нижний Новгород;
- административная панель для справочников, выгрузки пользователей и импорта/экспорта конкурсов из XLSX;
- OpenAPI/Swagger для backend API;
- Liquibase-миграции с rollback-файлами.

## Стек

| Слой | Технологии |
| --- | --- |
| Backend | Java 21, Spring Boot 4, Spring Security, JWT, JPA, MapStruct |
| Frontend | React 18, Vite, React Router, react-simple-maps |
| Database | PostgreSQL 15, Liquibase |
| Files | Apache POI для XLSX |
| Deploy | Docker, Docker Compose, Nginx |
| Quality | JUnit 5, Mockito, Checkstyle, JaCoCo, SonarQube profile |

## Структура проекта

```text
interactive-map/
├── db/                         # Liquibase changelog и SQL-миграции
├── deploy/                     # Docker Compose, env-template, scripts
├── frontend/                   # Основной React SPA, используется в Docker deploy
├── custom-frontend/            # Альтернативный Vite/React UI
├── services/
│   └── applicants-service/     # Spring Boot backend
├── template.docx
└── README.md
```

## Быстрый старт через Docker

### 1. Подготовьте env-файл

```bash
cp deploy/.env.example deploy/.env
```

Заполните `deploy/.env`:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://interactive-map-db:5432/interactive-map-db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

SUPPORT_EMAIL=your-email@gmail.com
APP_PASSWORD=your-gmail-app-password

JWT_SECRET_KEY=your-base64-encoded-secret-key
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

`JWT_SECRET_KEY` должен быть base64-строкой достаточной длины для HMAC. Для локальной разработки можно сгенерировать ключ любым надежным генератором случайных байт и закодировать его в base64.

### 2. Запустите приложение

```bash
cd deploy
docker-compose up --build
```

После запуска:

- Frontend: <http://localhost:3000>
- Backend API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>

Контейнер `migrator` применяет Liquibase-миграции из `db/` до запуска backend-сервиса.

## Локальная разработка

### Backend

Поднимите PostgreSQL и примените миграции:

```bash
cd deploy
docker-compose up interactive-map-db migrator
```

Создайте локальный конфиг:

```bash
cp services/applicants-service/src/main/resources/application-example.yml \
   services/applicants-service/src/main/resources/application.yml
```

Запустите сервис:

```bash
cd services/applicants-service
mvn spring-boot:run
```

### Frontend

Основной frontend:

```bash
cd frontend
npm install
npm run dev
```

Vite dev server запускается на <http://localhost:3000> и проксирует `/api/*` на <http://localhost:8080>.

Альтернативный UI:

```bash
cd custom-frontend
npm install
npm run dev
```

## API

Основные группы endpoint'ов:

| Группа | Назначение |
| --- | --- |
| `/api/auth/*` | регистрация, вход, верификация, refresh token, logout, восстановление пароля |
| `/api/users/*` | профиль пользователя и XLSX-экспорт пользователей |
| `/api/map/*` | публичные данные для карты, интересов, регионального каталога и конкурсов |
| `/api/news` | публичная лента новостей |
| `/api/education-levels` | справочник уровней образования |
| `/api/interests` | справочник интересов |
| `/api/regions` | справочник регионов |
| `/api/admin/contests/*` | импорт, экспорт и очистка конкурсов |

Доступ:

- публичные endpoints: `/api/auth/signup`, `/api/auth/login`, `/api/auth/verify`, `/api/auth/resend`, `/api/auth/forgot-password`, `/api/auth/reset-password`, `/api/map/*`, `/api/news`;
- пользовательские endpoints требуют JWT;
- `/api/admin/**` требует роль `ADMIN`;
- сейчас роль `ADMIN` назначается при регистрации email на доменах `hse.ru` и `edu.hse.ru`.

## Миграции БД

Главный changelog: `db/db.changelog-master.xml`.

Миграции разделены по версиям:

```text
db/changelog/
├── v1.0.0.xml
├── ...
└── v1.0.8.xml
```

Применить миграции вручную:

```bash
cd deploy
docker-compose run --rm migrator
```

## Тесты и сборка

Backend:

```bash
cd services/applicants-service
mvn test
```

Frontend:

```bash
cd frontend
npm run build
```

Alternative frontend:

```bash
cd custom-frontend
npm run build
```

Проверка зависимостей:

```bash
cd frontend
npm audit --omit=dev

cd ../custom-frontend
npm audit --omit=dev
```

## SonarQube

Локальный SonarQube запускается отдельным compose-файлом:

```bash
cd deploy/sonarqube
docker-compose up -d
```

После создания token в SonarQube:

```bash
cd services/applicants-service
mvn verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<your-local-sonarqube-token>
```

## Работа с XLSX

Администратор может:

- выгружать пользователей в Excel через `/api/users/export`;
- импортировать конкурсы из XLSX через `/api/admin/contests/import`;
- выгружать конкурсы через `/api/admin/contests/export`;
- очистить список конкурсов через `DELETE /api/admin/contests`.

Пример файла импорта конкурсов доступен в `frontend/public/samples/contests-import-sample.xlsx`.

## Полезные команды

Пересобрать и запустить все сервисы:

```bash
cd deploy
docker-compose up --build -d
```

Пересобрать только backend и frontend:

```bash
cd deploy
docker-compose up --build -d applicants-service frontend
```

Остановить сервисы:

```bash
cd deploy
docker-compose down
```

Остановить сервисы и удалить volume PostgreSQL:

```bash
cd deploy
docker-compose down -v
```

Логи backend:

```bash
docker logs interactive-map-applicants-service -f
```

## Переменные окружения

| Переменная | Назначение |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | пользователь PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | пароль PostgreSQL |
| `SUPPORT_EMAIL` | SMTP-логин отправителя |
| `APP_PASSWORD` | пароль приложения для SMTP |
| `JWT_SECRET_KEY` | base64 secret для JWT |
| `CORS_ALLOWED_ORIGINS` | разрешенные origin для браузерных запросов |

## Примечания по безопасности

- Не коммитьте реальные `.env`, пароли SMTP и JWT secret.
- Для production укажите конкретные `CORS_ALLOWED_ORIGINS`, не используйте wildcard.
- Refresh tokens хранятся в БД и ограничены уникальностью по пользователю и token.
- Frontend хранит access/refresh tokens в `localStorage`; при повышенных требованиях к безопасности стоит рассмотреть httpOnly cookies и CSRF-защиту.
