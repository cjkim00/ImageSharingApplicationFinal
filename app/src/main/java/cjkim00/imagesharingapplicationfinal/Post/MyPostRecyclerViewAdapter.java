package cjkim00.imagesharingapplicationfinal.Post;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cjkim00.imagesharingapplicationfinal.ImageView.ImageViewerActivity;
import cjkim00.imagesharingapplicationfinal.Post.PostFragment.OnListFragmentInteractionListener;
import cjkim00.imagesharingapplicationfinal.R;


public class MyPostRecyclerViewAdapter extends RecyclerView.Adapter<MyPostRecyclerViewAdapter.ViewHolder> {

    private final List<Post> mPosts;
    private final List<Post> mLikedPosts;
    private List<Integer> mPostIDs;
    private List<Integer> mLikedPostIDs;

    private final OnListFragmentInteractionListener mListener;

    public MyPostRecyclerViewAdapter(List<Post> posts, List<Post> likedPosts, OnListFragmentInteractionListener listener) {
        //public MyPostRecyclerViewAdapter(List<Post> posts, OnListFragmentInteractionListener listener) {
        mPosts = (ArrayList<Post>) posts;
        mListener = listener;
        mLikedPosts = likedPosts;
        mPostIDs = new ArrayList<>();
        mLikedPostIDs = new ArrayList<>();
        convertPostListToIDList();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mPost = mPosts.get(position);
        holder.mDescView.setText(mPosts.get(position).getDescription());
        holder.mLikesView.setText(String.valueOf(mPosts.get(position).getLikes()));
        holder.mViewsView.setText(String.valueOf(mPosts.get(position).getViews()));

        if(checkIfPostIsLiked(holder.mPost.getPostID())) {
            holder.mLikeButton.setImageResource(R.drawable.ic_favorite_red_24dp);
            holder.isLiked = true;
        } else {
            holder.mLikeButton.setImageResource(R.drawable.ic_favorite_blue_24dp);
            holder.isLiked = false;
        }
        holder.mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.isLiked) {
                    removeLikeFromPost(holder.mPost.getPostID());
                    holder.isLiked = false;
                    holder.mLikeButton.setImageResource(R.drawable.ic_favorite_blue_24dp);
                    Log.i("LIKE", "Removed Like");
                } else {
                    likePost(holder.mPost.getPostID());
                    holder.isLiked = true;
                    holder.mLikeButton.setImageResource(R.drawable.ic_favorite_red_24dp);
                    Log.i("LIKE", "Added Like");
                }
            }
        });

        getImageFromStorage(holder.mImage, mPosts.get(position).getImageLocation(), holder.mPost);


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mPost);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.i("MSG", "Size: " + mPosts.size());
        return mPosts.size();

    }

    public void getImageFromStorage(ImageView imageView, String location, Post post) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(location);
        final long ONE_MEGABYTE = 1024 * 1024;
        final long FIFTEEN_MEGABYTES = 15360 * 15360;
        imageRef.getBytes(FIFTEEN_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
                post.setByteArray(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //create empty bitmap to prevent crashes
            }
        });

    }

    public void convertPostListToIDList() {
        for(int i = 0; i < mPosts.size(); i++) {
            mPostIDs.add(mPosts.get(i).getPostID());

        }
        //Log.i("MSGID", String.valueOf(mLikedPostIDs.size()) + ", ");
        for(int i = 0; i < mLikedPosts.size(); i++) {
            mLikedPostIDs.add(mLikedPosts.get(i).getPostID());
            Log.i("MSGID", String.valueOf(mLikedPostIDs.get(i)) + ", ");
        }
    }

    public boolean checkIfPostIsLiked(int postID) {
        return mLikedPostIDs.contains(postID);
    }

    public void likePost(int postID) {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection urlConnection = null;
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                            .appendPath("like_post")
                            .build();

                    URL url = new URL(uri.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    conn.setUseCaches(false);
                    conn.setAllowUserInteraction(false);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.connect();
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("Email", ImageViewerActivity.mEmail);
                    jsonParam.put("PostID", postID);
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());
                    os.flush();
                    os.close();

                    int status = conn.getResponseCode();
                    switch (status) {
                        case 200:
                        case 201:
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(conn.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }
                            br.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void removeLikeFromPost(int postID) {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                            .appendPath("remove_from_like_post")
                            .build();

                    URL url = new URL(uri.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    conn.setUseCaches(false);
                    conn.setAllowUserInteraction(false);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.connect();
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("Email", ImageViewerActivity.mEmail);
                    jsonParam.put("PostID", postID);
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());
                    os.flush();
                    os.close();

                    int status = conn.getResponseCode();
                    switch (status) {
                        case 200:
                        case 201:
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(conn.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }
                            br.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView mImage;
        final TextView mDescView;
        final TextView mLikesView;
        final TextView mViewsView;
        final ImageButton mLikeButton;
        private boolean isLiked;
        private int postID;

        Post mPost;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mImage = view.findViewById(R.id.imageView_postImage_postfragment);
            mDescView = view.findViewById(R.id.textview_postdesc_fragmentpost);
            mLikesView = view.findViewById(R.id.textview_likes_fragmentpost);
            mViewsView = view.findViewById(R.id.textview_views_fragmentpost);
            mLikeButton = view.findViewById(R.id.imageButton_like_post_fragment_post);
            isLiked = false;
        }

        //@Override
        //public String toString() {
        //    return super.toString() + " '" + mContentView.getText() + "'";
        //}
    }
}