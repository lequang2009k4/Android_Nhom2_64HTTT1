package android.compress.adapters;

import android.compress.models.FirebaseManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<FirebaseManager.User> userList;
    private List<FirebaseManager.User> userListFull;
    private OnUserDeleteListener deleteListener;

    public interface OnUserDeleteListener {
        void onDeleteClicked(FirebaseManager.User user, int position);
    }

    public UserAdapter(List<FirebaseManager.User> userList, OnUserDeleteListener listener) {
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        FirebaseManager.User user = userList.get(position);
        holder.fullNameTextView.setText(user.getFullName());
        holder.phoneTextView.setText(user.getPhone()); // CẬP NHẬT: Hiển thị số điện thoại

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClicked(user, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void filter(String text) {
        userList.clear();
        if (text.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            text = text.toLowerCase();
            for (FirebaseManager.User item : userListFull) {
                // Có thể tìm theo tên hoặc số điện thoại
                if (item.getUsername().toLowerCase().contains(text) || item.getPhone().contains(text)) {
                    userList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        // Cần tìm đúng item trong userListFull để xóa
        FirebaseManager.User userToRemove = userList.get(position);
        userListFull.remove(userToRemove);
        userList.remove(position);
        notifyItemRemoved(position);
    }

    // CẬP NHẬT: ViewHolder để ánh xạ đúng các view mới
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameTextView;
        TextView phoneTextView; // CẬP NHẬT: Thêm view cho SĐT
        ImageButton deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.text_view_fullname);
            phoneTextView = itemView.findViewById(R.id.text_view_phone); // CẬP NHẬT
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
