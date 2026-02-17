create table if not exists news (
    id bigserial primary key,
    public_id uuid not null default gen_random_uuid() unique,
    published_at timestamptz null,
    video_url text null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
create index if not exists idx_news_public_id on news(public_id);