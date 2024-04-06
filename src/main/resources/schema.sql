DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS requests CASCADE;

CREATE TABLE IF NOT EXISTS users
(
    user_id BIGINT GENERATED ALWAYS AS IDENTITY,
    name    VARCHAR(255) NOT NULL,
    email   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (user_id),
    CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests
(
    request_id   BIGINT GENERATED ALWAYS AS IDENTITY,
    description  VARCHAR(512)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    requestor_id BIGINT,
    CONSTRAINT pk_request PRIMARY KEY (request_id),
    CONSTRAINT fk_requests_to_users FOREIGN KEY (requestor_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS items
(
    item_id      BIGINT GENERATED ALWAYS AS IDENTITY,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(512) NOT NULL,
    is_available BOOLEAN      NOT NULL,
    owner_id     BIGINT,
    request_id   BIGINT,
    CONSTRAINT pk_item PRIMARY KEY (item_id),
    CONSTRAINT fk_items_to_users FOREIGN KEY (owner_id) REFERENCES users (user_id),
    CONSTRAINT fk_items_to_requests FOREIGN KEY (request_id) REFERENCES requests (request_id)
);

CREATE TABLE IF NOT EXISTS bookings
(
    booking_id BIGINT GENERATED ALWAYS AS IDENTITY,
    status     VARCHAR(8)                  NOT NULL,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    booker_id  BIGINT,
    item_id    BIGINT,
    CONSTRAINT pk_booking PRIMARY KEY (booking_id),
    CONSTRAINT fk_bookings_to_users FOREIGN KEY (booker_id) REFERENCES users (user_id),
    CONSTRAINT fk_bookings_to_items FOREIGN KEY (item_id) REFERENCES items (item_id)
);

CREATE TABLE IF NOT EXISTS comments
(
    comment_id   BIGINT GENERATED ALWAYS AS IDENTITY,
    text         VARCHAR(512)                NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id      BIGINT,
    author_id    BIGINT,
    CONSTRAINT pk_comment PRIMARY KEY (comment_id),
    CONSTRAINT fk_comments_to_items FOREIGN KEY (item_id) REFERENCES items (item_id),
    CONSTRAINT fk_comments_to_users FOREIGN KEY (author_id) REFERENCES users (user_id)
);
