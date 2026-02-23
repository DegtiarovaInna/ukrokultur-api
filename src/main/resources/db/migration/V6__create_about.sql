create extension if not exists pgcrypto;
create table if not exists about_intro (
    id bigserial primary key,
    image text null,

    title_en text not null,
    title_de text not null,
    title_uk text not null,

    text_en text not null,
    text_de text not null,
    text_uk text not null,

    published boolean not null default true,
    updated_at timestamptz not null default now()
);

create table if not exists about_member (
    id uuid primary key default gen_random_uuid(),
    slug varchar(120) not null unique,
    name varchar(200) not null,
    image text null,
    sort_order int not null default 0,
    published boolean not null default true,
    instagram_url text null,

    role_en text not null,
    role_de text not null,
    role_uk text not null,

    biography_en text not null,
    biography_de text not null,
    biography_uk text not null
);
create index if not exists idx_about_member_sort on about_member(sort_order);
create index if not exists idx_about_member_slug on about_member(slug);