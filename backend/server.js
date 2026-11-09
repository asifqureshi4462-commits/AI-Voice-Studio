'use strict';

const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const jwt = require('jsonwebtoken');

const app = express();

// --- Configuration ---
const PORT = process.env.PORT || 8080;
const JWT_SECRET = process.env.JWT_SECRET || 'avs_production_jwt_secret_key_minimum_32_chars!';
const ELEVENLABS_API_KEY = process.env.ELEVENLABS_API_KEY || '';
const DB_PATH = process.env.DB_PATH || path.join(__dirname, 'data', 'app.db');
const AUDIO_DIR = process.env.AUDIO_DIR || path.join(__dirname, 'data', 'audio');
const MAIL_WEBHOOK_URL = process.env.MAIL_WEBHOOK_URL || '';
const FREE_MONTHLY_CHARS = parseInt(process.env.FREE_MONTHLY_CHARS || '100000', 10);
const ACCESS_TOKEN_EXPIRY = '2h';

// Ensure required directories exist
fs.mkdirSync(path.dirname(DB_PATH), { recursive: true });
fs.mkdirSync(AUDIO_DIR, { recursive: true });

// --- Persistent Database Setup (SQLite with JSON Fallback) ---
let dbSync = null;
let isNativeSqlite = false;

try {
  const { DatabaseSync } = require('node:sqlite');
  dbSync = new DatabaseSync(DB_PATH);
  isNativeSqlite = true;
} catch (e) {
  dbSync = null;
  isNativeSqlite = false;
}

const jsonStorePath = path.join(path.dirname(DB_PATH), 'store.json');
let memoryStore = {
  users: [],
  refreshTokens: [],
  passwordResets: [],
  generations: [],
  projects: [],
  favoriteVoices: []
};

function loadJsonStore() {
  if (fs.existsSync(jsonStorePath)) {
    try {
      memoryStore = JSON.parse(fs.readFileSync(jsonStorePath, 'utf8'));
    } catch (err) {
      console.error('Error loading JSON storage:', err);
    }
  }
}

function saveJsonStore() {
  const tmpPath = jsonStorePath + '.tmp';
  fs.writeFileSync(tmpPath, JSON.stringify(memoryStore, null, 2), 'utf8');
  fs.renameSync(tmpPath, jsonStorePath);
}

