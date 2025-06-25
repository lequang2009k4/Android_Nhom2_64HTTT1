package android.compress.adapters;

import android.compress.R;
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
    private List<FirebaseManager.User> userListFull; // Dùng để lưu danh sách gốc cho việc tìm kiếm
    private OnUserDeleteListener deleteListener;

    public interface OnUserDeleteListener {
        void onDeleteClicked(FirebaseManager.User user, int position);
    }

    public UserAdapter(List<FirebaseManager.User> userList, OnUserDeleteListener listener) {
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList); // Sao chép danh sách gốc
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
        // TODO: Cập nhật các trường dữ liệu thật (số file, v.v...)

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

    // Hàm để lọc danh sách khi tìm kiếm
    public void filter(String text) {
        userList.clear();
        if (text.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            text = text.toLowerCase();
            for (FirebaseManager.User item : userListFull) {
                if (item.getUsername().toLowerCase().contains(text)) {
                    userList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Hàm để xóa item khỏi adapter
    public void removeItem(int position) {
        userList.remove(position);
        userListFull.remove(position); // Cũng xóa khỏi danh sách gốc
        notifyItemRemoved(position);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameTextView;
        ImageButton deleteButton;
        // Thêm các view khác nếu cần

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.text_view_fullname);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
