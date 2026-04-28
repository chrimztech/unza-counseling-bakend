CREATE TABLE IF NOT EXISTS keyboard_shortcuts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    key VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_keyboard_shortcuts_user_id ON keyboard_shortcuts(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_keyboard_shortcuts_user_key ON keyboard_shortcuts(user_id, key);

CREATE TABLE IF NOT EXISTS user_bookmarks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    url VARCHAR(255) NOT NULL,
    icon VARCHAR(255),
    category VARCHAR(255),
    usage_count INTEGER DEFAULT 0,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_bookmarks_user_id ON user_bookmarks(user_id);
CREATE INDEX IF NOT EXISTS idx_user_bookmarks_category ON user_bookmarks(user_id, category);

CREATE TABLE IF NOT EXISTS user_feedback (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    feedback_type VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    rating INTEGER,
    status VARCHAR(255) DEFAULT 'PENDING',
    admin_response VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_feedback_user_id ON user_feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_user_feedback_status ON user_feedback(status);
