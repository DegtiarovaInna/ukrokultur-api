create table if not exists project_image (
    id bigserial primary key,
    project_id bigint not null references project(id) on delete cascade,
    url text not null,
    sort_order int not null default 0,
    is_cover boolean not null default false
);

create index if not exists idx_project_image_project_id on project_image(project_id);
create index if not exists idx_project_image_project_id_sort on project_image(project_id, sort_order);