if (isNativeSqlite) {
  dbSync.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      email TEXT UNIQUE NOT NULL,
      password_hash TEXT NOT NULL,
      role TEXT NOT NULL DEFAULT 'user',
      plan TEXT NOT NULL DEFAULT 'free',
      email_verified INTEGER NOT NULL DEFAULT 1,
      created_at INTEGER NOT NULL
    );

    CREATE TABLE IF NOT EXISTS refresh_tokens (
      token_hash TEXT PRIMARY KEY,
      user_id TEXT NOT NULL,
      expires_at INTEGER NOT NULL,
      revoked INTEGER NOT NULL DEFAULT 0
    );

    CREATE TABLE IF NOT EXISTS password_resets (
      token TEXT PRIMARY KEY,
      email TEXT NOT NULL,
      expires_at INTEGER NOT NULL,
      used INTEGER NOT NULL DEFAULT 0
    );

    CREATE TABLE IF NOT EXISTS generations (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL,
      project_id TEXT,
      provider TEXT NOT NULL,
      model TEXT NOT NULL,
      voice_id TEXT NOT NULL,
      text_preview TEXT,
      text TEXT,
      output_format TEXT NOT NULL,
      status TEXT NOT NULL,
      audio_asset_id TEXT,
      char_count INTEGER NOT NULL,
      duration_ms INTEGER,
      bytes INTEGER,
      settings_json TEXT,
      created_at INTEGER NOT NULL,
      completed_at INTEGER
    );

    CREATE TABLE IF NOT EXISTS projects (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL,
      name TEXT NOT NULL,
      type TEXT NOT NULL DEFAULT 'tts',
      is_favorite INTEGER NOT NULL DEFAULT 0,
      is_draft INTEGER NOT NULL DEFAULT 0,
      content_json TEXT,
      settings_json TEXT,
      created_at INTEGER NOT NULL,
      updated_at INTEGER NOT NULL
    );

    CREATE TABLE IF NOT EXISTS favorite_voices (
      user_id TEXT NOT NULL,
      voice_id TEXT NOT NULL,
      name TEXT NOT NULL,
      created_at INTEGER NOT NULL,
      PRIMARY KEY (user_id, voice_id)
    );
  `);
} else {
  loadJsonStore();
}

// --- Database Operations ---
const db = {
  findUserByEmail(email) {
    const clean = (email || '').trim().toLowerCase();
    if (isNativeSqlite) {
      return dbSync.prepare('SELECT * FROM users WHERE LOWER(email) = ?').get(clean) || null;
    }
    return memoryStore.users.find(u => u.email.toLowerCase() === clean) || null;
  },

  findUserById(id) {
    if (isNativeSqlite) {
      return dbSync.prepare('SELECT * FROM users WHERE id = ?').get(id) || null;
    }
    return memoryStore.users.find(u => u.id === id) || null;
  },

  createUser(user) {
    if (isNativeSqlite) {
      dbSync.prepare(`
        INSERT INTO users (id, name, email, password_hash, role, plan, email_verified, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      `).run(user.id, user.name, user.email, user.password_hash, user.role, user.plan, user.email_verified ? 1 : 0, user.created_at);
    } else {
      memoryStore.users.push(user);
      saveJsonStore();
    }
    return user;
  },

  updateUserName(id, name) {
    if (isNativeSqlite) {
      dbSync.prepare('UPDATE users SET name = ? WHERE id = ?').run(name, id);
    } else {
      const u = memoryStore.users.find(x => x.id === id);
      if (u) { u.name = name; saveJsonStore(); }
    }
  },

  updateUserPassword(id, hash) {
    if (isNativeSqlite) {
      dbSync.prepare('UPDATE users SET password_hash = ? WHERE id = ?').run(hash, id);
    } else {
      const u = memoryStore.users.find(x => x.id === id);
      if (u) { u.password_hash = hash; saveJsonStore(); }
    }
  },

  deleteUser(id) {
    if (isNativeSqlite) {
      dbSync.prepare('DELETE FROM users WHERE id = ?').run(id);
      dbSync.prepare('DELETE FROM refresh_tokens WHERE user_id = ?').run(id);
      dbSync.prepare('DELETE FROM generations WHERE user_id = ?').run(id);
      dbSync.prepare('DELETE FROM projects WHERE user_id = ?').run(id);
      dbSync.prepare('DELETE FROM favorite_voices WHERE user_id = ?').run(id);
    } else {
      memoryStore.users = memoryStore.users.filter(x => x.id !== id);
      memoryStore.refreshTokens = memoryStore.refreshTokens.filter(x => x.user_id !== id);
      memoryStore.generations = memoryStore.generations.filter(x => x.user_id !== id);
      memoryStore.projects = memoryStore.projects.filter(x => x.user_id !== id);
      memoryStore.favoriteVoices = memoryStore.favoriteVoices.filter(x => x.user_id !== id);
      saveJsonStore();
    }
  },

  saveRefreshToken(tokenHash, userId, expiresAt) {
    if (isNativeSqlite) {
      dbSync.prepare('INSERT INTO refresh_tokens (token_hash, user_id, expires_at, revoked) VALUES (?, ?, ?, 0)').run(tokenHash, userId, expiresAt);
    } else {
      memoryStore.refreshTokens.push({ token_hash: tokenHash, user_id: userId, expires_at: expiresAt, revoked: 0 });
      saveJsonStore();
    }
  },

  findRefreshToken(tokenHash) {
    if (isNativeSqlite) {
      return dbSync.prepare('SELECT * FROM refresh_tokens WHERE token_hash = ?').get(tokenHash) || null;
    }
    return memoryStore.refreshTokens.find(x => x.token_hash === tokenHash) || null;
  },

  revokeRefreshToken(tokenHash) {
    if (isNativeSqlite) {
      dbSync.prepare('UPDATE refresh_tokens SET revoked = 1 WHERE token_hash = ?').run(tokenHash);
    } else {
      const t = memoryStore.refreshTokens.find(x => x.token_hash === tokenHash);
      if (t) { t.revoked = 1; saveJsonStore(); }
    }
  },

  revokeAllUserTokens(userId) {
    if (isNativeSqlite) {
      dbSync.prepare('UPDATE refresh_tokens SET revoked = 1 WHERE user_id = ?').run(userId);
    } else {
      memoryStore.refreshTokens.forEach(x => { if (x.user_id === userId) x.revoked = 1; });
      saveJsonStore();
    }
  },

  savePasswordReset(token, email, expiresAt) {
    if (isNativeSqlite) {
      dbSync.prepare('INSERT INTO password_resets (token, email, expires_at, used) VALUES (?, ?, ?, 0)').run(token, email, expiresAt);
    } else {
      memoryStore.passwordResets.push({ token, email, expires_at: expiresAt, used: 0 });
      saveJsonStore();
    }
  },

  findPasswordReset(token) {
    if (isNativeSqlite) {
      return dbSync.prepare('SELECT * FROM password_resets WHERE token = ?').get(token) || null;
    }
    return memoryStore.passwordResets.find(x => x.token === token) || null;
  },

  markResetUsed(token) {
    if (isNativeSqlite) {
      dbSync.prepare('UPDATE password_resets SET used = 1 WHERE token = ?').run(token);
    } else {
      const r = memoryStore.passwordResets.find(x => x.token === token);
      if (r) { r.used = 1; saveJsonStore(); }
    }
  },

  getUserCounts(userId) {
    if (isNativeSqlite) {
      const p = dbSync.prepare('SELECT COUNT(*) as c FROM projects WHERE user_id = ?').get(userId).c;
      const g = dbSync.prepare('SELECT COUNT(*) as c FROM generations WHERE user_id = ?').get(userId).c;
      const f = dbSync.prepare('SELECT COUNT(*) as c FROM favorite_voices WHERE user_id = ?').get(userId).c;
      return { projects: p, generations: g, favoriteVoices: f };
    }
    return {
      projects: memoryStore.projects.filter(x => x.user_id === userId).length,
      generations: memoryStore.generations.filter(x => x.user_id === userId).length,
      favoriteVoices: memoryStore.favoriteVoices.filter(x => x.user_id === userId).length
    };
  },

  getMonthlyCharsUsed(userId) {
    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);
    const startMs = monthStart.getTime();

    if (isNativeSqlite) {
      const row = dbSync.prepare(`
        SELECT COALESCE(SUM(char_count), 0) as total, COUNT(*) as gen_count
        FROM generations WHERE user_id = ? AND created_at >= ? AND status = 'completed'
      `).get(userId, startMs);
      return { used: row.total, count: row.gen_count };
    }

    const filtered = memoryStore.generations.filter(x => x.user_id === userId && x.created_at >= startMs && x.status === 'completed');
    const used = filtered.reduce((acc, cur) => acc + (cur.char_count || 0), 0);
    return { used, count: filtered.length };
  },

  saveGeneration(gen) {
    if (isNativeSqlite) {
      dbSync.prepare(`
        INSERT INTO generations (
          id, user_id, project_id, provider, model, voice_id, text_preview, text,
          output_format, status, audio_asset_id, char_count, duration_ms, bytes,
          settings_json, created_at, completed_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `).run(
        gen.id, gen.user_id, gen.project_id || null, gen.provider, gen.model, gen.voice_id,
        gen.text_preview, gen.text, gen.output_format, gen.status, gen.audio_asset_id,
        gen.char_count, gen.duration_ms || null, gen.bytes || null,
        gen.settings_json || null, gen.created_at, gen.completed_at || null
      );
    } else {
      memoryStore.generations.unshift(gen);
      saveJsonStore();
    }
  },

  getGenerations(userId, q, limit, offset) {
    limit = Math.min(Math.max(1, limit || 25), 100);
    offset = Math.max(0, offset || 0);

    if (isNativeSqlite) {
      let query = 'SELECT * FROM generations WHERE user_id = ?';
      const params = [userId];
      if (q) {
        query += ' AND (text_preview LIKE ? OR model LIKE ?)';
        params.push(`%${q}%`, `%${q}%`);
      }
      const total = dbSync.prepare(`SELECT COUNT(*) as total FROM (${query})`).get(...params).total;
      query += ' ORDER BY created_at DESC LIMIT ? OFFSET ?';
      params.push(limit, offset);
      const rows = dbSync.prepare(query).all(...params);
      return { rows, total };
    }

    let list = memoryStore.generations.filter(x => x.user_id === userId);
    if (q) {
      const qLow = q.toLowerCase();
      list = list.filter(x => (x.text_preview && x.text_preview.toLowerCase().includes(qLow)) || (x.model && x.model.toLowerCase().includes(qLow)));
    }
    const total = list.length;
    const rows = list.slice(offset, offset + limit);
    return { rows, total };
  },

  deleteGeneration(userId, id) {
    if (isNativeSqlite) {
      dbSync.prepare('DELETE FROM generations WHERE user_id = ? AND id = ?').run(userId, id);
    } else {
      memoryStore.generations = memoryStore.generations.filter(x => !(x.user_id === userId && x.id === id));
      saveJsonStore();
    }
  },

  updateGenerationDuration(assetId, durationMs) {
    if (isNativeSqlite) {
      dbSync.prepare('UPDATE generations SET duration_ms = ? WHERE audio_asset_id = ?').run(durationMs, assetId);
    } else {
      const g = memoryStore.generations.find(x => x.audio_asset_id === assetId);
      if (g) { g.duration_ms = durationMs; saveJsonStore(); }
    }
  },

  getProjects(userId, q, favorite, limit, offset) {
    limit = Math.min(Math.max(1, limit || 25), 100);
    offset = Math.max(0, offset || 0);

    if (isNativeSqlite) {
      let query = 'SELECT * FROM projects WHERE user_id = ?';
      const params = [userId];
      if (q) { query += ' AND name LIKE ?'; params.push(`%${q}%`); }
      if (favorite !== null && favorite !== undefined) { query += ' AND is_favorite = ?'; params.push(favorite ? 1 : 0); }
      const total = dbSync.prepare(`SELECT COUNT(*) as total FROM (${query})`).get(...params).total;
      query += ' ORDER BY updated_at DESC LIMIT ? OFFSET ?';
      params.push(limit, offset);
      const rows = dbSync.prepare(query).all(...params);
      return { rows, total };
    }

    let list = memoryStore.projects.filter(x => x.user_id === userId);
    if (q) { const qLow = q.toLowerCase(); list = list.filter(x => x.name.toLowerCase().includes(qLow)); }
    if (favorite !== null && favorite !== undefined) { list = list.filter(x => Boolean(x.is_favorite) === Boolean(favorite)); }
    const total = list.length;
    const rows = list.slice(offset, offset + limit);
    return { rows, total };
  },

  createProject(proj) {
    if (isNativeSqlite) {
      dbSync.prepare(`
        INSERT INTO projects (id, user_id, name, type, is_favorite, is_draft, content_json, settings_json, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `).run(proj.id, proj.user_id, proj.name, proj.type, proj.is_favorite ? 1 : 0, proj.is_draft ? 1 : 0, proj.content_json, proj.settings_json, proj.created_at, proj.updated_at);
    } else {
      memoryStore.projects.unshift(proj);
      saveJsonStore();
    }
    return proj;
  },

  updateProject(userId, id, updates) {
    if (isNativeSqlite) {
      const fields = [];
      const vals = [];
      if (updates.name !== undefined) { fields.push('name = ?'); vals.push(updates.name); }
      if (updates.isFavorite !== undefined) { fields.push('is_favorite = ?'); vals.push(updates.isFavorite ? 1 : 0); }
      if (updates.isDraft !== undefined) { fields.push('is_draft = ?'); vals.push(updates.isDraft ? 1 : 0); }
      if (updates.content_json !== undefined) { fields.push('content_json = ?'); vals.push(updates.content_json); }
      if (updates.settings_json !== undefined) { fields.push('settings_json = ?'); vals.push(updates.settings_json); }
      fields.push('updated_at = ?'); vals.push(Date.now());
      vals.push(userId, id);
      dbSync.prepare(`UPDATE projects SET ${fields.join(', ')} WHERE user_id = ? AND id = ?`).run(...vals);
      return dbSync.prepare('SELECT * FROM projects WHERE user_id = ? AND id = ?').get(userId, id);
    }
    const p = memoryStore.projects.find(x => x.user_id === userId && x.id === id);
    if (!p) return null;
    if (updates.name !== undefined) p.name = updates.name;
    if (updates.isFavorite !== undefined) p.is_favorite = updates.isFavorite ? 1 : 0;
    if (updates.isDraft !== undefined) p.is_draft = updates.isDraft ? 1 : 0;
    if (updates.content_json !== undefined) p.content_json = updates.content_json;
    if (updates.settings_json !== undefined) p.settings_json = updates.settings_json;
    p.updated_at = Date.now();
    saveJsonStore();
    return p;
  },

  deleteProject(userId, id) {
    if (isNativeSqlite) {
      dbSync.prepare('DELETE FROM projects WHERE user_id = ? AND id = ?').run(userId, id);
    } else {
      memoryStore.projects = memoryStore.projects.filter(x => !(x.user_id === userId && x.id === id));
      saveJsonStore();
    }
  },

  getFavorites(userId) {
    if (isNativeSqlite) {
      return dbSync.prepare('SELECT voice_id as voiceId, name, created_at as createdAt FROM favorite_voices WHERE user_id = ? ORDER BY created_at DESC').all(userId);
    }
    return memoryStore.favoriteVoices.filter(x => x.user_id === userId).map(x => ({ voiceId: x.voice_id, name: x.name, createdAt: x.created_at }));
  },

  addFavorite(userId, voiceId, name) {
    if (isNativeSqlite) {
      dbSync.prepare('INSERT OR REPLACE INTO favorite_voices (user_id, voice_id, name, created_at) VALUES (?, ?, ?, ?)').run(userId, voiceId, name, Date.now());
    } else {
      memoryStore.favoriteVoices = memoryStore.favoriteVoices.filter(x => !(x.user_id === userId && x.voice_id === voiceId));
      memoryStore.favoriteVoices.push({ user_id: userId, voice_id: voiceId, name, created_at: Date.now() });
      saveJsonStore();
    }
  },

  removeFavorite(userId, voiceId) {
    if (isNativeSqlite) {
      dbSync.prepare('DELETE FROM favorite_voices WHERE user_id = ? AND voice_id = ?').run(userId, voiceId);
    } else {
      memoryStore.favoriteVoices = memoryStore.favoriteVoices.filter(x => !(x.user_id === userId && x.voice_id === voiceId));
      saveJsonStore();
    }
  }
};

// --- Security Functions ---
function hashPassword(password) {
  const salt = crypto.randomBytes(16).toString('hex');
  const derived = crypto.scryptSync(password, salt, 64).toString('hex');
  return `${salt}:${derived}`;
}

function verifyPassword(password, storedHash) {
  if (!storedHash || !storedHash.includes(':')) return false;
  const [salt, key] = storedHash.split(':');
  const derived = crypto.scryptSync(password, salt, 64);
  const keyBuf = Buffer.from(key, 'hex');
  return crypto.timingSafeEqual(derived, keyBuf);
}

function hashToken(token) {
  return crypto.createHash('sha256').update(token).digest('hex');
}

function signAccessToken(user) {
  return jwt.sign(
    { sub: user.id, email: user.email, role: user.role, plan: user.plan },
    JWT_SECRET,
    { expiresIn: ACCESS_TOKEN_EXPIRY }
  );
}

function generateRefreshToken(userId) {
  const rawToken = crypto.randomBytes(32).toString('hex');
  const tokenHash = hashToken(rawToken);
  const expiresAt = Date.now() + 30 * 24 * 60 * 60 * 1000;
  db.saveRefreshToken(tokenHash, userId, expiresAt);
  return rawToken;
}

function sanitizeUser(u) {
  return {
    id: u.id,
    name: u.name,
    email: u.email,
    role: u.role,
    plan: u.plan,
    emailVerified: Boolean(u.email_verified),
    createdAt: u.created_at
  };
}

// --- Middlewares ---
app.use(cors());
app.use(express.json({ limit: '4mb' }));

function requireAuth(req, res, next) {
  const authHeader = req.headers['authorization'];
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      ok: false,
      error: { code: 'unauthenticated', message: 'Sign in to continue.' }
    });
  }

  const token = authHeader.substring(7);
  try {
    const payload = jwt.verify(token, JWT_SECRET);
    const user = db.findUserById(payload.sub);
    if (!user) {
      return res.status(401).json({
        ok: false,
        error: { code: 'unauthenticated', message: 'User account not found.' }
      });
    }
    req.user = sanitizeUser(user);
    next();
  } catch (err) {
    return res.status(401).json({
      ok: false,
      error: { code: 'session_expired', message: 'Session expired. Please sign in again.' }
    });
  }
}

// --- Main Router ---
const api = express.Router();

// 1. SIGN UP
api.post('/auth/signup', (req, res) => {
  const { name, email, password } = req.body || {};
  if (!name || name.trim().length === 0 || name.length > 80) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_name', message: 'Please enter your full name.' } });
  }
  if (!email || !email.includes('@')) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_email', message: 'Please enter a valid email address.' } });
  }
  if (!password || password.length < 8) {
    return res.status(400).json({ ok: false, error: { code: 'weak_password', message: 'Password must be at least 8 characters long.' } });
  }

  const existing = db.findUserByEmail(email);
  if (existing) {
    return res.status(409).json({ ok: false, error: { code: 'email_taken', message: 'An account with this email address already exists.' } });
  }

  const newUser = {
    id: 'usr_' + crypto.randomBytes(8).toString('hex'),
    name: name.trim(),
    email: email.trim().toLowerCase(),
    password_hash: hashPassword(password),
    role: 'user',
    plan: 'free',
    email_verified: 1,
    created_at: Date.now()
  };

  db.createUser(newUser);

  const accessToken = signAccessToken(newUser);
  const refreshToken = generateRefreshToken(newUser.id);
  const sanitized = sanitizeUser(newUser);

  res.status(200).json({
    ok: true,
    data: {
      verification: { required: false, emailDelivered: true, reason: 'auto_verified' },
      session: {
        accessToken,
        refreshToken,
        expiresIn: 7200,
        user: sanitized
      },
      user: sanitized
    }
  });
});

// 2. LOGIN
api.post('/auth/login', (req, res) => {
  const { email, password } = req.body || {};
  if (!email || !password) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_credentials', message: 'Email and password are required.' } });
  }

  const user = db.findUserByEmail(email);
  if (!user || !verifyPassword(password, user.password_hash)) {
    return res.status(401).json({ ok: false, error: { code: 'invalid_credentials', message: 'Incorrect email or password.' } });
  }

  const accessToken = signAccessToken(user);
  const refreshToken = generateRefreshToken(user.id);

  res.status(200).json({
    ok: true,
    data: {
      accessToken,
      refreshToken,
      expiresIn: 7200,
      user: sanitizeUser(user)
    }
  });
});

// 3. REFRESH TOKEN
api.post('/auth/refresh', (req, res) => {
  const { refreshToken } = req.body || {};
  if (!refreshToken) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_token', message: 'Missing refresh token.' } });
  }

  const tHash = hashToken(refreshToken);
  const record = db.findRefreshToken(tHash);

  if (!record || record.revoked || record.expires_at < Date.now()) {
    return res.status(401).json({ ok: false, error: { code: 'session_expired', message: 'Session expired. Please sign in again.' } });
  }

  db.revokeRefreshToken(tHash);

  const user = db.findUserById(record.user_id);
  if (!user) {
    return res.status(401).json({ ok: false, error: { code: 'session_expired', message: 'User not found.' } });
  }

  const nextAccessToken = signAccessToken(user);
  const nextRefreshToken = generateRefreshToken(user.id);

  res.status(200).json({
    ok: true,
    data: {
      accessToken: nextAccessToken,
      refreshToken: nextRefreshToken,
      expiresIn: 7200,
      user: sanitizeUser(user)
    }
  });
});

// 4. FORGOT PASSWORD
api.post('/auth/forgot-password', async (req, res) => {
  const { email } = req.body || {};
  if (!email || !email.includes('@')) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_email', message: 'Please enter a valid email address.' } });
  }

  const clean = email.trim().toLowerCase();
  const user = db.findUserByEmail(clean);

  // Generate 12-character secure alphanumeric token
  const resetToken = crypto.randomBytes(6).toString('hex').toUpperCase();
  const expiresAt = Date.now() + 60 * 60 * 1000;

  if (user) {
    db.savePasswordReset(resetToken, clean, expiresAt);
    console.log(`[AUTH] Password reset requested for ${clean}. Reset code: ${resetToken}`);

    if (MAIL_WEBHOOK_URL) {
      try {
        await fetch(MAIL_WEBHOOK_URL, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            to: clean,
            subject: 'AI Voice Studio Password Reset Code',
            text: `Your password reset code is: ${resetToken}. It will expire in 1 hour.`
          })
        });
      } catch (err) {
        console.error('Mail webhook error:', err.message);
      }
    }
  }

  res.status(200).json({
    ok: true,
    data: {
      requested: true,
      mailConfigured: Boolean(MAIL_WEBHOOK_URL)
    }
  });
});

// 5. RESET PASSWORD
api.post('/auth/reset-password', (req, res) => {
  const { token, password } = req.body || {};
  if (!token || !password) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_request', message: 'Token and new password are required.' } });
  }

  if (password.length < 8) {
    return res.status(400).json({ ok: false, error: { code: 'weak_password', message: 'Password must be at least 8 characters long.' } });
  }

  const cleanToken = token.trim().toUpperCase();
  const resetRecord = db.findPasswordReset(cleanToken);

  if (!resetRecord || resetRecord.used || resetRecord.expires_at < Date.now()) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_code', message: 'The reset code is invalid or has expired.' } });
  }

  const user = db.findUserByEmail(resetRecord.email);
  if (!user) {
    return res.status(404).json({ ok: false, error: { code: 'user_not_found', message: 'Account not found.' } });
  }

  const newHash = hashPassword(password);
  db.updateUserPassword(user.id, newHash);
  db.markResetUsed(cleanToken);
  db.revokeAllUserTokens(user.id);

  res.status(200).json({ ok: true, data: {} });
});

// 6. LOGOUT
api.post('/auth/logout', (req, res) => {
  const { refreshToken } = req.body || {};
  if (refreshToken) {
    db.revokeRefreshToken(hashToken(refreshToken));
  }
  res.status(200).json({ ok: true, data: {} });
});

// 7. VERIFY EMAIL
api.post('/auth/verify-email', (req, res) => {
  res.status(200).json({ ok: true, data: {} });
});

api.post('/auth/resend-verification', (req, res) => {
  res.status(200).json({
    ok: true,
    data: { requested: true, mailConfigured: Boolean(MAIL_WEBHOOK_URL) }
  });
});

// 8. CHANGE PASSWORD
api.post('/auth/change-password', requireAuth, (req, res) => {
  const { currentPassword, newPassword } = req.body || {};
  if (!currentPassword || !newPassword) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_request', message: 'Please provide both current and new passwords.' } });
  }

  const user = db.findUserById(req.user.id);
  if (!user || !verifyPassword(currentPassword, user.password_hash)) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_credentials', message: 'Current password is incorrect.' } });
  }

  if (newPassword.length < 8) {
    return res.status(400).json({ ok: false, error: { code: 'weak_password', message: 'New password must be at least 8 characters long.' } });
  }

  db.updateUserPassword(user.id, hashPassword(newPassword));
  db.revokeAllUserTokens(user.id);

  res.status(200).json({ ok: true, data: {} });
});

// 9. PROFILE & ACCOUNT
api.get('/me', requireAuth, (req, res) => {
  const user = db.findUserById(req.user.id);
  const counts = db.getUserCounts(req.user.id);
  res.status(200).json({
    ok: true,
    data: {
      user: sanitizeUser(user),
      counts
    }
  });
});

api.patch('/me', requireAuth, (req, res) => {
  const { name } = req.body || {};
  if (!name || name.trim().length === 0 || name.length > 80) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_name', message: 'Invalid name provided.' } });
  }

  db.updateUserName(req.user.id, name.trim());
  const updated = db.findUserById(req.user.id);
  const counts = db.getUserCounts(req.user.id);

  res.status(200).json({
    ok: true,
    data: {
      user: sanitizeUser(updated),
      counts
    }
  });
});

api.delete('/account', requireAuth, (req, res) => {
  const { password } = req.body || {};
  const user = db.findUserById(req.user.id);
  if (!user || !verifyPassword(password, user.password_hash)) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_password', message: 'Incorrect password.' } });
  }

  db.deleteUser(req.user.id);
  res.status(200).json({ ok: true, data: {} });
});

// 10. PROVIDERS
api.get('/providers', (req, res) => {
  res.status(200).json({
    ok: true,
    data: {
      defaultProvider: 'elevenlabs',
      providers: [
        { id: 'elevenlabs', label: 'ElevenLabs', configured: Boolean(ELEVENLABS_API_KEY), isDefault: true, outputFormats: ['mp3_44100_128', 'mp3_44100_64', 'opus_48000_128'], maxChars: 10000 }
      ]
    }
  });
});

// 11. VOICES
api.get('/voices', async (req, res) => {
  if (!ELEVENLABS_API_KEY) {
    return res.status(200).json({
      ok: true,
      data: {
        voices: [
          { voiceId: '21m00Tcm4TlvDq8ikWAM', name: 'Rachel', category: 'conversational', description: 'Calm, natural, and friendly.', previewUrl: 'https://storage.googleapis.com/eleven-public-prod/previews/voices/21m00Tcm4TlvDq8ikWAM/preview.mp3', labels: { accent: 'American', gender: 'Female' }, languages: [] },
          { voiceId: 'pNInz6obpgDQGcFmaJgB', name: 'Adam', category: 'narration', description: 'Deep, warm, and authoritative.', previewUrl: 'https://storage.googleapis.com/eleven-public-prod/previews/voices/pNInz6obpgDQGcFmaJgB/preview.mp3', labels: { accent: 'American', gender: 'Male' }, languages: [] },
          { voiceId: 'ErXwobaYiN019PkySvjV', name: 'Antoni', category: 'narration', description: 'Crisp, articulate, and expressive.', previewUrl: 'https://storage.googleapis.com/eleven-public-prod/previews/voices/ErXwobaYiN019PkySvjV/preview.mp3', labels: { accent: 'American', gender: 'Male' }, languages: [] }
        ],
        hasMore: false,
        nextPageToken: null,
        totalCount: 3
      }
    });
  }

  try {
    const upstream = await fetch('https://api.elevenlabs.io/v1/voices', {
      headers: { 'xi-api-key': ELEVENLABS_API_KEY }
    });
    if (!upstream.ok) {
      return res.status(upstream.status).json({
        ok: false,
        error: { code: 'provider_error', message: 'Failed to fetch voices from provider.' }
      });
    }

    const json = await upstream.json();
    const mapped = (json.voices || []).map(v => ({
      voiceId: v.voice_id,
      name: v.name,
      category: v.category || 'premade',
      description: (v.labels && v.labels.description) || '',
      previewUrl: v.preview_url || '',
      labels: v.labels || {},
      languages: []
    }));

    res.status(200).json({
      ok: true,
      data: {
        voices: mapped,
        hasMore: false,
        nextPageToken: null,
        totalCount: mapped.length
      }
    });
  } catch (err) {
    res.status(502).json({ ok: false, error: { code: 'provider_unreachable', message: err.message } });
  }
});

// 12. MODELS
api.get('/models', (req, res) => {
  res.status(200).json({
    ok: true,
    data: {
      provider: 'elevenlabs',
      models: [
        { modelId: 'eleven_multilingual_v2', name: 'Eleven Multilingual v2', description: 'Ultra-realistic lifelike speech.', maxCharacters: 10000, canUseStyle: true, canUseSpeakerBoost: true, languages: [] },
        { modelId: 'eleven_turbo_v2_5', name: 'Eleven Turbo v2.5', description: 'Fast low-latency synthesis.', maxCharacters: 10000, canUseStyle: false, canUseSpeakerBoost: true, languages: [] },
        { modelId: 'eleven_monolingual_v1', name: 'Eleven English v1', description: 'Classic English synthesis.', maxCharacters: 5000, canUseStyle: false, canUseSpeakerBoost: false, languages: [] }
      ],
      outputFormats: [
        'mp3_44100_128',
        'mp3_44100_96',
        'mp3_44100_64',
        'opus_48000_128',
        'opus_48000_64'
      ],
      capabilities: {
        stability: true,
        similarity: true,
        style: true,
        speakerBoost: true,
        speed: true,
        speedMin: 0.7,
        speedMax: 1.2
      },
      maxTextChars: 10000
    }
  });
});

// 13. TTS SYNTHESIS
api.post('/tts', requireAuth, async (req, res) => {
  const { text, voiceId, modelId, outputFormat, voiceSettings, projectId } = req.body || {};

  if (!text || text.trim().length === 0) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_text', message: 'Script text cannot be empty.' } });
  }
  if (!voiceId) {
    return res.status(400).json({ ok: false, error: { code: 'invalid_voice', message: 'Voice selection is required.' } });
  }

  const { used } = db.getMonthlyCharsUsed(req.user.id);
  const requestedChars = text.length;
  if (used + requestedChars > FREE_MONTHLY_CHARS) {
    return res.status(402).json({
      ok: false,
      error: { code: 'usage_limit_exceeded', message: 'Monthly character limit reached.' }
    });
  }

  if (!ELEVENLABS_API_KEY) {
    return res.status(503).json({
      ok: false,
      error: { code: 'provider_not_configured', message: 'ElevenLabs API key is not configured on the backend.' }
    });
  }

  const selectedModel = modelId || 'eleven_multilingual_v2';
  const selectedFormat = outputFormat || 'mp3_44100_128';

  const bodyPayload = {
    text,
    model_id: selectedModel,
    voice_settings: voiceSettings ? {
      stability: voiceSettings.stability ?? 0.5,
      similarity_boost: voiceSettings.similarityBoost ?? 0.75,
      style: voiceSettings.style ?? 0.0,
      use_speaker_boost: voiceSettings.useSpeakerBoost ?? true
    } : {
      stability: 0.5,
      similarity_boost: 0.75,
      style: 0.0,
      use_speaker_boost: true
    }
  };

  try {
    const upstream = await fetch(`https://api.elevenlabs.io/v1/text-to-speech/${voiceId}?output_format=${selectedFormat}`, {
      method: 'POST',
      headers: {
        'xi-api-key': ELEVENLABS_API_KEY,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(bodyPayload)
    });

    if (!upstream.ok) {
      const errText = await upstream.text();
      return res.status(upstream.status).json({
        ok: false,
        error: { code: 'provider_error', message: errText || 'ElevenLabs synthesis failed.' }
      });
    }

    const genId = 'gen_' + crypto.randomBytes(8).toString('hex');
    const assetId = 'ast_' + crypto.randomBytes(8).toString('hex');
    const ext = selectedFormat.startsWith('opus') ? 'opus' : 'mp3';
    const filePath = path.join(AUDIO_DIR, `${assetId}.${ext}`);

    const buffer = Buffer.from(await upstream.arrayBuffer());
    fs.writeFileSync(filePath, buffer);

    db.saveGeneration({
      id: genId,
      user_id: req.user.id,
      project_id: projectId || null,
      provider: 'elevenlabs',
      model: selectedModel,
      voice_id: voiceId,
      text_preview: text.length > 80 ? text.substring(0, 80) + '…' : text,
      text,
      output_format: selectedFormat,
      status: 'completed',
      audio_asset_id: assetId,
      char_count: requestedChars,
      duration_ms: null,
      bytes: buffer.length,
      settings_json: JSON.stringify(bodyPayload.voice_settings),
      created_at: Date.now(),
      completed_at: Date.now()
    });

    const remaining = Math.max(0, FREE_MONTHLY_CHARS - (used + requestedChars));
    res.setHeader('Content-Type', selectedFormat.startsWith('opus') ? 'audio/ogg' : 'audio/mpeg');
    res.setHeader('X-Generation-Id', genId);
    res.setHeader('X-Audio-Asset-Id', assetId);
    res.setHeader('X-Usage-Remaining', remaining.toString());
    res.send(buffer);

  } catch (err) {
    res.status(502).json({ ok: false, error: { code: 'provider_error', message: err.message } });
  }
});

