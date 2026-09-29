package thanhdnh.ueh.edu.article_app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

  public void loadMockData() {
    executor.execute(() -> {
      List<UserProfile> mockUsers = new ArrayList<>();
      mockUsers.add(new UserProfile(1, "nguyen_an", "an.nguyen@example.com", "Sinh viên yêu thích phát triển ứng dụng Android.", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500", "0901234567", "Lập trình, Nhiếp ảnh"));
      mockUsers.add(new UserProfile(2, "tran_binh", "binh.tran@example.com", "Thích thiết kế giao diện đơn giản và dễ sử dụng.", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500", "0912345678", "Thiết kế, Âm nhạc"));
      mockUsers.add(new UserProfile(3, "le_chi", "chi.le@example.com", "Đang học Java và phát triển ứng dụng di động.", "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=500", "0923456789", "Đọc sách, Du lịch"));
      mockUsers.add(new UserProfile(4, "pham_duy", "duy.pham@example.com", "Đam mê công nghệ và lập trình di động.", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=500", "0934567890", "Chơi game, Thể thao"));
      mockUsers.add(new UserProfile(5, "hoang_yen", "yen.hoang@example.com", "Yêu thích nhiếp ảnh và sáng tạo nội dung.", "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=500", "0945678901", "Nhiếp ảnh, Viết lách"));

      UserList mockList = new UserList(mockUsers);
      showUsers(mockList);
    });
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
            loadMockData();
          }
        }
    );
  }

  private void parseFileInBackground(File file) {
    executor.execute(() -> {
      try (InputStream input = new FileInputStream(file)) {
        showUsers(parse(input));
      } catch (Exception exception) {
        loadMockData();
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
