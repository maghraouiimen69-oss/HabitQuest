package com.example.habitquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private TextInputLayout tilUsername;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm = findViewById(R.id.tilConfirm);

        findViewById(R.id.btnRegister).setOnClickListener(v -> attemptRegister());
        findViewById(R.id.linkLogin).setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);

        String username = tilUsername.getEditText().getText().toString().trim();
        String email = tilEmail.getEditText().getText().toString().trim();
        String password = tilPassword.getEditText().getText().toString();
        String confirm = tilConfirm.getEditText().getText().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, R.string.err_fill_all, Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() < 3) {
            tilUsername.setError(getString(R.string.err_username_short));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.err_email_invalid));
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError(getString(R.string.err_password_short));
            return;
        }
        if (!password.equals(confirm)) {
            tilConfirm.setError(getString(R.string.err_password_match));
            return;
        }
        if (db.usernameExists(username)) {
            tilUsername.setError(getString(R.string.err_username_taken));
            return;
        }
        if (db.emailExists(email)) {
            tilEmail.setError(getString(R.string.err_email_taken));
            return;
        }

        long userId = db.insertUser(username, email, password);
        if (userId == -1) {
            Toast.makeText(this, R.string.err_register, Toast.LENGTH_SHORT).show();
            return;
        }

        session.saveSession((int) userId);
        Toast.makeText(this, R.string.success_register, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, DashboardActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
