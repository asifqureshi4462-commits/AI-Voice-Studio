package com.asifqureshi.aivoicestudio.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.R;
import com.asifqureshi.aivoicestudio.data.models.UserProfile;
import com.asifqureshi.aivoicestudio.data.repository.AuthRepository;
import com.asifqureshi.aivoicestudio.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        MaterialButton btnDemoLogin = findViewById(R.id.btnDemoLogin);
        MaterialButton btnGuest = findViewById(R.id.btnGuest);

        findViewById(R.id.btnForgotPassword).setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        findViewById(R.id.btnGoToSignup).setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            boolean remember = cbRememberMe.isChecked();

            authRepository.login(email, pass, remember, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(UserProfile profile) {
                    Toast.makeText(LoginActivity.this, "Welcome, " + profile.getName() + "!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        btnDemoLogin.setOnClickListener(v -> {
            etEmail.setText("asifqureshi4462@gmail.com");
            etPassword.setText("studio2026");
            btnSignIn.performClick();
        });

        btnGuest.setOnClickListener(v -> {
            authRepository.loginAsGuest(new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(UserProfile profile) {
                    Toast.makeText(LoginActivity.this, "Signed in as Studio Guest", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
