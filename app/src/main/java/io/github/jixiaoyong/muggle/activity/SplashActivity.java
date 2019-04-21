package io.github.jixiaoyong.muggle.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.utils.AppUtils;

public class SplashActivity extends AppCompatActivity {
    @BindView(R.id.splash_layout)
    RelativeLayout splashLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setFillAfter(true);
        animation.setDuration(1000);
        splashLayout.startAnimation(animation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1500);

        AppUtils.setLightMode(this, true);

    }

}
