# ukrokultur-api

Backend API for the “Help Ukrainians in Mönchengladbach” website.

## Tech Stack
- Java 21
- Spring Boot 4 (Web, Validation, Data JPA, Security, OAuth2 Resource Server)
- PostgreSQL
- Flyway (DB migrations)
- Supabase Storage (images/files)
- Resend (email sending)
- springdoc-openapi (Swagger UI)

## Features (MVP)
### Public
- `GET /news?page=1&pageSize=10` — paginated news feed (sorted by `newsDate` desc)
- `POST /contact` — contact form submission (hCaptcha optional in dev, email via Resend)

### Admin (JWT Bearer)
- `POST /auth/login` — admin login, returns JWT
- `POST /admin/news` — create news
- `PUT /admin/news/{id}` — update news
- `DELETE /admin/news/{id}` — delete news
- `POST /admin/media/upload` — upload file to Supabase Storage
- `DELETE /admin/media?objectPath=...` — delete file from Supabase Storage

## Data model
- `app_user` (admins only)
- `news`
- `news_translation` (en/de/uk)
- `news_image` (optional; cover + gallery)

## Local setup (Windows / IntelliJ IDEA Community)
### 1) Requirements
- Java 21
- Docker Desktop

### 2) Configure environment
Create `.env` locally (do not commit it). Use `.env.example` as a template.

Important (IntelliJ Community):
.env is NOT auto-loaded. Add variables in Run Configuration → Environment variables:
- `SPRING_PROFILES_ACTIVE=dev`
- `SWAGGER_ENABLED=true`
- plus variables from `.env` you need (DB, JWT_SECRET, Supabase, Resend, etc.)

### 3) Start local Postgres
```bash
docker compose up -d
### 4) Run tests

`mvnw.cmd test`

### 5) Run application
`mvnw.cmd spring-boot:run`

### 5) Swagger
Open:
http://localhost:8080/swagger-ui/index.html

