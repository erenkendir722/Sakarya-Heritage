package com.sakaryamiras.app.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.repository.AuthRepository;

public class AuthActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "auth_mode";
    public static final String MODE_LOGIN = "login";
    public static final String MODE_SIGNUP = "signup";

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton primaryButton;
    private MaterialButton switchModeButton;
    private TextView titleView;
    private ProgressBar progress;
    private Toolbar toolbar;

    private final AuthRepository authRepo = new AuthRepository();
    private boolean signupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        primaryButton = findViewById(R.id.btn_primary);
        switchModeButton = findViewById(R.id.btn_switch_mode);
        titleView = findViewById(R.id.auth_title);
        progress = findViewById(R.id.auth_progress);

        String mode = getIntent().getStringExtra(EXTRA_MODE);
        signupMode = MODE_SIGNUP.equals(mode);
        applyMode();

        primaryButton.setOnClickListener(v -> submit());
        switchModeButton.setOnClickListener(v -> {
            signupMode = !signupMode;
            applyMode();
        });
    }

    private void applyMode() {
        if (signupMode) {
            titleView.setText(R.string.auth_signup_title);
            toolbar.setTitle(R.string.auth_signup_title);
            primaryButton.setText(R.string.auth_signup_button);
            switchModeButton.setText(R.string.auth_switch_to_login);
        } else {
            titleView.setText(R.string.auth_login_title);
            toolbar.setTitle(R.string.auth_login_title);
            primaryButton.setText(R.string.auth_login_button);
            switchModeButton.setText(R.string.auth_switch_to_signup);
        }
    }

    private void submit() {
        String email = textOf(emailInput);
        String password = textOf(passwordInput);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.form_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (signupMode && password.length() < 6) {
            Toast.makeText(this, R.string.auth_password_too_short, Toast.LENGTH_SHORT).show();
            return;
        }

        setBusy(true);
        if (signupMode) {
            authRepo.signUp(email, password)
                    .addOnSuccessListener(result -> {
                        setBusy(false);
                        Toast.makeText(this, R.string.auth_signup_success, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        setBusy(false);
                        Toast.makeText(this,
                                getString(R.string.auth_signup_error, e.getMessage()),
                                Toast.LENGTH_LONG).show();
                    });
        } else {
            authRepo.signIn(email, password)
                    .addOnSuccessListener(result -> {
                        setBusy(false);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        setBusy(false);
                        Toast.makeText(this, R.string.auth_login_error, Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        primaryButton.setEnabled(!busy);
        switchModeButton.setEnabled(!busy);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }
}
