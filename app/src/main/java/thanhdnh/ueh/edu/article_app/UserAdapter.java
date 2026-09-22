package thanhdnh.ueh.edu.article_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends BaseAdapter {
  private final Context context;
  private final List<UserProfile> users;

  public UserAdapter(Context context, List<UserProfile> users) {
    this.context = context;
    this.users = users;
  }

  @Override public int getCount() { return users.size(); }
  @Override public UserProfile getItem(int position) { return users.get(position); }
  @Override public long getItemId(int position) { return getItem(position).getId(); }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      convertView = LayoutInflater.from(context).inflate(R.layout.user_disp_tpl, parent, false);
      holder = new ViewHolder();
      holder.avatar = convertView.findViewById(R.id.imv_photo);
      holder.username = convertView.findViewById(R.id.tv_username);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    UserProfile user = getItem(position);
    holder.username.setText(valueOrUnknown(user.getUsername()));
    holder.avatar.setImageResource(android.R.drawable.ic_menu_gallery);
    if (user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
      Picasso.get()
          .load(user.getAvatarUrl())
          .placeholder(android.R.drawable.ic_menu_gallery)
          .error(android.R.drawable.ic_menu_report_image)
          .fit()
          .centerCrop()
          .into(holder.avatar);
    }
    return convertView;
  }

  private String valueOrUnknown(String value) {
    return value == null || value.trim().isEmpty() ? "Chưa có tên" : value;
  }

  private static class ViewHolder {
    ImageView avatar;
    TextView username;
  }
}
