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

public class SignupActivity extends AppCompatActivity {
    private TextInputEditText etFullName, etSignupEmail, etSignupPassword;
    private CheckBox cbTerms;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authRepository = new AuthRepository(this);

        etFullName = findViewById(R.id.etFullName);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        cbTerms = findViewById(R.id.cbTerms);
        MaterialButton btnCreateAccount = findViewById(R.id.btnCreateAccount);

        findViewById(R.id.btnGoToLogin).setOnClickListener(v -> finish());

        btnCreateAccount.setOnClickListener(v -> {
            String name = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
            String email = etSignupEmail.getText() != null ? etSignupEmail.getText().toString().trim() : "";
            String password = etSignupPassword.getText() != null ? etSignupPassword.getText().toString().trim() : "";
            boolean terms = cbTerms.isChecked();

            authRepository.signup(name, email, password, terms, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(UserProfile profile) {
                    Toast.makeText(SignupActivity.this, "Account created! Welcome, " + profile.getName(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
