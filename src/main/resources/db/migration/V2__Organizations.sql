CREATE TYPE public.organization_type AS ENUM ('PERMANENT', 'MANUAL');

CREATE SEQUENCE public.organization_ids START 10001 INCREMENT 1;

CREATE TABLE public.organization(
    id BIGINT PRIMARY KEY,
    organization_name VARCHAR(255) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    type public.organization_type NOT NULL
);

CREATE SEQUENCE public.organization_member_ids START 10001 INCREMENT 1;

CREATE TABLE public.organization_member(
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    roles VARCHAR(63)[] NOT NULL,
    allowed_urls BIGINT[] NOT NULL,
    allowed_all_urls BOOLEAN NOT NULL
);

ALTER TABLE public.organization
    ADD CONSTRAINT org_owner_user_fk
        FOREIGN KEY (owner_user_id)
            REFERENCES public.service_user(id)
                ON DELETE CASCADE;

ALTER TABLE public.organization_member
    ADD CONSTRAINT org_member_user_fk
        FOREIGN KEY (user_id)
            REFERENCES public.service_user(id)
                ON DELETE CASCADE;

ALTER TABLE public.organization_member
    ADD CONSTRAINT org_member_organization_fk
        FOREIGN KEY (organization_id)
            REFERENCES public.organization(id)
            ON DELETE CASCADE;

ALTER TABLE public.organization
    ADD CONSTRAINT org_name_ak
        UNIQUE (organization_name);
