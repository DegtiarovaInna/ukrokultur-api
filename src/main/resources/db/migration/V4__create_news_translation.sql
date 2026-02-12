create table if not exists news_translation (
    id bigserial primary key,
    news_id bigint not null references news(id) on delete cascade,
    lang varchar(5) not null,
    title varchar(500) not null,
    text text not null,
    unique (news_id, lang)
);

create index if not exists idx_news_translation_news_id on news_translation(news_id);
