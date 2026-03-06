create extension if not exists pgcrypto;
create table if not exists project (
    id bigserial primary key,
    public_id uuid not null default gen_random_uuid() unique,
    slug varchar(200) unique,

    published boolean not null default true,
    sort_order int not null default 0,

    start_date date null,
    end_date date null,

    cover_image text null,

    title_en text not null,
    title_de text not null,
    title_uk text not null,

    subtitle_en text null,
    subtitle_de text null,
    subtitle_uk text null,

    description_en text not null,
    description_de text not null,
    description_uk text not null,

    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_project_published on project(published);
create index if not exists idx_project_slug on project(slug);
create index if not exists idx_project_public_id on project(public_id);
create index if not exists idx_project_sort_order on project(sort_order);

create table if not exists project_image (
    id bigserial primary key,
    project_id bigint not null references project(id) on delete cascade,
    url text not null,
    sort_order int not null default 0
);
create index if not exists idx_project_image_project_id on project_image(project_id);
create index if not exists idx_project_image_project_id_sort on project_image(project_id, sort_order);

create table if not exists project_goal (
    id bigserial primary key,
    project_id bigint not null references project(id) on delete cascade,
    sort_order int not null default 0,
    en text not null,
    de text not null,
    uk text not null
);
create index if not exists idx_project_goal_project_id on project_goal(project_id);
create index if not exists idx_project_goal_project_id_sort on project_goal(project_id, sort_order);

create table if not exists project_activity (
    id bigserial primary key,
    project_id bigint not null references project(id) on delete cascade,
    sort_order int not null default 0,
    en text not null,
    de text not null,
    uk text not null
);
create index if not exists idx_project_activity_project_id on project_activity(project_id);
create index if not exists idx_project_activity_project_id_sort on project_activity(project_id, sort_order);

create table if not exists project_partner (
    id bigserial primary key,
    project_id bigint not null references project(id) on delete cascade,
    sort_order int not null default 0,
    country_en text not null,
    country_de text not null,
    country_uk text not null,
    organization varchar(500) not null
);
create index if not exists idx_project_partner_project_id on project_partner(project_id);
create index if not exists idx_project_partner_project_id_sort on project_partner(project_id, sort_order);