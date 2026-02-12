create table if not exists project_translation (
    id bigserial primary key,
    project_id bigint not null references project(id) on delete cascade,
    lang varchar(5) not null,
    title varchar(500) not null,
    text text not null,
    unique (project_id, lang)
);

create index if not exists idx_project_translation_project_id on project_translation(project_id);
