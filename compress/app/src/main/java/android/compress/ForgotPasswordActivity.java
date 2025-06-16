package android.compress; // Thay 'com.example.yourappname' bằng package name của bạn

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText phoneEditText;
    private MaterialButton recoverButton;
    private TextView goBackLoginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        phoneEditText = findViewById(R.id.edit_text_forgot_phone);
        recoverButton = findViewById(R.id.button_recover_password);
        goBackLoginTextView = findViewById(R.id.text_go_back_login);

        recoverButton.setOnClickListener(v -> {
            String phone = phoneEditText.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Kiểm tra xem số điện thoại có tồn tại trong hệ thống không

                // Chuyển sang màn hình OTP cho luồng quên mật khẩu
                Intent intent = new Intent(ForgotPasswordActivity.this, OtpActivity.class);
                intent.putExtra("phone", phone);
                intent.putExtra("flow_type", "forgot_password"); // Đánh dấu đây là luồng quên mật khẩu
                startActivity(intent);
            }
        });

        goBackLoginTextView.setOnClickListener(v -> finish());
    }
}
