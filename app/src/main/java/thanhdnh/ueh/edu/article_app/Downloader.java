package thanhdnh.ueh.edu.article_app;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

public final class Downloader {
  private static final OkHttpClient CLIENT = new OkHttpClient();

  private Downloader() {
  }

  public interface DownloadCallback {
    void onSuccess(File downloadedFile);

    void onError(Exception exception);
  }

  // Giữ hàm cũ để tương thích. Hàm này là đồng bộ nên chỉ được gọi ở luồng nền.
  public static File downloadFile(String url, File cacheDirectory) {
    Request request = new Request.Builder().url(url).build();
    try (Response response = CLIENT.newCall(request).execute()) {
      if (!response.isSuccessful() || response.body() == null) {
        return null;
      }

      File outputFile = File.createTempFile("download_", extensionOf(response), cacheDirectory);
      try (BufferedSink sink = Okio.buffer(Okio.sink(outputFile))) {
        sink.writeAll(response.body().source());
      }
      return outputFile;
    } catch (Exception exception) {
      return null;
    }
  }

  // Đúng chữ ký bài yêu cầu: trả void, nhận Handler, ProgressBar và ImageView.
  public static void downloadWithProgress(
      String url,
      Handler mainHandler,
      Context context,
      File cacheDirectory,
      ProgressBar progressBar,
      ImageView imageView
  ) {
    downloadWithProgress(url, mainHandler, cacheDirectory, progressBar, new DownloadCallback() {
      @Override
      public void onSuccess(File downloadedFile) {
        if (imageView != null) {
          imageView.setImageURI(Uri.fromFile(downloadedFile));
        }
      }

      @Override
      public void onError(Exception exception) {
        // Màn hình gọi hàm có callback sẽ tự hiển thị thông báo phù hợp.
      }
    });
  }

  // Overload có callback dùng khi tải JSON: tải xong mới đọc file và parse Gson.
  public static void downloadWithProgress(
      String url,
      Handler mainHandler,
      File cacheDirectory,
      ProgressBar progressBar,
      DownloadCallback callback
  ) {
    showProgress(mainHandler, progressBar);

    Request request;
    try {
      request = new Request.Builder().url(url).build();
    } catch (IllegalArgumentException exception) {
      finishWithError(mainHandler, progressBar, callback, exception);
      return;
    }

    CLIENT.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException exception) {
        finishWithError(mainHandler, progressBar, callback, exception);
      }

      @Override
      public void onResponse(Call call, Response response) {
        try (Response safeResponse = response) {
          ResponseBody body = safeResponse.body();
          if (!safeResponse.isSuccessful() || body == null) {
            throw new IOException("HTTP " + safeResponse.code());
          }

          long totalBytes = body.contentLength();
          File outputFile = File.createTempFile(
              "download_", extensionOf(safeResponse), cacheDirectory
          );

          try (InputStream input = body.byteStream();
               OutputStream output = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[8 * 1024];
            long downloadedBytes = 0L;
            int count;
            while ((count = input.read(buffer)) != -1) {
              output.write(buffer, 0, count);
              downloadedBytes += count;
              if (totalBytes > 0) {
                int percent = (int) Math.min(100, downloadedBytes * 100L / totalBytes);
                mainHandler.post(() -> {
                  if (progressBar != null) {
                    progressBar.setIndeterminate(false);
                    progressBar.setProgress(percent);
                  }
                });
              }
            }
          }

          mainHandler.post(() -> {
            hideProgress(progressBar);
            callback.onSuccess(outputFile);
          });
        } catch (Exception exception) {
          finishWithError(mainHandler, progressBar, callback, exception);
        }
      }
    });
  }

  private static void showProgress(Handler handler, ProgressBar progressBar) {
    handler.post(() -> {
      if (progressBar != null) {
        progressBar.setProgress(0);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
      }
    });
  }

  private static void hideProgress(ProgressBar progressBar) {
    if (progressBar != null) {
      progressBar.setVisibility(View.GONE);
    }
  }

  private static void finishWithError(
      Handler handler,
      ProgressBar progressBar,
      DownloadCallback callback,
      Exception exception
  ) {
    handler.post(() -> {
      hideProgress(progressBar);
      callback.onError(exception);
    });
  }

  private static String extensionOf(Response response) {
    String contentType = response.header("Content-Type", "").toLowerCase();
    if (contentType.contains("json")) return ".json";
    if (contentType.contains("png")) return ".png";
    if (contentType.contains("jpeg") || contentType.contains("jpg")) return ".jpg";
    return ".tmp";
  }
}
