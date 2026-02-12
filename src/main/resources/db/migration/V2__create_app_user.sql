create table if not exists app_user (
    id bigserial primary key,
    email varchar(255) not null unique,
    password varchar(255) not null,
    role varchar(50) not null,
    created_at timestamptz not null default now()
);