// 14. USAGE
api.get('/usage', requireAuth, (req, res) => {
  const { used, count } = db.getMonthlyCharsUsed(req.user.id);
  const monthStart = new Date(); monthStart.setDate(1); monthStart.setHours(0, 0, 0, 0);
  const nextMonth = new Date(monthStart); nextMonth.setMonth(nextMonth.getMonth() + 1);

  res.status(200).json({
    ok: true,
    data: {
      plan: req.user.plan,
      note: 'Monthly Studio Quota',
      limitChars: FREE_MONTHLY_CHARS,
      usedChars: used,
      remainingChars: Math.max(0, FREE_MONTHLY_CHARS - used),
      periodStart: monthStart.getTime(),
      periodEnd: nextMonth.getTime(),
      generationsThisPeriod: count
    }
  });
});

// 15. AUDIO RETRIEVAL
api.get('/audio/:id', requireAuth, (req, res) => {
  const assetId = req.params.id;
  const mp3 = path.join(AUDIO_DIR, `${assetId}.mp3`);
  const opus = path.join(AUDIO_DIR, `${assetId}.opus`);
  const target = fs.existsSync(mp3) ? mp3 : fs.existsSync(opus) ? opus : null;

  if (!target) {
    return res.status(404).json({ ok: false, error: { code: 'audio_not_found', message: 'Audio file not found.' } });
  }

  const stat = fs.statSync(target);
  const range = req.headers.range;
  const contentType = target.endsWith('.opus') ? 'audio/ogg' : 'audio/mpeg';

  if (range) {
    const parts = range.replace(/bytes=/, '').split('-');
    const start = parseInt(parts[0], 10);
    const end = parts[1] ? parseInt(parts[1], 10) : stat.size - 1;
    res.writeHead(206, {
      'Content-Range': `bytes ${start}-${end}/${stat.size}`,
      'Accept-Ranges': 'bytes',
      'Content-Length': end - start + 1,
      'Content-Type': contentType
    });
    fs.createReadStream(target, { start, end }).pipe(res);
  } else {
    res.writeHead(200, {
      'Content-Length': stat.size,
      'Content-Type': contentType
    });
    fs.createReadStream(target).pipe(res);
  }
});

