alter table news
    add column if not exists slug varchar(200) unique,
    add column if not exists event_date date null,
    add column if not exists published boolean not null default true,
    add column if not exists video_type varchar(30) null,
    add column if not exists video_label varchar(200) null;

create index if not exists idx_news_published on news(published);
create index if not exists idx_news_slug on news(slug);
create index if not exists idx_news_event_date on news(event_date);
