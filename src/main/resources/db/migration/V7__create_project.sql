create table if not exists project (
    id bigserial primary key,
    slug varchar(200) unique,
    published boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_project_published on project(published);
create index if not exists idx_project_slug on project(slug);
