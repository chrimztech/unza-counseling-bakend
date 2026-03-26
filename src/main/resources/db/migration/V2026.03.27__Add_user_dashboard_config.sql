-- Create user dashboard configuration table
CREATE TABLE IF NOT EXISTS user_dashboard_config (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    widget_id VARCHAR(100) NOT NULL,
    widget_type VARCHAR(50) NOT NULL,
    position_x INTEGER,
    position_y INTEGER,
    width INTEGER,
    height INTEGER,
    visible BOOLEAN DEFAULT true,
    config_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, widget_id)
);

-- Create indexes for faster queries
CREATE INDEX idx_dashboard_user_id ON user_dashboard_config(user_id);
CREATE INDEX idx_dashboard_user_widget ON user_dashboard_config(user_id, widget_id);
