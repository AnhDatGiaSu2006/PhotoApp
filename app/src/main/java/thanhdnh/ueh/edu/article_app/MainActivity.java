package thanhdnh.ueh.edu.article_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
  // URL đề xuất. Khi có JSON thật, chỉ thay hằng số này.
  private static final String USERS_JSON_URL =
      "https://raw.githubusercontent.com/thanhdnh/json/main/users.json";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (getSupportActionBar() != null) getSupportActionBar().hide();

    GridView gridView = findViewById(R.id.gridview);
    ProgressBar progressBar = findViewById(R.id.download_progress);
    new UserData(this, gridView, progressBar).loadData(USERS_JSON_URL);

    gridView.setOnItemClickListener((parent, view, position, rowId) -> {
      Object item = parent.getItemAtPosition(position);
      if (!(item instanceof UserProfile)) {
        Toast.makeText(this, "Không tìm thấy hồ sơ", Toast.LENGTH_SHORT).show();
        return;
      }
      UserProfile user = (UserProfile) item;
      Intent intent = new Intent(this, ViewUserProfileActivity.class);
      intent.putExtra(ViewUserProfileActivity.EXTRA_USER_ID, user.getId());
      startActivity(intent);
    });
  }
}
