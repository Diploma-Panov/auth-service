INSERT INTO public.users(id, first_name, last_name, company_name, email, password_hash, profile_picture_url, system_role, registration_date, last_login_date)
    VALUES (9999, 'Maksym', 'Administrator', 'KhNUE', 'admin@mpanov.com', '$2a$07$fuLAGabVkcYFx.cDzAp6dexGCFUZi8ClSiUDj6tI7sYYBfptH4clq', null, 'ADMIN', current_timestamp, current_timestamp);

INSERT INTO public.users(id, first_name, last_name, company_name, email, password_hash, profile_picture_url, system_role, registration_date, last_login_date)
    VALUES (10000, 'Maksym', 'Panov', 'KhNUE', 'maksym@mpanov.com', '$2a$07$fuLAGabVkcYFx.cDzAp6dexGCFUZi8ClSiUDj6tI7sYYBfptH4clq', null, 'USER', current_timestamp, current_timestamp);

INSERT INTO public.organizations(id, creator_user_id, name, slug, organization_scope, site_url, description, organization_avatar_url, created_at, type)
    VALUES (9999, 9999, 'Administrator Organization', 'administrator-organization', 'SHORTENER_SCOPE', 'https://mpanov.com', 'Administrator seeded organization', null, current_timestamp,  'PERMANENT');

INSERT INTO public.organizations(id, creator_user_id, name, slug, organization_scope, site_url, description, organization_avatar_url, created_at, type)
    VALUES (10000, 10000, 'Maksym Panov Organization', 'maksym-panov-organization', 'SHORTENER_SCOPE', 'https://mpanov.com', 'Maksym Panov seeded organization', null, current_timestamp, 'PERMANENT');

INSERT INTO public.organization_members(id, member_user_id, organization_id, roles, member_urls, allowed_all_urls)
    VALUES (9999, 9999, 9999, '{"ORGANIZATION_OWNER"}', '{}', true);

INSERT INTO public.organization_members(id, member_user_id, organization_id, roles, member_urls, allowed_all_urls)
    VALUES (10000, 10000, 10000, '{"ORGANIZATION_OWNER"}', '{}', true);
