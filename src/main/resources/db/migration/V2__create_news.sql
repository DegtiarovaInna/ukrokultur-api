create table if not exists news (
    id bigserial primary key,
    public_id uuid not null default gen_random_uuid() unique,
    slug varchar(200) unique,
    published_at timestamptz null,
    event_date date null,
    published boolean not null default true,
    video_url text null,
    video_type varchar(30) null,
    video_label varchar(200) null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
create index if not exists idx_news_public_id on news(public_id);
create index if not exists idx_news_published on news(published);
create index if not exists idx_news_slug on news(slug);
create index if not exists idx_news_event_date on news(event_date);