-- V12__create_home.sql

create table if not exists home_page (
    id bigserial primary key,

    hero_image text null,
    hero_title_en text not null,
    hero_title_de text not null,
    hero_title_uk text not null,
    hero_subtitle_en text not null,
    hero_subtitle_de text not null,
    hero_subtitle_uk text not null,
    hero_published boolean not null default true,

    mission_image text null,
    mission_title_en text not null,
    mission_title_de text not null,
    mission_title_uk text not null,
    mission_text_en text not null,
    mission_text_de text not null,
    mission_text_uk text not null,
    mission_published boolean not null default true,

    work_fields_published boolean not null default true,

    updated_at timestamptz not null default now()
);

create table if not exists home_work_field_item (
    id varchar(120) primary key,
    sort_order int not null default 0,
    published boolean not null default true,

    title_en text not null,
    title_de text not null,
    title_uk text not null,

    description_en text not null,
    description_de text not null,
    description_uk text not null
);

create index if not exists idx_home_work_field_sort on home_work_field_item(sort_order);
