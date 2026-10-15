# AI Voice Studio — Professional ElevenLabs AI Text-to-Speech Application
**Developer & Architecture:** Asif Qureshi  
**Platform:** Native Android (Java + XML) & Node.js Backend  
**Package:** `com.asifqureshi.aivoicestudio`  
**Audio Engine:** AndroidX Media3 (ExoPlayer)  
**AI Voice Engine:** ElevenLabs Voice API  

---

## 🚀 Overview

**AI Voice Studio** is a full-featured, production-ready AI speech generation and audio production suite inspired by ElevenLabs. Built natively in **Java and XML** following clean **MVVM + Repository** architectural patterns, it delivers ultra-realistic speech synthesis, multi-speaker dialogue scenes, long-form audiobook narrations, real-time waveform visualizers, voice discovery, audio trimming/editing, and authorized instant voice cloning workflows.

---

## 🌟 Core Features

### 1. Splash & Onboarding
- Branded launch screen with animated waveform logo and tagline.
- 3-step interactive onboarding explaining ElevenLabs neural voice models, 29+ languages, and creator audio studio tools.
- Intelligent session routing (`Onboarding` → `Login` → `Home Dashboard`).

### 2. Authentication System
- Secure email/password login & registration with client-side field validation.
- "Remember Me" session persistence and Password Reset workflow.
- Demo Quick-Login and Guest Studio Mode for instant testing.
- Legal consent and Terms of Service agreements.

### 3. Studio Dashboard (`HomeFragment`)
- Personalized greeting and real-time character quota meter (`14,250 / 100,000 chars used`).
- Quick Studio shortcuts:
  - **Speech Studio:** Instant script-to-speech conversion.
  - **Dialogue Studio:** Multi-character scene synthesis.
  - **Long Narration:** Large script paragraph segmentation and batch processing.
  - **Audio Studio:** Waveform playback, trimming, and music mixing.
  - **Voice Cloning:** AI voice design and authorized instant voice cloning.
- Recent audio generations list with quick playback and favorite actions.

### 4. Text-to-Speech Studio (`TtsFragment`)
- Real-time character counter (up to 5,000 characters per request).
- Built-in script templates: *Audiobook Narration, Commercial Promo, Podcast Intro, Tech Tutorial*.
- External `.txt` script import via Android Storage Access Framework.
- Selected Voice selector and **Voice Settings Bottom Sheet**:
  - **Stability Slider:** (0% to 100%)
  - **Similarity & Clarity Boost:** (0% to 100%)
  - **Style Exaggeration:** (0% to 100%)
  - **Playback Speed:** (0.5x to 2.0x)
  - **Speaker Boost Toggle:** Enhance audio presence
  - **Models:** `eleven_multilingual_v2`, `eleven_turbo_v2_5`, `eleven_monolingual_v1`
- Generated Audio Player card:
  - Real-time animated **Waveform Visualizer** (`WaveformVisualizerView`)
  - Interactive scrubbing seek bar
  - One-tap MP3 download to device Downloads folder
  - Android Sharesheet integration
  - "Studio Edit" shortcut to trim and mix in Audio Studio

### 5. Voice Discovery Library (`VoicesFragment`)
- Search by voice name, accent, or style description.
- Filter chips: *All, Narration, Conversational, News, Characters, Social Media*.
- Real audio streaming previews with play/pause state management.
- Persistent favorite toggles.
- One-tap "Use Voice" to select speaker for generation.

### 6. Multi-Speaker Dialogue Studio (`DialogueActivity`)
- Pre-configured dual-speaker conversation (Speaker A & Speaker B).
- Add unlimited custom speakers with individual voice assignments.
- Line-by-line script editor with individual speech previews.
- **Synthesize Full Dialogue:** Automatically generates each speaker line in sequence and merges them into a unified cinematic MP3 master track.

### 7. Long Script Narration Studio (`LongScriptActivity`)
- Handles long scripts (5,000 to 50,000+ characters) for YouTube, audiobooks, and courses.
- Intelligent paragraph and sentence boundary segmentation into balanced chunks.
- Batch generation queue with visual state tracking (*Pending, Generating, Completed, Error*).
- Automatic audio stitching of all completed chunks into a single seamless master audio file.

### 8. Audio Studio & Trimmer (`AudioStudioActivity`)
- Waveform scrubber with millisecond precision.
- Dual-slider trimming (Trim Start and Trim End points).
- Background music volume mixer overlay.
- High-fidelity master audio export.

### 9. Voice Design & Authorized Cloning (`VoiceCloningActivity`)
- **Voice Design:** Prompt-based voice synthesis using ElevenLabs generative models.
- **Authorized Voice Cloning:** Explicit legal consent checkbox requiring verified permission before uploading voice samples to prevent unauthorized impersonation.

### 10. Project Library & History (`LibraryFragment`)
- Tabbed view: *All Projects* and *Favorites*.
- SQLite database persistence with project metadata (title, voice, file size, duration, date).
- Delete, play, and share actions.

### 11. Profile & Settings & Admin Console
- Profile details, subscription tier badge, and quota breakdown.
- **Settings:** Backend server URL configuration, ElevenLabs direct API key entry, audio cache clearer.
- **Admin Console:** Provider health status, API telemetry, generation counts, and latency statistics.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Platform** | Native Android (API 26+) |
| **Language** | Java 17 |
| **UI Framework** | Android XML Layouts + Google Material Design 3 |
| **Architecture** | MVVM + Repository Pattern |
| **Audio Playback** | AndroidX Media3 (ExoPlayer 1.5.1) |
| **Networking** | Retrofit 2.12 + OkHttp 4.10 + Gson |
| **Persistence** | SQLiteOpenHelper + SharedPreferences |
| **Backend** | Node.js + Express + Rate-Limiter |
| **CI/CD** | GitHub Actions (`.github/workflows/android.yml`) |

---

## 📦 How to Build the Project

### Prerequisites
- Android Studio Ladybug / Meerkat or later
- JDK 17
- Android SDK API 36 (Minimum API 26)

### Building via Command Line
```bash
# Run Unit Tests
gradle testDebugUnitTest

# Assemble Debug APK
gradle assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🌐 Backend Setup

1. Open a terminal in `/backend`:
   ```bash
   cd backend
   npm install
   ```
2. Copy `.env.example` to `.env` and configure your ElevenLabs API key:
   ```bash
   cp .env.example .env
   ```
3. Start the server:
   ```bash
   npm start
   ```
4. In the Android app Settings, enter `http://10.0.2.2:5000/api/` (for emulator) or your live backend URL.

---

## 🤖 GitHub Actions Workflow

The automated build workflow is configured in `.github/workflows/android.yml`.
On every push, it:
1. Checks out repository code.
2. Sets up JDK 17 with Gradle caching.
3. Runs all unit tests (`testDebugUnitTest`).
4. Compiles the debug APK (`assembleDebug`).
5. Uploads the generated APK as an artifact named **`ai-voice-studio-apk`**.

---

## 📄 License & Credits
Developed by **Asif Qureshi**.  
Voice synthesis powered by **ElevenLabs API**.
