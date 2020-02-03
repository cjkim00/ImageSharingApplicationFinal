package cjkim00.imagesharingapplicationfinal.Post;

import java.io.Serializable;

public class Post implements Serializable {

    private byte[] mByteArray;
    private String mImageLocation;
    private String mDesc;
    private int mLikes;
    private int mViews;
    private int mPostID;

    public Post(String imageLocation, String desc, int likes, int views, int postID) {
        //mBitmap = image;
        mImageLocation = imageLocation;
        mDesc = desc;
        mLikes = likes;
        mViews = views;
        mPostID = postID;
    }


    public void setByteArray(byte[] byteArray) {
        mByteArray = byteArray;
    }

    public byte[] getByteArray() { return mByteArray; }

    public String getImageLocation() { return mImageLocation; }

    public String getDescription() {
        return mDesc;
    }

    public int getLikes() {
        return mLikes;
    }

    public int getViews() {
        return mViews;
    }

    public int getPostID() { return mPostID; }
}
