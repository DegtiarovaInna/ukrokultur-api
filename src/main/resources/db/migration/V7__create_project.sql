create table if not exists project (
    id bigserial primary key,
    public_id uuid not null default gen_random_uuid() unique,
    slug varchar(200) unique,
    published boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_project_published on project(published);
create index if not exists idx_project_slug on project(slug);
create index if not exists idx_project_public_id on project(public_id);

