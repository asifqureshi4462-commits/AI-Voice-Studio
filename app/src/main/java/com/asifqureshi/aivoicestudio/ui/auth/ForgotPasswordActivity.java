package com.asifqureshi.aivoicestudio.ui.auth;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        TextInputEditText etEmail = findViewById(R.id.etForgotEmail);
        MaterialButton btnSendReset = findViewById(R.id.btnSendReset);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSendReset.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Password reset instructions sent to " + email, Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
