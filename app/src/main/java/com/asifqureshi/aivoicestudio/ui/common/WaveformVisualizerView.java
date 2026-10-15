package com.asifqureshi.aivoicestudio.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class WaveformVisualizerView extends View {
    private Paint activePaint;
    private Paint inactivePaint;
    private float progress = 0.0f; // 0.0 to 1.0
    private boolean isPlaying = false;
    private final float[] barHeights = new float[]{
            0.3f, 0.5f, 0.7f, 0.4f, 0.9f, 0.6f, 0.8f, 0.35f,
            0.65f, 0.85f, 0.45f, 0.95f, 0.55f, 0.75f, 0.3f, 0.6f,
            0.85f, 0.5f, 0.7f, 0.9f, 0.4f, 0.8f, 0.6f, 0.35f,
            0.75f, 0.55f, 0.85f, 0.45f, 0.65f, 0.3f, 0.5f, 0.7f
    };
    private final RectF rect = new RectF();
    private float animPhase = 0.0f;

    public WaveformVisualizerView(Context context) {
        super(context);
        init();
    }

    public WaveformVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveformVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activePaint.setColor(Color.parseColor("#00E5FF")); // Electric Cyan

        inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        inactivePaint.setColor(Color.parseColor("#26334D")); // Dark Slate
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
        invalidate();
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        if (playing) {
            animPhase += 0.2f;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        int numBars = barHeights.length;
        float barWidth = (width / (float) numBars) * 0.65f;
        float spacing = (width - (numBars * barWidth)) / (float) (numBars - 1);
        float centerY = height / 2.0f;

        for (int i = 0; i < numBars; i++) {
            float barProgress = (float) i / (float) numBars;
            Paint paint = (barProgress <= progress) ? activePaint : inactivePaint;

            float dynamicScale = barHeights[i];
            if (isPlaying) {
                // Subtle breathing animation during active playback
                dynamicScale += (float) (Math.sin(animPhase + i * 0.4) * 0.15f);
                dynamicScale = Math.max(0.2f, Math.min(1.0f, dynamicScale));
            }

            float barHeight = Math.max(8f, height * dynamicScale * 0.85f);
            float left = i * (barWidth + spacing);
            float top = centerY - (barHeight / 2.0f);
            float right = left + barWidth;
            float bottom = centerY + (barHeight / 2.0f);

            rect.set(left, top, right, bottom);
            canvas.drawRoundRect(rect, barWidth / 2.0f, barWidth / 2.0f, paint);
        }

        if (isPlaying) {
            animPhase += 0.08f;
            postInvalidateDelayed(50);
        }
    }
}
