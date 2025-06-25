package android.compress;

import android.app.AlertDialog;
import android.compress.adapters.UserAdapter;
import android.compress.models.FirebaseManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements UserAdapter.OnUserDeleteListener {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private TextInputEditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        recyclerView = findViewById(R.id.recycler_view_users);
        searchEditText = findViewById(R.id.edit_text_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userAdapter != null) {
                    userAdapter.filter(s.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        FirebaseManager.getAllUsers(new FirebaseManager.UserListCallback() {
            @Override
            public void onSuccess(List<FirebaseManager.User> userList) {
                userAdapter = new UserAdapter(userList, AdminDashboardActivity.this);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(AdminDashboardActivity.this, "Lỗi tải danh sách: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDeleteClicked(FirebaseManager.User user, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa user")
                .setMessage("Bạn có chắc chắn muốn xóa " + user.getUsername() + "?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    FirebaseManager.deleteUser(user.getUserId(), new FirebaseManager.SimpleCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                            userAdapter.removeItem(position); // Cập nhật UI
                        }
                        @Override
                        public void onFailure(String message) {
                            Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
