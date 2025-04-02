ALTER TABLE public.organizations
    DROP CONSTRAINT org_owner_user_fk;

ALTER TABLE public.organizations
    ADD CONSTRAINT org_owner_user_fk
        FOREIGN KEY (creator_user_id)
            REFERENCES public.users(id);

ALTER TABLE public.organizations
    ALTER COLUMN creator_user_id DROP NOT NULL;

ALTER TABLE public.organization_members
    ALTER COLUMN member_user_id DROP NOT NULL;

ALTER TABLE public.organization_members
    ALTER COLUMN organization_id DROP NOT NULL;
