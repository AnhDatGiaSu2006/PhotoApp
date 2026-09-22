package thanhdnh.ueh.edu.article_app;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
  @SerializedName("id")
  private int id;
  @SerializedName("username")
  private String username;
  @SerializedName("email")
  private String email;
  @SerializedName("desc")
  private String description;
  @SerializedName("avatar_url")
  private String avatarUrl;
  @SerializedName("tel")
  private String tel;
  @SerializedName("hobby")
  private String hobby;

  public int getId() { return id; }
  public String getUsername() { return username; }
  public String getEmail() { return email; }
  public String getDescription() { return description; }
  public String getAvatarUrl() { return avatarUrl; }
  public String getTel() { return tel; }
  public String getHobby() { return hobby; }
}
