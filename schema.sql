-- RecipeMate API - PostgreSQL Database Schema
-- Generated from JPA Entities
-- PostgreSQL 16+
-- Character Set: UTF-8

-- =====================================================
-- 1. ENUMS (Custom Types)
-- =====================================================

CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE group_buy_status AS ENUM ('RECRUITING', 'CONFIRMED', 'COMPLETED', 'CANCELLED');
CREATE TYPE delivery_method AS ENUM ('DIRECT', 'PARCEL');
CREATE TYPE post_category AS ENUM ('FREE', 'TIP', 'QUESTION', 'REVIEW');
CREATE TYPE comment_type AS ENUM ('GROUP_BUY', 'POST');
CREATE TYPE notification_type AS ENUM ('COMMENT', 'PARTICIPATION', 'GROUP_BUY_STATUS', 'BADGE', 'SYSTEM');
CREATE TYPE entity_type AS ENUM ('GROUP_BUY', 'POST', 'COMMENT', 'REVIEW');
CREATE TYPE badge_type AS ENUM (
    'FIRST_POST',
    'FIRST_GROUP_BUY',
    'GROUP_BUY_CREATOR_10',
    'PARTICIPATION_10',
    'PARTICIPATION_50',
    'REVIEW_WRITER_10',
    'ACTIVE_COMMENTER',
    'POPULAR_POST_WRITER'
);
CREATE TYPE point_type AS ENUM (
    'FIRST_POST',
    'FIRST_GROUP_BUY',
    'PARTICIPATION',
    'REVIEW',
    'COMMENT',
    'GROUP_BUY_COMPLETION',
    'ADMIN_GRANT'
);

-- =====================================================
-- 2. TABLES
-- =====================================================

-- -----------------------------------------------------
-- Table: users
-- -----------------------------------------------------
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    profile_image_url VARCHAR(500),
    role user_role NOT NULL DEFAULT 'USER',
    points INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_users_points CHECK (points >= 0)
);

COMMENT ON TABLE users IS '사용자 정보';
COMMENT ON COLUMN users.username IS '로그인 ID';
COMMENT ON COLUMN users.email IS '이메일 주소';
COMMENT ON COLUMN users.nickname IS '닉네임';
COMMENT ON COLUMN users.points IS '보유 포인트';
COMMENT ON COLUMN users.is_active IS '계정 활성화 여부';
COMMENT ON COLUMN users.deleted_at IS '소프트 삭제 시간';