api.patch('/audio/:id', requireAuth, (req, res) => {
  const { durationMs } = req.body || {};
  if (durationMs) {
    db.updateGenerationDuration(req.params.id, durationMs);
  }
  res.status(200).json({ ok: true, data: {} });
});

// 16. GENERATIONS
api.get('/generations', requireAuth, (req, res) => {
  const { q, limit, offset } = req.query;
  const { rows, total } = db.getGenerations(req.user.id, q, parseInt(limit, 10), parseInt(offset, 10));

  res.status(200).json({
    ok: true,
    data: {
      generations: rows.map(r => ({
        id: r.id,
        projectId: r.project_id,
        provider: r.provider,
        model: r.model,
        voiceId: r.voice_id,
        textPreview: r.text_preview,
        text: r.text,
        outputFormat: r.output_format,
        status: r.status,
        audioAssetId: r.audio_asset_id,
        charCount: r.char_count,
        durationMs: r.duration_ms,
        bytes: r.bytes,
        settings: r.settings_json ? JSON.parse(r.settings_json) : null,
        createdAt: r.created_at,
        completedAt: r.completed_at
      })),
      total,
      limit: parseInt(limit, 10) || 25,
      offset: parseInt(offset, 10) || 0
    }
  });
});

api.delete('/generations/:id', requireAuth, (req, res) => {
  db.deleteGeneration(req.user.id, req.params.id);
  res.status(200).json({ ok: true, data: {} });
});

