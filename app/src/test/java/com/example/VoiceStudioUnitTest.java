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
}
