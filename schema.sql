-- RecipeMate API - PostgreSQL Database Schema
-- Generated from H2 schema dump, and converted to PostgreSQL dialect.
-- Character Set: UTF-8

-- =====================================================
-- 1. ENUMS (Custom Types)
-- =====================================================

CREATE TYPE badge_type AS ENUM('FIRST_GROUP_BUY', 'POPULAR_HOST', 'REVIEWER', 'TEN_PARTICIPATIONS');
CREATE TYPE comment_type AS ENUM('GENERAL', 'Q_AND_A');
CREATE TYPE group_buy_category AS ENUM('DAIRY', 'ETC', 'FRUIT', 'GRAIN', 'MEAT', 'RECIPE', 'SEAFOOD', 'SEASONING', 'SNACK', 'VEGETABLE');
CREATE TYPE delivery_method AS ENUM('BOTH', 'DIRECT', 'PARCEL');
CREATE TYPE group_buy_status AS ENUM('CANCELLED', 'CLOSED', 'COMPLETED', 'IMMINENT', 'RECRUITING');
CREATE TYPE notification_related_entity_type AS ENUM('COMMENT', 'DIRECT_MESSAGE', 'GROUP_BUY', 'POST', 'RECIPE', 'REVIEW');
CREATE TYPE notification_type AS ENUM('CANCEL_PARTICIPATION', 'COMMENT_GROUP_BUY', 'COMMENT_POST', 'DIRECT_MESSAGE', 'GROUP_BUY_COMPLETED', 'GROUP_BUY_DEADLINE', 'JOIN_GROUP_BUY', 'RECIPE_CORRECTION_APPROVED', 'RECIPE_CORRECTION_REJECTED', 'REPLY_COMMENT', 'REVIEW_GROUP_BUY');
CREATE TYPE participation_status AS ENUM('CANCELLED', 'PAYMENT_COMPLETED');
CREATE TYPE point_history_type AS ENUM('CHARGE', 'EARN', 'RECIPE_CORRECTION', 'REFUND', 'USE');
CREATE TYPE post_category AS ENUM('FREE', 'TIPS');
CREATE TYPE recipe_correction_type AS ENUM('INCORRECT_INFO', 'OTHER', 'SUGGESTION', 'TYPO');
CREATE TYPE recipe_correction_status AS ENUM('APPROVED', 'PENDING', 'REJECTED');
CREATE TYPE recipe_source_api AS ENUM('FOOD_SAFETY', 'MEAL_DB', 'USER');
CREATE TYPE report_type AS ENUM('ABUSE', 'COPYRIGHT', 'ETC', 'FALSE_INFORMATION', 'INAPPROPRIATE_CONTENT', 'SPAM');
CREATE TYPE reported_entity_type AS ENUM('COMMENT', 'GROUP_PURCHASE', 'POST', 'RECIPE', 'USER');
CREATE TYPE report_status AS ENUM('DISMISSED', 'PENDING', 'PROCESSED');
CREATE TYPE user_role AS ENUM('ADMIN', 'USER');

-- =====================================================
-- 2. TABLES
-- =====================================================