// 17. PROJECTS
api.get('/projects', requireAuth, (req, res) => {
  const { q, favorite, limit, offset } = req.query;
  const favBool = favorite === 'true' ? true : favorite === 'false' ? false : null;
  const { rows, total } = db.getProjects(req.user.id, q, favBool, parseInt(limit, 10), parseInt(offset, 10));

  res.status(200).json({
    ok: true,
    data: {
      projects: rows.map(p => ({
        id: p.id,
        name: p.name,
        type: p.type,
        isFavorite: Boolean(p.is_favorite),
        isDraft: Boolean(p.is_draft),
        content: p.content_json ? JSON.parse(p.content_json) : null,
        settings: p.settings_json ? JSON.parse(p.settings_json) : null,
        createdAt: p.created_at,
        updatedAt: p.updated_at,
        audioCount: 1
      })),
      total,
      limit: parseInt(limit, 10) || 25,
      offset: parseInt(offset, 10) || 0
    }
  });
});

api.post('/projects', requireAuth, (req, res) => {
  const { name, type, content, settings, isDraft } = req.body || {};
  const proj = {
    id: 'prj_' + crypto.randomBytes(8).toString('hex'),
    user_id: req.user.id,
    name: name || 'Untitled Project',
    type: type || 'tts',
    is_favorite: 0,
    is_draft: isDraft ? 1 : 0,
    content_json: content ? JSON.stringify(content) : null,
    settings_json: settings ? JSON.stringify(settings) : null,
    created_at: Date.now(),
    updated_at: Date.now()
  };

  db.createProject(proj);
  res.status(200).json({
    ok: true,
    data: {
      project: {
        id: proj.id,
        name: proj.name,
        type: proj.type,
        isFavorite: false,
        isDraft: Boolean(proj.is_draft),
        content,
        settings,
        createdAt: proj.created_at,
        updatedAt: proj.updated_at
      }
    }
  });
});

