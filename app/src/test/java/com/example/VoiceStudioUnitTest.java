package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.asifqureshi.aivoicestudio.audio.AudioProcessingUtils;
import com.asifqureshi.aivoicestudio.data.models.Voice;
import com.asifqureshi.aivoicestudio.data.models.VoiceSettings;
import org.junit.Test;

public class VoiceStudioUnitTest {

    @Test
    public void testDurationFormatting() {
        assertEquals("00:00", AudioProcessingUtils.formatDuration(0));
        assertEquals("00:24", AudioProcessingUtils.formatDuration(24000));
        assertEquals("01:05", AudioProcessingUtils.formatDuration(65000));
        assertEquals("1:01:05", AudioProcessingUtils.formatDuration(3665000));
    }

    @Test
    public void testFileSizeFormatting() {
        assertEquals("0 B", AudioProcessingUtils.formatFileSize(0));
        assertEquals("500 B", AudioProcessingUtils.formatFileSize(500));
        assertEquals("1.0 KB", AudioProcessingUtils.formatFileSize(1024));
        assertEquals("1.0 MB", AudioProcessingUtils.formatFileSize(1024 * 1024));
    }

    @Test
    public void testVoiceModel() {
        Voice voice = new Voice("21m00Tcm4TlvDq8ikWAM", "Rachel", "Conversational", "American", "Female", "Calm", "https://preview.mp3", "English (US)");
        assertEquals("21m00Tcm4TlvDq8ikWAM", voice.getVoiceId());
        assertEquals("Rachel", voice.getName());
        assertEquals("American", voice.getAccent());
        assertNotNull(voice.getCategory());
    }

    @Test
    public void testVoiceSettingsClamping() {
        VoiceSettings settings = new VoiceSettings();
        settings.setStability(1.5f);
        assertTrue(settings.getStability() <= 1.0f);

        settings.setSpeed(3.0f);
        assertTrue(settings.getSpeed() <= 2.0f);

        settings.setSpeed(0.1f);
        assertTrue(settings.getSpeed() >= 0.5f);
    }

    @Test
    public void testElevenLabsServiceMethods() throws NoSuchMethodException {
        Class<?> serviceClass = com.asifqureshi.aivoicestudio.data.network.ElevenLabsService.class;
        assertTrue(serviceClass.isInterface());
        assertNotNull(serviceClass.getMethod("generateTextToSpeech", String.class, String.class, String.class, com.asifqureshi.aivoicestudio.data.models.GenerationRequest.class));
        assertNotNull(serviceClass.getMethod("generateSpeech", String.class, String.class, com.asifqureshi.aivoicestudio.data.models.GenerationRequest.class));
    }

    @Test
    public void testOpenRouterRequestModel() {
        com.asifqureshi.aivoicestudio.data.models.OpenRouterRequest req = new com.asifqureshi.aivoicestudio.data.models.OpenRouterRequest("deepgram/flux-tts:free", "Hello OpenRouter", "aura-asteria-en", "mp3");
        assertEquals("deepgram/flux-tts:free", req.getModel());
        assertEquals("Hello OpenRouter", req.getInput());
        assertEquals("aura-asteria-en", req.getVoice());
        assertEquals("mp3", req.getResponseFormat());
    }

    @Test
    public void testOpenRouterServiceMethods() throws NoSuchMethodException {
        Class<?> serviceClass = com.asifqureshi.aivoicestudio.data.network.OpenRouterService.class;
        assertTrue(serviceClass.isInterface());
        assertNotNull(serviceClass.getMethod("generateOpenRouterSpeech", String.class, com.asifqureshi.aivoicestudio.data.models.OpenRouterRequest.class));
    }
}
