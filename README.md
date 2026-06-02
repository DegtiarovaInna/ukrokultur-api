# ukrokultur-api

Backend API for the “Help Ukrainians in Mönchengladbach” website.
The project provides public website content and an admin API for managing home page content, 
about page content, news, projects, media uploads, and contact form submissions.

## Tech Stack
- Java 21
- Spring Boot 4
- Spring Web
- Spring Validation
- Spring Data JPA / Hibernate
- Spring Security
- OAuth2 Resource Server / JWT
- PostgreSQL
- Flyway
- Supabase Storage
- Resend
- hCaptcha
- springdoc-openapi / Swagger UI
- Caffeine Cache
- JUnit, MockMvc, Mockito
- Testcontainers PostgreSQL
- Docker

## Features (MVP)
### Public
- `GET /home` — get public home page content
- `GET /about` — get public about page content
- `GET /news?page=1&pageSize=10&publishedOnly=true` — get paginated news
- `GET /news/{id}` — get public news item by id
- `GET /projects?page=1&pageSize=10&publishedOnly=true` — get paginated projects
- `GET /projects/{id}` — get public project by id
- `POST /contact` — send contact form message
- `GET /actuator/health` — health check

### Admin (JWT Bearer)
Authentication:

- `POST /auth/login` — admin login, returns JWT and sets admin session cookie
- `POST /auth/logout` — logout and clear admin cookie
- `GET /admin/me` — get current admin user

Home:

- `GET /admin/home` — get home page content, including unpublished content
- `PUT /admin/home` — update home page content with JSON
- `PUT /admin/home/multipart` — update home page content with JSON data and files

About:

- `GET /admin/about/intro` — get about intro
- `PUT /admin/about/intro` — update about intro with JSON
- `PUT /admin/about/intro/multipart` — update about intro with JSON data and image
- `GET /admin/about/members` — list about members
- `POST /admin/about/members` — create about member with JSON
- `POST /admin/about/members/multipart` — create about member with JSON data and image
- `PUT /admin/about/members/{id}` — update about member with JSON
- `PUT /admin/about/members/{id}/multipart` — update about member with JSON data and image
- `DELETE /admin/about/members/{id}` — delete about member
- `PATCH /admin/about/members/{id}/move` — reorder about member

News:

- `GET /admin/news/{id}` — get news item by id for admin
- `POST /admin/news` — create news with JSON
- `POST /admin/news/multipart` — create news with JSON data, images and/or video
- `PUT /admin/news/{id}` — update news with JSON
- `PUT /admin/news/{id}/multipart` — update news with JSON data, images and/or video
- `DELETE /admin/news/{id}` — delete news
- `POST /admin/news/{id}/images` — add images to news gallery
- `DELETE /admin/news/{id}/images` — delete one news image by URL
- `PATCH /admin/news/{id}/images/order` — reorder news images

Projects:

- `GET /admin/projects/{id}` — get project by id for admin
- `POST /admin/projects` — create project with JSON
- `POST /admin/projects/multipart` — create project with JSON data, cover image and/or gallery images
- `PUT /admin/projects/{id}` — update project with JSON
- `PUT /admin/projects/{id}/multipart` — update project with JSON data, cover image and/or gallery images
- `DELETE /admin/projects/{id}` — delete project
- `POST /admin/projects/{id}/gallery` — add images to project gallery
- `DELETE /admin/projects/{id}/gallery` — delete one project gallery image by URL
- `PATCH /admin/projects/{id}/gallery/order` — reorder project gallery images

Media:

- `POST /admin/media/upload` — upload file to default folder
- `POST /admin/media/upload/{folder}` — upload file to selected folder
- `POST /admin/media/upload/batch/{folder}` — upload multiple files
- `DELETE /admin/media?objectPath=...` — delete file from Supabase Storage

Allowed media folders:

- `news`
- `projects`
- `about`
- `home`
- `pages`

## Media Upload Rules

Media files are stored in Supabase Storage. The database stores public URLs.

Current upload limits:

- Images: max `10 MB`
- Videos: max `25 MB`
- One multipart file technical limit: `30 MB`
- Total multipart request limit: `50 MB`

Allowed image types:

- JPEG
- PNG
- WEBP
- GIF

Allowed video types:

- MP4
- WEBM
- MOV

## Data model (core)
- Core tables:

- app_user
- home_page
- home_work_field_item
- about_intro
- about_member
- news
- news_translation
- news_image
- project
- project_image

Media files are stored in Supabase Storage. Public file URLs are stored in the database.

## Local setup (Windows / IntelliJ IDEA Community)
### Requirements

* Java 21
* Docker Desktop
* Maven
* IntelliJ IDEA Community (or another Java IDE)

### Configure Environment

Create a local `.env` file (do not commit it). Use `.env.example` as a template.

Important for IntelliJ IDEA Community:

`.env` is NOT loaded automatically.

Add required variables in:

`Run Configuration → Environment Variables`

Minimum required:

* `SPRING_PROFILES_ACTIVE=dev`
* `SWAGGER_ENABLED=true`
* `JWT_SECRET=your-secret`

Plus any variables you need from `.env` (DB, Supabase, Resend, etc.).

### Start Local PostgreSQL

```bash
docker compose up -d
```

### Run Tests

Docker Desktop must be running.

Repository and integration tests use Testcontainers PostgreSQL.

```bash
mvnw.cmd test
```

### Run Application

```bash
mvnw.cmd spring-boot:run
```

### Swagger UI

Open:

```text
http://localhost:8080/swagger-ui/index.html
```

### Health Check

Open:

```text
http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```
