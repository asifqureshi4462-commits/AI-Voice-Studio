package com.asifqureshi.aivoicestudio.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.R;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TextView tvPageIndicator;
    private MaterialButton btnNext;
    private PreferencesManager prefs;

    private static class SlideItem {
        final int iconRes;
        final String title;
        final String desc;

        SlideItem(int iconRes, String title, String desc) {
            this.iconRes = iconRes;
            this.title = title;
            this.desc = desc;
        }
    }

    private SlideItem[] slides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        prefs = new PreferencesManager(this);

        slides = new SlideItem[]{
                new SlideItem(R.drawable.ic_wave, getString(R.string.onboarding_title_1), getString(R.string.onboarding_desc_1)),
                new SlideItem(R.drawable.ic_voice, getString(R.string.onboarding_title_2), getString(R.string.onboarding_desc_2)),
                new SlideItem(R.drawable.ic_sparkles, getString(R.string.onboarding_title_3), getString(R.string.onboarding_desc_3))
        };

        viewPager = findViewById(R.id.viewPagerOnboarding);
        tvPageIndicator = findViewById(R.id.tvPageIndicator);
        btnNext = findViewById(R.id.btnNext);
        TextView btnSkip = findViewById(R.id.btnSkip);

        viewPager.setAdapter(new OnboardingAdapter());
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tvPageIndicator.setText(String.format("%d / %d", position + 1, slides.length));
                if (position == slides.length - 1) {
                    btnNext.setText(R.string.onboarding_get_started);
                } else {
                    btnNext.setText(R.string.onboarding_next);
                }
            }
        });

        btnSkip.setOnClickListener(v -> completeOnboarding());

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < slides.length - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                completeOnboarding();
            }
        });
    }

    private void completeOnboarding() {
        prefs.setOnboardingCompleted(true);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder> {
        @NonNull
        @Override
        public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(OnboardingActivity.this).inflate(R.layout.item_onboarding_slide, parent, false);
            return new SlideViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
            SlideItem item = slides[position];
            holder.ivIcon.setImageResource(item.iconRes);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.desc);
        }

        @Override
        public int getItemCount() {
            return slides.length;
        }

        class SlideViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvTitle, tvDesc;

            public SlideViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.ivSlideIcon);
                tvTitle = itemView.findViewById(R.id.tvSlideTitle);
                tvDesc = itemView.findViewById(R.id.tvSlideDesc);
            }
        }
    }
}
