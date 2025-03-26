CREATE TYPE public.organization_type AS ENUM ('PERMANENT', 'MANUAL');

CREATE TYPE public.organization_scope AS ENUM ('SHORTENER_SCOPE');

CREATE SEQUENCE public.organization_ids START 10001 INCREMENT 1;

CREATE TABLE public.organizations(
    id BIGINT PRIMARY KEY,
    creator_user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    organization_scope public.organization_scope NOT NULL,
    site_url VARCHAR(127),
    description TEXT,
    organization_avatar_url VARCHAR(512),
    type public.organization_type NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE SEQUENCE public.organization_member_ids START 10001 INCREMENT 1;

CREATE TABLE public.organization_members(
    id BIGINT PRIMARY KEY,
    member_user_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    roles VARCHAR(63)[] NOT NULL,
    member_urls BIGINT[] NOT NULL,
    allowed_all_urls BOOLEAN NOT NULL
);

ALTER TABLE public.organizations
    ADD CONSTRAINT org_owner_user_fk
        FOREIGN KEY (creator_user_id)
            REFERENCES public.users(id)
                ON DELETE CASCADE;

ALTER TABLE public.organization_members
    ADD CONSTRAINT org_member_user_fk
        FOREIGN KEY (member_user_id)
            REFERENCES public.users(id)
                ON DELETE CASCADE;

ALTER TABLE public.organization_members
    ADD CONSTRAINT org_member_organization_fk
        FOREIGN KEY (organization_id)
            REFERENCES public.organizations(id)
            ON DELETE CASCADE;

ALTER TABLE public.organizations
    ADD CONSTRAINT org_name_ak
        UNIQUE (name);
