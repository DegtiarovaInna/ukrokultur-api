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
- GET /home — home page content (hero/mission/work fields)
- GET /about — about page content (intro + members)
- GET /news?page=1&pageSize=10&publishedOnly=true — paginated news feed
- GET /projects?publishedOnly=true — projects list
- POST /contact — contact form submission (hCaptcha optional, email via Resend)

### Admin (JWT Bearer)
- POST /auth/login — admin login, returns JWT
- PUT /admin/home — update home content
- GET /admin/home — get home content (including unpublished)
- GET /admin/about/intro — get about intro
- PUT /admin/about/intro — update about intro
- GET /admin/about/members — list members
- POST /admin/about/members — create member
- PUT /admin/about/members/{id} — update member
- DELETE /admin/about/members/{id} — delete member
- POST /admin/news — create news
- PUT /admin/news/{id} — update news
- DELETE /admin/news/{id} — delete news
- POST /admin/projects — create project
- PUT /admin/projects/{id} — update project
- DELETE /admin/projects/{id} — delete project
- POST /admin/media/upload/{folder} — upload file to Supabase Storage (folders: news/projects/about/home/pages)
- DELETE /admin/media?objectPath=... — delete file from Supabase Storage
- GET /actuator/health — health check (public)

## Data model (core)
- app_user (admins)
- home_page + home_work_field_item
- about_intro + about_member
- news + news_translation (en/de/uk) + news_image
- project + project_translation (en/de/uk) + project_image
- media stored in Supabase Storage, DB stores public URLs

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

