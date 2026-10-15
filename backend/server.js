const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const rateLimit = require('express-rate-limit');
const fetch = require('node-fetch');

dotenv.config();

const app = express();
const PORT = process.env.PORT || 5000;
const ELEVENLABS_API_KEY = process.env.ELEVENLABS_API_KEY || '';
const ELEVENLABS_BASE_URL = 'https://api.elevenlabs.io';

// Middleware
app.use(cors());
app.use(express.json());

// Rate Limiting to prevent API quota exhaustion
const apiLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, // Limit each IP to 100 requests per window
    standardHeaders: true,
    legacyHeaders: false,
    message: { error: 'Too many requests. Please try again later.' }
});

app.use('/api/', apiLimiter);

// Health Check
app.get('/api/health', (req, res) => {
    res.json({
        status: 'online',
        service: 'AI Voice Studio Backend',
        developer: 'Asif Qureshi',
        elevenlabsConfigured: !!ELEVENLABS_API_KEY,
        timestamp: new Date().toISOString()
    });
});

// Authentication Proxy (Mock / DB integration)
app.post('/api/auth/login', (req, res) => {
    const { email, password } = req.body;
    if (!email || !password) {
        return res.status(400).json({ error: 'Email and password required' });
    }
    // Return authenticated studio session
    res.json({
        token: 'auth_jwt_studio_' + Date.now(),
        user: {
            userId: 'usr_' + Date.now(),
            name: email.split('@')[0],
            email: email,
            planName: 'Pro Studio Plan',
            charactersUsed: 14250,
            charactersLimit: 100000
        }
    });
});

// Get Voices from ElevenLabs
app.get('/api/v1/voices', async (req, res) => {
    const apiKey = req.headers['xi-api-key'] || ELEVENLABS_API_KEY;
    if (!apiKey) {
        return res.status(401).json({ error: 'ElevenLabs API key not configured on server or in client request.' });
    }

    try {
        const response = await fetch(`${ELEVENLABS_BASE_URL}/v1/voices`, {
            headers: { 'xi-api-key': apiKey }
        });

        if (!response.ok) {
            const errorText = await response.text();
            return res.status(response.status).json({ error: errorText });
        }

        const data = await response.json();
        res.json(data);
    } catch (err) {
        console.error('Error fetching voices:', err);
        res.status(500).json({ error: 'Failed to contact ElevenLabs service: ' + err.message });
    }
});

// Generate Text-to-Speech
app.post('/api/v1/text-to-speech/:voice_id', async (req, res) => {
    const { voice_id } = req.params;
    const { text, model_id, voice_settings } = req.body;
    const outputFormat = req.query.output_format || 'mp3_44100_128';

    const apiKey = req.headers['xi-api-key'] || ELEVENLABS_API_KEY;
    if (!apiKey) {
        return res.status(401).json({ error: 'ElevenLabs API key not configured on server or in client request.' });
    }

    if (!text || text.trim().length === 0) {
        return res.status(400).json({ error: 'Text prompt cannot be empty.' });
    }

    try {
        const response = await fetch(`${ELEVENLABS_BASE_URL}/v1/text-to-speech/${voice_id}?output_format=${outputFormat}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'xi-api-key': apiKey
            },
            body: JSON.stringify({
                text: text,
                model_id: model_id || 'eleven_multilingual_v2',
                voice_settings: voice_settings || {
                    stability: 0.5,
                    similarity_boost: 0.75,
                    style: 0.0,
                    use_speaker_boost: true
                }
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            return res.status(response.status).json({ error: errorText });
        }

        // Stream audio bytes back to client
        res.setHeader('Content-Type', 'audio/mpeg');
        response.body.pipe(res);
    } catch (err) {
        console.error('TTS Generation error:', err);
        res.status(500).json({ error: 'Generation failed: ' + err.message });
    }
});

// Telemetry & Subscription info
app.get('/api/v1/user/subscription', async (req, res) => {
    const apiKey = req.headers['xi-api-key'] || ELEVENLABS_API_KEY;
    if (!apiKey) {
        return res.status(401).json({ error: 'ElevenLabs API key not configured.' });
    }

    try {
        const response = await fetch(`${ELEVENLABS_BASE_URL}/v1/user/subscription`, {
            headers: { 'xi-api-key': apiKey }
        });

        const data = await response.json();
        res.json(data);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.listen(PORT, () => {
    console.log(`AI Voice Studio Backend running on http://localhost:${PORT}`);
    console.log(`Developer: Asif Qureshi`);
});
