package thanhdnh.ueh.edu.article_app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserData {
  private static UserList data;

  private final Context context;
  private final GridView gridView;
  private final ProgressBar progressBar;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public UserData(Context context, GridView gridView, ProgressBar progressBar) {
    this.context = context;
    this.gridView = gridView;
    this.progressBar = progressBar;
  }

  public static UserProfile getUserById(int id) {
    if (data == null) return null;
    for (UserProfile user : data.getUsers()) {
      if (user.getId() == id) return user;
    }
    return null;
  }

  public void loadData(String url) {
    Downloader.downloadWithProgress(
        url,
        mainHandler,
        context.getCacheDir(),
        progressBar,
        new Downloader.DownloadCallback() {
          @Override
          public void onSuccess(File downloadedFile) {
            parseFileInBackground(downloadedFile);
          }

          @Override
          public void onError(Exception exception) {
            loadSampleFromAssets("Không tải được URL, đang dùng dữ liệu mẫu");
          }
        }
    );
  }

  private void parseFileInBackground(File file) {
    executor.execute(() -> {
      try (InputStream input = new FileInputStream(file)) {
        showUsers(parse(input));
      } catch (Exception exception) {
        loadSampleFromAssets("JSON không hợp lệ, đang dùng dữ liệu mẫu");
      }
    });
  }

  private void loadSampleFromAssets(String message) {
    executor.execute(() -> {
      try (InputStream input = context.getAssets().open("users.json")) {
        showUsers(parse(input));
        mainHandler.post(() ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        );
      } catch (Exception exception) {
        mainHandler.post(() ->
            Toast.makeText(context, "Không thể đọc dữ liệu người dùng", Toast.LENGTH_LONG).show()
        );
      }
    });
  }

  private UserList parse(InputStream input) throws IOException {
    StringBuilder json = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(input, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) json.append(line);
    }

    UserList parsed = new Gson().fromJson(json.toString(), UserList.class);
    if (parsed == null || parsed.getUsers().isEmpty()) {
      throw new IOException("Danh sách users rỗng hoặc thiếu");
    }
    return parsed;
  }

  private void showUsers(UserList parsed) {
    data = parsed;
    List<UserProfile> users = parsed.getUsers();
    mainHandler.post(() -> gridView.setAdapter(new UserAdapter(context, users)));
  }
}
