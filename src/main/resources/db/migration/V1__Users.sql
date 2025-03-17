CREATE TYPE public.service_user_type AS ENUM ('USER', 'ADMIN');

CREATE SEQUENCE public.service_user_ids START 10001 INCREMENT 1;

CREATE TABLE public.service_user(
    id BIGINT PRIMARY KEY,
    firstname VARCHAR(63) NOT NULL,
    lastname VARCHAR(63),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(15),
    password_hash VARCHAR(4095) NOT NULL,
    profile_picture_url VARCHAR(511),
    type public.service_user_type NOT NULL
);