package cjkim00.imagesharingapplicationfinal.Search;

public class Member {

    private String mEmail;
    private String mUsername;
    private String mDescription;
    private String mProfileImageLocation;
    private int mFollowers;
    private int mFollowing;

    public Member(String username, String desc, String imageLocation,  int followers, int following) {
        mUsername = username;
        mDescription = desc;
        mProfileImageLocation = imageLocation;
        mFollowers = followers;
        mFollowing = following;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getProfileImageLocation() { return mProfileImageLocation; }

    public String getDescription() {
        return mDescription;
    }

    public int getFollowers() {
        return mFollowers;
    }

    public int getFollowing() {
        return mFollowing;
    }

}
