create table if not exists news_image (
    id bigserial primary key,
    news_id bigint not null references news(id) on delete cascade,
    url text not null,
    sort_order int not null default 0,
    is_cover boolean not null default false
);

create index if not exists idx_news_image_news_id on news_image(news_id);
create index if not exists idx_news_image_news_id_sort on news_image(news_id, sort_order);
