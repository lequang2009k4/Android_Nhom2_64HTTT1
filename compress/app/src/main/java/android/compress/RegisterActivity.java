package android.compress; // Thay 'com.example.yourappname' bằng package name của bạn

import android.app.ProgressDialog;
import android.compress.models.FirebaseManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, phoneEditText, passwordEditText;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        usernameEditText = findViewById(R.id.edit_text_register_username);
        phoneEditText = findViewById(R.id.edit_text_phone);
        passwordEditText = findViewById(R.id.edit_text_register_password);
        MaterialButton registerButton = findViewById(R.id.button_register);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        registerButton.setOnClickListener(v -> handleRegistration());
        findViewById(R.id.text_login_link).setOnClickListener(v -> finish());
    }

    private void handleRegistration() {
        String username = usernameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        FirebaseManager.sendRegistrationOtp(this, username, phone, new FirebaseManager.VerificationCallback() {
            @Override
            public void onVerified(String verificationId) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Mã OTP đã được gửi.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                intent.putExtra("flow_type", "register");
                intent.putExtra("verificationId", verificationId);
                intent.putExtra("username", username);
                intent.putExtra("phone", phone);
                intent.putExtra("password", password);
                startActivity(intent);
            }

            @Override
            public void onFailure(String message) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
