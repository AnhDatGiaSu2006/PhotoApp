package thanhdnh.ueh.edu.article_app;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UserList {
  @SerializedName("users")
  private List<UserProfile> users;

  public List<UserProfile> getUsers() {
    return users == null ? new ArrayList<>() : users;
  }
}
