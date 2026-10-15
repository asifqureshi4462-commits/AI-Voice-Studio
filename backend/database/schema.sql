-- AI Voice Studio Database Schema
-- Developer: Asif Qureshi

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    plan_tier VARCHAR(64) DEFAULT 'pro_creator',
    monthly_character_quota INT DEFAULT 100000,
    characters_used_this_cycle INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    text_prompt TEXT NOT NULL,
    voice_id VARCHAR(128) NOT NULL,
    voice_name VARCHAR(128) NOT NULL,
    model_id VARCHAR(128) NOT NULL,
    audio_storage_url TEXT NOT NULL,
    duration_ms INT NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    project_type VARCHAR(32) DEFAULT 'tts',
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS favorite_voices (
    user_id VARCHAR(64) REFERENCES users(id) ON DELETE CASCADE,
    voice_id VARCHAR(128) NOT NULL,
    PRIMARY KEY (user_id, voice_id)
);

CREATE TABLE IF NOT EXISTS usage_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) REFERENCES users(id) ON DELETE CASCADE,
    voice_id VARCHAR(128) NOT NULL,
    characters_count INT NOT NULL,
    latency_ms INT,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
