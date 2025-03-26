CREATE TYPE public.user_system_role AS ENUM ('USER', 'ADMIN');

CREATE SEQUENCE public.user_ids START 10001 INCREMENT 1;

CREATE TABLE public.users(
    id BIGINT UNIQUE NOT NULL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    company_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(2048) NOT NULL,
    profile_picture_url VARCHAR(512),
    system_role public.user_system_role NOT NULL,
    registration_date TIMESTAMP NOT NULL DEFAULT current_timestamp,
    last_login_date TIMESTAMP NOT NULL DEFAULT current_timestamp
);