api.patch('/projects/:id', requireAuth, (req, res) => {
  const updated = db.updateProject(req.user.id, req.params.id, req.body || {});
  if (!updated) {
    return res.status(404).json({ ok: false, error: { code: 'not_found', message: 'Project not found.' } });
  }
  res.status(200).json({
    ok: true,
    data: {
      project: {
        id: updated.id,
        name: updated.name,
        type: updated.type,
        isFavorite: Boolean(updated.is_favorite),
        isDraft: Boolean(updated.is_draft),
        content: updated.content_json ? JSON.parse(updated.content_json) : null,
        settings: updated.settings_json ? JSON.parse(updated.settings_json) : null,
        createdAt: updated.created_at,
        updatedAt: updated.updated_at
      }
    }
  });
});

api.delete('/projects/:id', requireAuth, (req, res) => {
  db.deleteProject(req.user.id, req.params.id);
  res.status(200).json({ ok: true, data: {} });
});

// 18. FAVORITES
api.get('/favorites/voices', requireAuth, (req, res) => {
  const voices = db.getFavorites(req.user.id);
  res.status(200).json({ ok: true, data: { voices } });
});

api.put('/favorites/voices/:id', requireAuth, (req, res) => {
  const name = req.body && req.body.name ? req.body.name : req.params.id;
  db.addFavorite(req.user.id, req.params.id, name);
  res.status(200).json({ ok: true, data: {} });
});

api.delete('/favorites/voices/:id', requireAuth, (req, res) => {
  db.removeFavorite(req.user.id, req.params.id);
  res.status(200).json({ ok: true, data: {} });
});

// --- Register All Route Prefixes ---
app.use('/v1', api);
app.use('/api/v1', api);
app.use('/api', api);

// Health check
app.get('/healthz', (req, res) => {
  res.status(200).json({ ok: true, status: 'healthy', providerConfigured: Boolean(ELEVENLABS_API_KEY) });
});

app.get('/', (req, res) => {
  res.status(200).send('AI Voice Studio Backend is running.');
});

// 404 Handler
app.use((req, res) => {
  res.status(404).json({
    ok: false,
    error: {
      code: 'not_found',
      message: `The endpoint '${req.method} ${req.originalUrl}' does not exist.`
    }
  });
});

// Start Server
app.listen(PORT, '0.0.0.0', () => {
  console.log(`AI Voice Studio Backend running on port ${PORT}`);
  console.log(`ElevenLabs Key Configured: ${Boolean(ELEVENLABS_API_KEY)}`);
});
