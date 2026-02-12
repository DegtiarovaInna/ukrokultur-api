create table if not exists news (
    id bigserial primary key,
    published_at timestamptz null,
    video_url text null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
