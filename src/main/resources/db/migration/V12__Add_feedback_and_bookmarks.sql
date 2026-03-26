-- Feedback Collection - In-app surveys
CREATE TABLE IF NOT EXISTS user_feedback (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    feedback_type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    rating INTEGER,
    status VARCHAR(20) DEFAULT 'PENDING',
    admin_response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Favorites/Bookmarks - Quick access to frequent pages
CREATE TABLE IF NOT EXISTS user_bookmarks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    url VARCHAR(500) NOT NULL,
    icon VARCHAR(100),
    category VARCHAR(50) DEFAULT 'general',
    usage_count INTEGER DEFAULT 0,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Knowledge Base - Internal wiki/docs for staff
CREATE TABLE IF NOT EXISTS knowledge_base_articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    category VARCHAR(50),
    tags VARCHAR(255),
    author VARCHAR(100),
    published BOOLEAN DEFAULT false,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feedback_user_id ON user_feedback(user_id);
CREATE INDEX idx_feedback_status ON user_feedback(status);
CREATE INDEX idx_bookmarks_user_id ON user_bookmarks(user_id);
CREATE INDEX idx_bookmarks_category ON user_bookmarks(category);
CREATE INDEX idx_kb_published ON knowledge_base_articles(published);
CREATE INDEX idx_kb_category ON knowledge_base_articles(category);

-- Keyboard Shortcuts - Faster navigation
CREATE TABLE IF NOT EXISTS keyboard_shortcuts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    key VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shortcuts_user_id ON keyboard_shortcuts(user_id);