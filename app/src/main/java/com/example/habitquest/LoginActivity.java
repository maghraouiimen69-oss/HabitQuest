package com.example.habitquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private TextInputLayout tilIdentifier;
    private TextInputLayout tilPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            goToDashboard();
            return;
        }

        tilIdentifier = findViewById(R.id.tilIdentifier);
        tilPassword = findViewById(R.id.tilPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.linkRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        tilIdentifier.setError(null);
        tilPassword.setError(null);

        String identifier = tilIdentifier.getEditText().getText().toString().trim();
        String password = tilPassword.getEditText().getText().toString();

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.err_fill_all, Toast.LENGTH_SHORT).show();
            if (identifier.isEmpty()) {
                tilIdentifier.setError(getString(R.string.err_fill_all));
            }
            if (password.isEmpty()) {
                tilPassword.setError(getString(R.string.err_fill_all));
            }
            return;
        }

        User user = db.login(identifier, password);
        if (user == null) {
            tilPassword.setError(getString(R.string.err_login));
            Toast.makeText(this, R.string.err_login, Toast.LENGTH_SHORT).show();
            return;
        }

        session.saveSession(user.getId());
        goToDashboard();
    }

    private void goToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