-- -----------------------------------------------------
-- Table: persistent_logins
-- -----------------------------------------------------
CREATE TABLE persistent_logins (
    series VARCHAR(64) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

COMMENT ON TABLE persistent_logins IS 'Remember-Me 인증 토큰 저장';
COMMENT ON COLUMN persistent_logins.series IS '토큰 시리즈 ID';
COMMENT ON COLUMN persistent_logins.username IS '사용자 로그인 ID';
COMMENT ON COLUMN persistent_logins.token IS '토큰 값';
COMMENT ON COLUMN persistent_logins.last_used IS '마지막 사용 시간';

-- -----------------------------------------------------
-- Table: group_buys
-- -----------------------------------------------------
CREATE TABLE group_buys (
    id BIGSERIAL PRIMARY KEY,
    host_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    ingredients VARCHAR(1000),
    category VARCHAR(50) NOT NULL,
    total_price INTEGER NOT NULL,
    target_headcount INTEGER NOT NULL,
    current_headcount INTEGER NOT NULL DEFAULT 0,
    deadline TIMESTAMP NOT NULL,
    delivery_method VARCHAR(20) NOT NULL DEFAULT 'BOTH',
    meetup_location VARCHAR(200),
    parcel_fee INTEGER,
    is_participant_list_public BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(20) NOT NULL DEFAULT 'RECRUITING',
    recipe_api_id VARCHAR(100),
    recipe_name VARCHAR(200),
    recipe_image_url VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_groupbuy_host FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_group_buys_target_headcount CHECK (target_headcount >= 2),
    CONSTRAINT chk_group_buys_current_headcount CHECK (current_headcount >= 0),
    CONSTRAINT chk_group_buys_total_price CHECK (total_price >= 0)
);

COMMENT ON TABLE group_buys IS '공동구매 게시글';
COMMENT ON COLUMN group_buys.host_id IS '방장 ID';
COMMENT ON COLUMN group_buys.category IS '카테고리';
COMMENT ON COLUMN group_buys.ingredients IS '레시피 재료 목록 (레시피 기반 공구만 해당)';
COMMENT ON COLUMN group_buys.target_headcount IS '목표 인원';
COMMENT ON COLUMN group_buys.current_headcount IS '현재 참여 인원';
COMMENT ON COLUMN group_buys.total_price IS '총 가격';
COMMENT ON COLUMN group_buys.meetup_location IS '만남 장소';
COMMENT ON COLUMN group_buys.parcel_fee IS '택배비';
COMMENT ON COLUMN group_buys.is_participant_list_public IS '참여자 목록 공개 여부';
COMMENT ON COLUMN group_buys.recipe_api_id IS '레시피 API ID';
COMMENT ON COLUMN group_buys.recipe_name IS '레시피 이름';
COMMENT ON COLUMN group_buys.recipe_image_url IS '레시피 이미지 URL';
COMMENT ON COLUMN group_buys.deadline IS '마감 시간';
COMMENT ON COLUMN group_buys.version IS '낙관적 락 버전';

-- -----------------------------------------------------
-- Table: group_buy_images
-- -----------------------------------------------------
CREATE TABLE group_buy_images (
    id BIGSERIAL PRIMARY KEY,
    group_buy_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_group_buy_images_group_buy FOREIGN KEY (group_buy_id) REFERENCES group_buys(id) ON DELETE CASCADE,
    CONSTRAINT chk_group_buy_images_order CHECK (display_order >= 0)
);

COMMENT ON TABLE group_buy_images IS '공동구매 이미지';
COMMENT ON COLUMN group_buy_images.display_order IS '이미지 표시 순서';

-- -----------------------------------------------------
-- Table: participations
-- -----------------------------------------------------
CREATE TABLE participations (
    id BIGSERIAL PRIMARY KEY,
    group_buy_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    selected_delivery_method VARCHAR(20) NOT NULL,
    participated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_participation_groupbuy FOREIGN KEY (group_buy_id) REFERENCES group_buys(id) ON DELETE CASCADE,
    CONSTRAINT fk_participation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_participations_quantity CHECK (quantity >= 1),
    CONSTRAINT uk_participation_user_groupbuy UNIQUE (user_id, group_buy_id)
);

COMMENT ON TABLE participations IS '공동구매 참여 정보';
COMMENT ON COLUMN participations.quantity IS '참여 수량';
COMMENT ON COLUMN participations.selected_delivery_method IS '선택한 수령 방법';
COMMENT ON COLUMN participations.participated_at IS '참여 시간';

-- -----------------------------------------------------
-- Table: posts
-- -----------------------------------------------------
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    view_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_posts_view_count CHECK (view_count >= 0)
);

COMMENT ON TABLE posts IS '커뮤니티 게시글';
COMMENT ON COLUMN posts.category IS '게시글 카테고리 (FREE/TIP/QUESTION/REVIEW)';
COMMENT ON COLUMN posts.view_count IS '조회수';

-- -----------------------------------------------------
-- Table: post_images
-- -----------------------------------------------------
CREATE TABLE post_images (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_post_images_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT chk_post_images_order CHECK (display_order >= 0)
);

COMMENT ON TABLE post_images IS '게시글 이미지';
COMMENT ON COLUMN post_images.display_order IS '이미지 표시 순서';

-- -----------------------------------------------------
-- Table: comments
-- -----------------------------------------------------
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL,
    group_buy_id BIGINT,
    post_id BIGINT,
    parent_id BIGINT,
    type comment_type NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_group_buy FOREIGN KEY (group_buy_id) REFERENCES group_buys(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT chk_comments_target CHECK (
        (type = 'GROUP_BUY' AND group_buy_id IS NOT NULL AND post_id IS NULL) OR
        (type = 'POST' AND post_id IS NOT NULL AND group_buy_id IS NULL)
    )
);