CREATE TABLE addresses(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    address_name VARCHAR(50) NOT NULL,
    city VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL,
    recipient_name VARCHAR(50) NOT NULL,
    recipient_phone_number VARCHAR(13) NOT NULL,
    street VARCHAR(200) NOT NULL,
    zipcode VARCHAR(10) NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE badges(
    id BIGSERIAL PRIMARY KEY,
    acquired_at TIMESTAMP(6) NOT NULL,
    badge_type badge_type NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE comment_likes(
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE comments(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    type comment_type NOT NULL,
    author_id BIGINT NOT NULL,
    group_buy_id BIGINT,
    parent_id BIGINT,
    post_id BIGINT
);

CREATE TABLE direct_messages(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL,
    receiver_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL
);

CREATE TABLE group_buy_images(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    display_order INTEGER NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    group_buy_id BIGINT NOT NULL
);

CREATE TABLE group_buys(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    category group_buy_category NOT NULL,
    content VARCHAR(2000) NOT NULL,
    current_amount INTEGER NOT NULL,
    current_headcount INTEGER NOT NULL,
    deadline TIMESTAMP(6) NOT NULL,
    delivery_method delivery_method NOT NULL,
    ingredients VARCHAR,
    is_participant_list_public BOOLEAN NOT NULL,
    latitude FLOAT(53),
    longitude FLOAT(53),
    meetup_location VARCHAR(200),
    parcel_fee INTEGER,
    recipe_api_id VARCHAR(100),
    recipe_image_url VARCHAR(500),
    recipe_name VARCHAR(200),
    status group_buy_status NOT NULL,
    target_amount INTEGER NOT NULL,
    target_headcount INTEGER NOT NULL,
    title VARCHAR(100) NOT NULL,
    version BIGINT,
    host_id BIGINT NOT NULL
);

CREATE TABLE manner_temp_histories(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    change_value FLOAT(53) NOT NULL,
    new_temperature FLOAT(53) NOT NULL,
    previous_temperature FLOAT(53) NOT NULL,
    reason VARCHAR(100) NOT NULL,
    related_review_id BIGINT,
    user_id BIGINT NOT NULL
);

CREATE TABLE notifications(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    content VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL,
    related_entity_id BIGINT,
    related_entity_type notification_related_entity_type,
    type notification_type NOT NULL,
    url VARCHAR(255),
    actor_id BIGINT,
    user_id BIGINT NOT NULL
);

CREATE TABLE participations(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    participated_at TIMESTAMP(6) NOT NULL,
    quantity INTEGER NOT NULL,
    selected_delivery_method delivery_method NOT NULL,
    status participation_status NOT NULL,
    total_payment INTEGER NOT NULL,
    address_id BIGINT,
    group_buy_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE persistent_logins(
    series VARCHAR(64) PRIMARY KEY,
    last_used TIMESTAMP(6) NOT NULL,
    token VARCHAR(64) NOT NULL,
    username VARCHAR(100) NOT NULL
);

CREATE TABLE point_histories(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    amount INTEGER NOT NULL,
    description VARCHAR(200) NOT NULL,
    type point_history_type NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE post_images(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    display_order INTEGER NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    post_id BIGINT NOT NULL
);

CREATE TABLE post_likes(
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE posts(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    category post_category NOT NULL,
    content VARCHAR NOT NULL,
    title VARCHAR(100) NOT NULL,
    view_count INTEGER NOT NULL,
    author_id BIGINT NOT NULL
);

CREATE TABLE recipe_corrections(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    admin_reason VARCHAR,
    correction_type recipe_correction_type NOT NULL,
    proposed_change VARCHAR NOT NULL,
    status recipe_correction_status NOT NULL,
    proposer_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    resolver_id BIGINT
);

CREATE TABLE recipe_ingredients(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    measure VARCHAR(200) NOT NULL,
    name VARCHAR(200) NOT NULL,
    recipe_id BIGINT NOT NULL
);

CREATE TABLE recipe_steps(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    description VARCHAR NOT NULL,
    image_url VARCHAR(500),
    step_number INTEGER NOT NULL,
    recipe_id BIGINT NOT NULL
);

CREATE TABLE recipe_wishlists(
    id BIGSERIAL PRIMARY KEY,
    wished_at TIMESTAMP(6) NOT NULL,
    recipe_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE recipes(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    area VARCHAR(100),
    calories INTEGER,
    carbohydrate INTEGER,
    category VARCHAR(100),
    fat INTEGER,
    full_image_url VARCHAR(500),
    instructions VARCHAR,
    last_synced_at TIMESTAMP(6) NOT NULL,
    protein INTEGER,
    serving_size VARCHAR(50),
    sodium INTEGER,
    source_api recipe_source_api NOT NULL,
    source_api_id VARCHAR(100),
    source_url VARCHAR(500),
    thumbnail_image_url VARCHAR(500),
    tips VARCHAR(1000),
    title VARCHAR(200) NOT NULL,
    youtube_url VARCHAR(500),
    user_id BIGINT
);

CREATE TABLE reports(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    admin_notes VARCHAR,
    content VARCHAR NOT NULL,
    report_type report_type NOT NULL,
    reported_entity_id BIGINT NOT NULL,
    reported_entity_type reported_entity_type NOT NULL,
    status report_status NOT NULL,
    reporter_id BIGINT NOT NULL,
    resolver_id BIGINT
);

CREATE TABLE reviews(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    content VARCHAR(500),
    rating INTEGER NOT NULL,
    group_buy_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL
);

CREATE TABLE search_keywords(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    search_count BIGINT NOT NULL
);

CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL,
    comment_notification BOOLEAN NOT NULL,
    email VARCHAR(100) NOT NULL,
    group_purchase_notification BOOLEAN NOT NULL,
    manner_temperature FLOAT(53) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(13) NOT NULL,
    points INTEGER NOT NULL,
    profile_image_url VARCHAR(500),
    role user_role NOT NULL
);

CREATE TABLE wishlists(
    id BIGSERIAL PRIMARY KEY,
    wished_at TIMESTAMP(6) NOT NULL,
    group_buy_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

-- =====================================================
-- 3. INDEXES
-- =====================================================

CREATE INDEX idx_address_user_id ON addresses(user_id);
CREATE INDEX idx_user_id_acquired_at ON badges(user_id, acquired_at);
CREATE INDEX idx_comment_like_comment_id ON comment_likes(comment_id);
CREATE INDEX idx_comment_like_user_id ON comment_likes(user_id);
CREATE INDEX idx_comment_group_buy_id ON comments(group_buy_id);
CREATE INDEX idx_comment_post_id ON comments(post_id);
CREATE INDEX idx_comment_parent_id ON comments(parent_id);
CREATE INDEX idx_comment_author_id ON comments(author_id);
CREATE INDEX idx_comment_group_buy_deleted ON comments(group_buy_id, deleted_at, created_at);
CREATE INDEX idx_comment_post_deleted ON comments(post_id, deleted_at, created_at);
CREATE INDEX idx_comment_parent_deleted ON comments(parent_id, deleted_at, created_at);
CREATE INDEX idx_dm_sender_created ON direct_messages(sender_id, created_at);
CREATE INDEX idx_dm_receiver_created ON direct_messages(receiver_id, created_at);
CREATE INDEX idx_dm_receiver_is_read ON direct_messages(receiver_id, is_read, created_at);
CREATE INDEX idx_groupbuy_status_deadline ON group_buys(status, deadline);
CREATE INDEX idx_groupbuy_recipe_api_id ON group_buys(recipe_api_id);
CREATE INDEX idx_groupbuy_category ON group_buys(category);
CREATE INDEX idx_groupbuy_host_id ON group_buys(host_id);
CREATE INDEX idx_groupbuy_deleted_created ON group_buys(deleted_at, created_at);
CREATE INDEX idx_groupbuy_status_deleted ON group_buys(status, deleted_at, created_at);
CREATE INDEX idx_groupbuy_category_deleted ON group_buys(category, deleted_at, created_at);
CREATE INDEX idx_manner_temp_history_user_id ON manner_temp_histories(user_id);
CREATE INDEX idx_manner_temp_history_created_at ON manner_temp_histories(created_at);
CREATE INDEX idx_user_id_is_read_created_at ON notifications(user_id, is_read, created_at);
CREATE INDEX idx_user_id_created_at ON notifications(user_id, created_at);
CREATE INDEX idx_participation_user_id ON participations(user_id);
CREATE INDEX idx_participation_group_buy_id ON participations(group_buy_id);
CREATE INDEX idx_persistent_logins_username ON persistent_logins(username);
CREATE INDEX idx_point_history_user_created ON point_histories(user_id, created_at);
CREATE INDEX idx_point_history_user_desc_created ON point_histories(user_id, description, created_at);
CREATE INDEX idx_post_like_post_id ON post_likes(post_id);
CREATE INDEX idx_post_like_user_id ON post_likes(user_id);
CREATE INDEX idx_post_category_created_at ON posts(category, created_at);
CREATE INDEX idx_post_author_id ON posts(author_id);
CREATE INDEX idx_post_deleted_at_created_at ON posts(deleted_at, created_at);
CREATE INDEX idx_post_category_deleted_at ON posts(category, deleted_at, created_at);
CREATE INDEX idx_recipe_correction_status ON recipe_corrections(status);
CREATE INDEX idx_recipe_correction_recipe_id ON recipe_corrections(recipe_id);
CREATE INDEX idx_recipe_correction_proposer_id ON recipe_corrections(proposer_id);
CREATE INDEX idx_recipe_ingredient_recipe_id ON recipe_ingredients(recipe_id);
CREATE INDEX idx_recipe_ingredient_name ON recipe_ingredients(name);
CREATE INDEX idx_recipe_id_step_number ON recipe_steps(recipe_id, step_number);
CREATE INDEX idx_recipe_wishlist_user_wished_at ON recipe_wishlists(user_id, wished_at);
CREATE INDEX idx_recipe_wishlist_recipe_id ON recipe_wishlists(recipe_id);
CREATE INDEX idx_title ON recipes(title);
CREATE INDEX idx_category ON recipes(category);
CREATE INDEX idx_area ON recipes(area);
CREATE INDEX idx_calories ON recipes(calories);
CREATE INDEX idx_source_api ON recipes(source_api);
CREATE INDEX idx_report_status ON reports(status);
CREATE INDEX idx_report_entity_type ON reports(reported_entity_type);
CREATE INDEX idx_report_reporter_id ON reports(reporter_id);
CREATE INDEX idx_review_group_buy_id ON reviews(group_buy_id);
CREATE INDEX idx_review_reviewer_id ON reviews(reviewer_id);
CREATE INDEX idx_search_keyword_count ON search_keywords(search_count DESC);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_nickname ON users(nickname);
CREATE INDEX idx_user_id_wished_at ON wishlists(user_id, wished_at);
CREATE INDEX idx_group_buy_id ON wishlists(group_buy_id);

-- =====================================================
-- 4. CONSTRAINTS
-- =====================================================

ALTER TABLE post_likes ADD CONSTRAINT post_like_uk UNIQUE (user_id, post_id);
ALTER TABLE badges ADD CONSTRAINT uk_user_badge_type UNIQUE (user_id, badge_type);
ALTER TABLE participations ADD CONSTRAINT uk_participation_user_groupbuy UNIQUE (user_id, group_buy_id);
ALTER TABLE group_buy_images ADD CONSTRAINT uk_group_buy_image_order UNIQUE (group_buy_id, display_order);
ALTER TABLE wishlists ADD CONSTRAINT uk_user_groupbuy UNIQUE (user_id, group_buy_id);
ALTER TABLE search_keywords ADD CONSTRAINT idx_search_keyword_keyword UNIQUE (keyword);
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE users ADD CONSTRAINT uk_users_nickname UNIQUE (nickname);
ALTER TABLE recipe_wishlists ADD CONSTRAINT uk_user_recipe UNIQUE (user_id, recipe_id);
ALTER TABLE comment_likes ADD CONSTRAINT comment_like_uk UNIQUE (user_id, comment_id);
ALTER TABLE reviews ADD CONSTRAINT uk_review_reviewer_groupbuy UNIQUE (reviewer_id, group_buy_id);
ALTER TABLE post_images ADD CONSTRAINT uk_post_image_order UNIQUE (post_id, display_order);
ALTER TABLE recipes ADD CONSTRAINT uk_source_api_id UNIQUE (source_api, source_api_id);

ALTER TABLE recipe_wishlists ADD CONSTRAINT fk_recipe_wishlist_user FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE recipe_corrections ADD CONSTRAINT fk_recipe_corrections_recipe FOREIGN KEY(recipe_id) REFERENCES recipes(id);
ALTER TABLE participations ADD CONSTRAINT fk_participation_user FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE reviews ADD CONSTRAINT fk_review_reviewer FOREIGN KEY(reviewer_id) REFERENCES users(id);
ALTER TABLE group_buys ADD CONSTRAINT fk_groupbuy_host FOREIGN KEY(host_id) REFERENCES users(id);
ALTER TABLE recipe_corrections ADD CONSTRAINT fk_recipe_corrections_resolver FOREIGN KEY(resolver_id) REFERENCES users(id);
ALTER TABLE direct_messages ADD CONSTRAINT fk_dm_sender FOREIGN KEY(sender_id) REFERENCES users(id);
ALTER TABLE reports ADD CONSTRAINT fk_reports_reporter FOREIGN KEY(reporter_id) REFERENCES users(id);
ALTER TABLE post_likes ADD CONSTRAINT fk_post_like_user FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE participations ADD CONSTRAINT fk_participation_address FOREIGN KEY(address_id) REFERENCES addresses(id);
ALTER TABLE badges ADD CONSTRAINT fk_badge_user FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE recipe_steps ADD CONSTRAINT fk_recipe_steps_recipe FOREIGN KEY(recipe_id) REFERENCES recipes(id);
ALTER TABLE recipes ADD CONSTRAINT fk_recipes_user FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE reports ADD CONSTRAINT fk_reports_resolver FOREIGN KEY(resolver_id) REFERENCES users(id);
ALTER TABLE comment_likes ADD CONSTRAINT fk_comment_like_user FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE addresses ADD CONSTRAINT fk_address_user FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE post_images ADD CONSTRAINT fk_post_image_post FOREIGN KEY(post_id) REFERENCES posts(id);
ALTER TABLE comment_likes ADD CONSTRAINT fk_comment_like_comment FOREIGN KEY(comment_id) REFERENCES comments(id);
ALTER TABLE recipe_corrections ADD CONSTRAINT fk_recipe_corrections_proposer FOREIGN KEY(proposer_id) REFERENCES users(id);
ALTER TABLE comments ADD CONSTRAINT fk_comment_post FOREIGN KEY(post_id) REFERENCES posts(id);
ALTER TABLE comments ADD CONSTRAINT fk_comment_author FOREIGN KEY(author_id) REFERENCES users(id);
ALTER TABLE group_buy_images ADD CONSTRAINT fk_group_buy_image_group_buy FOREIGN KEY(group_buy_id) REFERENCES group_buys(id);
ALTER TABLE posts ADD CONSTRAINT fk_post_author FOREIGN KEY(author_id) REFERENCES users(id);
ALTER TABLE direct_messages ADD CONSTRAINT fk_dm_receiver FOREIGN KEY(receiver_id) REFERENCES users(id);
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_actor FOREIGN KEY(actor_id) REFERENCES users(id);
ALTER TABLE wishlists ADD CONSTRAINT fk_wishlist_groupbuy FOREIGN KEY(group_buy_id) REFERENCES group_buys(id);
ALTER TABLE wishlists ADD CONSTRAINT fk_wishlist_user FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE recipe_ingredients ADD CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY(recipe_id) REFERENCES recipes(id);
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_user FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE manner_temp_histories ADD CONSTRAINT fk_manner_temp_history_user FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE comments ADD CONSTRAINT fk_comment_parent FOREIGN KEY(parent_id) REFERENCES comments(id);
ALTER TABLE reviews ADD CONSTRAINT fk_review_groupbuy FOREIGN KEY(group_buy_id) REFERENCES group_buys(id);
ALTER TABLE comments ADD CONSTRAINT fk_comment_group_buy FOREIGN KEY(group_buy_id) REFERENCES group_buys(id);
ALTER TABLE participations ADD CONSTRAINT fk_participation_groupbuy FOREIGN KEY(group_buy_id) REFERENCES group_buys(id);
ALTER TABLE post_likes ADD CONSTRAINT fk_post_like_post FOREIGN KEY(post_id) REFERENCES posts(id);