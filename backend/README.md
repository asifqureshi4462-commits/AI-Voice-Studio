# AI Voice Studio Backend Server
**Developer:** Asif Qureshi  
**Platform:** Node.js / Express  
**Purpose:** Secure proxy & authentication gateway for ElevenLabs AI Voice Synthesis.

---

## Architecture & Security Highlights

1. **Zero Secret Leakage:** The ElevenLabs API key remains strictly on this backend server. Client APKs connect to `/api/v1/...` and never need embedded keys.
2. **Rate Limiting:** Built-in `express-rate-limit` prevents API quota exhaustion and DDoS attacks.
3. **Audio Streaming:** Seamlessly pipes high-fidelity MPEG audio stream directly from ElevenLabs to the Android app.
4. **Direct Mode Fallback:** Android client also supports direct testing if an individual developer enters their own API key in Settings.

---

## Setup & Running Locally

### 1. Install Dependencies
```bash
cd backend
npm install
```

### 2. Configure Environment Variables
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Fill in your `ELEVENLABS_API_KEY`:
```env
PORT=5000
ELEVENLABS_API_KEY=your_elevenlabs_api_key_here
```

### 3. Start the Server
```bash
npm start
```
The server will start on `http://localhost:5000`.

### 4. Connect with Android App
- If running on Android Studio Emulator: set backend URL to `http://10.0.2.2:5000/api/` in app Settings.
- If deployed to Render, Railway, Heroku, or VPS: set backend URL to `https://your-domain.com/api/`.