COMMENT ON TABLE comments IS '댓글 (공동구매/게시글)';
COMMENT ON COLUMN comments.type IS '댓글 타입 (GROUP_BUY/POST)';
COMMENT ON CONSTRAINT chk_comments_target ON comments IS 'XOR: 공동구매 또는 게시글 중 하나에만 연결';

-- -----------------------------------------------------
-- Table: reviews
-- -----------------------------------------------------
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    group_buy_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_reviews_group_buy FOREIGN KEY (group_buy_id) REFERENCES group_buys(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_reviews_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT uk_reviews_group_buy_user UNIQUE (group_buy_id, user_id)
);

COMMENT ON TABLE reviews IS '공동구매 리뷰';
COMMENT ON COLUMN reviews.rating IS '별점 (1-5)';

-- -----------------------------------------------------
-- Table: wishlists
-- -----------------------------------------------------
CREATE TABLE wishlists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    entity_id BIGINT NOT NULL,
    entity_type entity_type NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_wishlists_user_entity UNIQUE (user_id, entity_id, entity_type)
);

COMMENT ON TABLE wishlists IS '위시리스트 (북마크)';
COMMENT ON COLUMN wishlists.entity_type IS '대상 타입 (GROUP_BUY/POST/COMMENT/REVIEW)';

-- -----------------------------------------------------
-- Table: notifications
-- -----------------------------------------------------
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    actor_id BIGINT,
    content VARCHAR(500) NOT NULL,
    url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT false,
    type VARCHAR(50) NOT NULL,
    related_entity_id BIGINT,
    related_entity_type VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL
);

COMMENT ON TABLE notifications IS '알림';
COMMENT ON COLUMN notifications.actor_id IS '알림을 발생시킨 사용자 ID';
COMMENT ON COLUMN notifications.type IS '알림 타입';
COMMENT ON COLUMN notifications.url IS '관련 URL';
COMMENT ON COLUMN notifications.is_read IS '읽음 여부';
COMMENT ON COLUMN notifications.related_entity_id IS '관련 엔티티 ID';
COMMENT ON COLUMN notifications.related_entity_type IS '관련 엔티티 타입';

-- -----------------------------------------------------
-- Table: badges
-- -----------------------------------------------------
CREATE TABLE badges (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_type badge_type NOT NULL,
    acquired_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_badges_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_badges_user_type UNIQUE (user_id, badge_type)
);

COMMENT ON TABLE badges IS '사용자 획득 뱃지';
COMMENT ON COLUMN badges.badge_type IS '뱃지 종류';
COMMENT ON COLUMN badges.acquired_at IS '획득 시간';

-- -----------------------------------------------------
-- Table: point_histories
-- -----------------------------------------------------
CREATE TABLE point_histories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type point_type NOT NULL,
    amount INTEGER NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_point_histories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE point_histories IS '포인트 변동 내역';
COMMENT ON COLUMN point_histories.type IS '포인트 변동 유형';
COMMENT ON COLUMN point_histories.amount IS '변동 포인트 (양수/음수)';

-- =====================================================
-- 3. INDEXES
-- =====================================================

-- users
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_nickname ON users(nickname);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- persistent_logins
CREATE INDEX idx_persistent_logins_username ON persistent_logins(username);
CREATE UNIQUE INDEX idx_persistent_logins_series ON persistent_logins(series);

-- group_buys
CREATE INDEX idx_groupbuy_host_id ON group_buys(host_id);
CREATE INDEX idx_groupbuy_status_deadline ON group_buys(status, deadline);
CREATE INDEX idx_groupbuy_recipe_api_id ON group_buys(recipe_api_id);
CREATE INDEX idx_groupbuy_category ON group_buys(category);
CREATE INDEX idx_groupbuy_deleted_created ON group_buys(deleted_at, created_at);
CREATE INDEX idx_groupbuy_status_deleted ON group_buys(status, deleted_at, created_at);
CREATE INDEX idx_groupbuy_category_deleted ON group_buys(category, deleted_at, created_at);

