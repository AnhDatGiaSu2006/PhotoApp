package thanhdnh.ueh.edu.article_app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ViewUserProfileActivity extends AppCompatActivity {
  public static final String EXTRA_USER_ID = "user_id";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_user_profile);
    if (getSupportActionBar() != null) getSupportActionBar().hide();

    TextView usernameView = findViewById(R.id.tv_detail_username);
    TextView idView = findViewById(R.id.tv_detail_id);
    TextView emailView = findViewById(R.id.tv_detail_email);
    TextView telView = findViewById(R.id.tv_detail_tel);
    TextView hobbyView = findViewById(R.id.tv_detail_hobby);
    TextView descriptionView = findViewById(R.id.tv_detail_description);
    ImageView avatarView = findViewById(R.id.iv_detail_avatar);

    int userId = getIntent().getIntExtra(EXTRA_USER_ID, -1);
    UserProfile user = UserData.getUserById(userId);
    if (user == null) {
      Toast.makeText(this, "Không tìm thấy hồ sơ", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    usernameView.setText(valueOrUnknown(user.getUsername()));
    idView.setText(getString(R.string.profile_id, user.getId()));
    emailView.setText(getString(R.string.profile_email, valueOrUnknown(user.getEmail())));
    telView.setText(getString(R.string.profile_tel, valueOrUnknown(user.getTel())));
    hobbyView.setText(getString(R.string.profile_hobby, valueOrUnknown(user.getHobby())));
    descriptionView.setText(valueOrUnknown(user.getDescription()));

    avatarView.setImageResource(android.R.drawable.ic_menu_gallery);
    if (user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
      Picasso.get()
          .load(user.getAvatarUrl())
          .placeholder(android.R.drawable.ic_menu_gallery)
          .error(android.R.drawable.ic_menu_report_image)
          .fit()
          .centerCrop()
          .into(avatarView);
    }
  }

  private String valueOrUnknown(String value) {
    return value == null || value.trim().isEmpty() ? getString(R.string.not_available) : value;
  }
}
