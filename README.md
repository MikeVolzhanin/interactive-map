# Interactive Map

Веб-приложение для работы с интерактивной картой. Frontend на React, backend на Spring Boot, база данных PostgreSQL.

## Стек технологий

| Слой | Технологии |
|------|-----------|
| Frontend | React 18, Vite, TypeScript, React Router |
| Backend | Java 21, Spring Boot, Spring Security, JWT |
| База данных | PostgreSQL 15, Liquibase |
| Деплой | Docker, Docker Compose, Nginx |

## Требования

- [Docker](https://www.docker.com/) и Docker Compose
- Git

## Быстрый старт

### 1. Клонировать репозиторий

```bash
git clone <repo-url>
cd interactive-map
```

### 2. Настроить переменные окружения

```bash
cp deploy/.env.example deploy/.env
```

Открыть `deploy/.env` и заполнить:

```env
# Database (можно оставить как есть для локального запуска)
SPRING_DATASOURCE_URL=jdbc:postgresql://interactive-map-db:5432/interactive-map-db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Mail — Gmail аккаунт для отправки писем
SUPPORT_EMAIL=your-email@gmail.com
APP_PASSWORD=your-gmail-app-password   # App Password из настроек Google

# JWT — случайная строка в base64 (минимум 32 символа)
JWT_SECRET_KEY=your-base64-encoded-secret-key
```

> Для получения Gmail App Password: Google Account → Security → 2-Step Verification → App passwords

### 3. Запустить

```bash
cd deploy
docker-compose up --build
```

После запуска:
- Фронтенд: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## Структура проекта

```
interactive-map/
├── frontend/               # React SPA
├── services/
│   └── applicants-service/ # Spring Boot сервис
├── db/                     # Liquibase миграции
│   ├── db.changelog-master.xml
│   └── changelog/
│       ├── v1.0.0.xml      # Начальная схема
│       ├── v1.0.1.xml      # Тестовые данные
│       └── v1.0.2.xml      # Добавление роли пользователя
└── deploy/
    ├── docker-compose.yml
    └── .env.example
```

## Полезные команды

### Пересобрать конкретный сервис после изменений

```bash
cd deploy
docker-compose up --build applicants-service
docker-compose up --build frontend
```

### Остановить все контейнеры

```bash
cd deploy
docker-compose down
```

### Остановить и удалить данные БД

```bash
cd deploy
docker-compose down -v
```

### Посмотреть логи сервиса

```bash
docker logs interactive-map-applicants-service -f
```

## Локальная разработка (без Docker)

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Запускается на http://localhost:3000. Запросы к `/api/*` проксируются на `http://localhost:8080`.

### Backend

Требуется запущенный PostgreSQL. Запустить только БД через Docker:

```bash
cd deploy
docker-compose up interactive-map-db migrator
```

Затем запустить сервис через IDE или Maven:

```bash
cd services/applicants-service
mvn spring-boot:run
```