-- group_buy_images
CREATE INDEX idx_group_buy_images_group_buy ON group_buy_images(group_buy_id);
CREATE INDEX idx_group_buy_images_order ON group_buy_images(group_buy_id, display_order);

-- participations
CREATE INDEX idx_participation_user_id ON participations(user_id);
CREATE INDEX idx_participation_group_buy_id ON participations(group_buy_id);

-- posts
CREATE INDEX idx_post_category_created_at ON posts(category, created_at);
CREATE INDEX idx_post_author_id ON posts(author_id);
CREATE INDEX idx_post_deleted_at_created_at ON posts(deleted_at, created_at);
CREATE INDEX idx_post_category_deleted_at ON posts(category, deleted_at, created_at);

-- post_images
CREATE INDEX idx_post_images_post ON post_images(post_id);
CREATE INDEX idx_post_images_order ON post_images(post_id, display_order);

-- comments
CREATE INDEX idx_comment_group_buy_id ON comments(group_buy_id);
CREATE INDEX idx_comment_post_id ON comments(post_id);
CREATE INDEX idx_comment_parent_id ON comments(parent_id);
CREATE INDEX idx_comment_author_id ON comments(author_id);
CREATE INDEX idx_comment_group_buy_deleted ON comments(group_buy_id, deleted_at, created_at);
CREATE INDEX idx_comment_post_deleted ON comments(post_id, deleted_at, created_at);
CREATE INDEX idx_comment_parent_deleted ON comments(parent_id, deleted_at, created_at);

-- reviews
CREATE INDEX idx_reviews_group_buy ON reviews(group_buy_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_reviews_created_at ON reviews(created_at);
CREATE INDEX idx_reviews_deleted_at ON reviews(deleted_at);

-- wishlists
CREATE INDEX idx_wishlists_user ON wishlists(user_id);
CREATE INDEX idx_wishlists_entity ON wishlists(entity_id, entity_type);
CREATE INDEX idx_wishlists_created_at ON wishlists(created_at);

-- notifications
CREATE INDEX idx_user_id_is_read_created_at ON notifications(user_id, is_read, created_at);
CREATE INDEX idx_user_id_created_at ON notifications(user_id, created_at);

-- badges
CREATE INDEX idx_badges_user ON badges(user_id);
CREATE INDEX idx_badges_type ON badges(badge_type);
CREATE INDEX idx_badges_acquired_at ON badges(acquired_at);

-- point_histories
CREATE INDEX idx_point_histories_user ON point_histories(user_id);
CREATE INDEX idx_point_histories_type ON point_histories(type);
CREATE INDEX idx_point_histories_created_at ON point_histories(created_at);

-- =====================================================
-- 4. TRIGGERS (Auto-update timestamps)
-- =====================================================

-- Function to update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to tables with updated_at
CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_group_buys_updated_at
    BEFORE UPDATE ON group_buys
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_participations_updated_at
    BEFORE UPDATE ON participations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_posts_updated_at
    BEFORE UPDATE ON posts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_comments_updated_at
    BEFORE UPDATE ON comments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 5. INITIAL DATA (Optional)
-- =====================================================

-- Create default admin user (password: admin123, BCrypt encoded)
INSERT INTO users (username, password, email, nickname, role, points, is_active)
VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
    'admin@recipemate.com',
    '관리자',
    'ADMIN',
    0,
    true
);

-- =====================================================
-- 6. SCHEMA VERSION INFO
-- =====================================================

CREATE TABLE schema_version (
    id SERIAL PRIMARY KEY,
    version VARCHAR(50) NOT NULL,
    description TEXT,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO schema_version (version, description)
VALUES ('1.0.0', 'Initial schema - All entities from JPA mapping');

COMMENT ON TABLE schema_version IS '스키마 버전 관리';
