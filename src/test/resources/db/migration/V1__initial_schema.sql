create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    username varchar(255) not null unique,
    password_hash varchar(255) not null,
    name varchar(255) not null,
    profile_image_url varchar(255)
);

create table projects (
    id bigserial primary key,
    name varchar(255) not null,
    description text,
    owner_id bigint not null,
    constraint fk_projects_owner foreign key (owner_id) references users(id)
);

create table project_members (
    id bigserial primary key,
    project_id bigint not null,
    user_id bigint not null,
    role varchar(255) not null,
    status varchar(255) not null,
    role_title varchar(255),
    role_description text,
    can_view_project boolean not null,
    can_create_task boolean not null,
    can_edit_task boolean not null,
    can_delete_task boolean not null,
    can_invite_member boolean not null,
    can_manage_members boolean not null,
    constraint fk_project_members_project foreign key (project_id) references projects(id),
    constraint fk_project_members_user foreign key (user_id) references users(id),
    constraint uk_project_members_project_user unique (project_id, user_id)
);

create table project_invitations (
    id bigserial primary key,
    project_id bigint not null,
    invited_by bigint not null,
    invited_email varchar(255),
    token varchar(255) not null unique,
    status varchar(255) not null,
    expires_at timestamp not null,
    created_at timestamp not null,
    proposed_role varchar(255) not null,
    role_title varchar(255),
    role_description text,
    can_view_project boolean not null,
    can_create_task boolean not null,
    can_edit_task boolean not null,
    can_delete_task boolean not null,
    can_invite_member boolean not null,
    can_manage_members boolean not null,
    constraint fk_project_invitations_project foreign key (project_id) references projects(id),
    constraint fk_project_invitations_invited_by foreign key (invited_by) references users(id)
);

create table notifications (
    id bigserial primary key,
    user_id bigint not null,
    type varchar(255) not null,
    title varchar(255) not null,
    message text not null,
    read boolean not null,
    created_at timestamp not null,
    project_id bigint,
    invitation_id bigint,
    project_member_id bigint,
    constraint fk_notifications_user foreign key (user_id) references users(id),
    constraint fk_notifications_project foreign key (project_id) references projects(id),
    constraint fk_notifications_invitation foreign key (invitation_id) references project_invitations(id),
    constraint fk_notifications_project_member foreign key (project_member_id) references project_members(id)
);

create table tasks (
    id bigserial primary key,
    project_id bigint not null,
    title varchar(255) not null,
    description text,
    status varchar(255) not null,
    priority varchar(255) not null,
    due_date date,
    is_late boolean not null,
    assigned_to bigint,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_tasks_project foreign key (project_id) references projects(id),
    constraint fk_tasks_assigned_to foreign key (assigned_to) references users(id)
);

create table activity_logs (
    id bigserial primary key,
    project_id bigint not null,
    user_id bigint not null,
    action varchar(255) not null,
    created_at timestamp not null,
    constraint fk_activity_logs_project foreign key (project_id) references projects(id),
    constraint fk_activity_logs_user foreign key (user_id) references users(id)
);
