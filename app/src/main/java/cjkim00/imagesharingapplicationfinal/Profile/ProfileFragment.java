package cjkim00.imagesharingapplicationfinal.Profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cjkim00.imagesharingapplicationfinal.Post.Post;
import cjkim00.imagesharingapplicationfinal.R;
import cjkim00.imagesharingapplicationfinal.Search.Member;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ProfileFragment extends Fragment {

    private String mCurrentUser;
    private String mUsername;
    private String mDescription;
    private String mLocation;
    private int mFollowers;
    private int mFollowing;
    private boolean mIsFollowing = false;

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 4;
    private ArrayList<Post> mUserPosts;
    private OnListFragmentInteractionListener mListener;



    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProfileFragment() {
    }

    public static ProfileFragment newInstance(int columnCount) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();

        mCurrentUser = bundle.getString("CurrentUser");
        mUsername = bundle.getString("Username");
        mDescription = bundle.getString("Description");
        mLocation = bundle.getString("Location");
        mFollowers = bundle.getInt("Followers");
        mFollowing = bundle.getInt("Following");

        mUserPosts = new ArrayList<>();

        Log.i("MSG6", "PROFILE FRAGMENT USERNAME: " + mUsername);

        try {
            getPostsFromUser(mUsername);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (getArguments() != null) {
            mColumnCount = 4;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_list, container, false);
        RecyclerView mRecyclerView = view.findViewById(R.id.list_profile_posts_fragment_profile_list);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        view.setMinimumWidth(width);
        view.setMinimumHeight(height);

        TextView username = view.findViewById(R.id.textView_username_fragment_profile);
        TextView description = view.findViewById(R.id.textView_profile_description_fragment_profile);
        TextView followers = view.findViewById(R.id.textView_followers_fragment_profile);
        TextView following = view.findViewById(R.id.textView_following_fragment_profile);
        ImageView profileImage = view.findViewById(R.id.imageView_profile_image_fragment_profile);
        Button followOrUnfollowButton = view.findViewById(R.id.button_follow_or_unfollow_fragment_profile);

        followOrUnfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButton(followOrUnfollowButton);
            }
        });

        try {
            checkIfFollowing(followOrUnfollowButton);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setProfileImage(profileImage, mLocation);
        username.setText(mUsername);
        description.setText(mDescription);
        followers.setText("Followers: " + mFollowers);
        following.setText("Following: " + mFollowing);
        // Set the adapter
        Context context = view.getContext();
        //RecyclerView recyclerView = (RecyclerView) view;
        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            Log.i("MSGVIEW", "Linear");
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            Log.i("MSGVIEW", "Grid");
        }
        mRecyclerView.setAdapter(new MyProfileRecyclerViewAdapter(mUserPosts, mListener));

        Log.i("MSG" , "Profile fragment.");
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateButton(Button button) {
        if(mIsFollowing) {
            unfollowUser();
            button.setText("Follow");
            mIsFollowing = false;
        } else {
            followUser();
            button.setText("Unfollow");
            mIsFollowing = true;
        }
        // mIsFollowing = !mIsFollowing;
    }

    public void followUser() {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection urlConnection = null;
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                            .appendPath("follow_user")
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
                    jsonParam.put("User", mCurrentUser);
                    jsonParam.put("Following", mUsername);
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
                            //setButton(sb.toString() , button);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void unfollowUser() {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection urlConnection = null;
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                            .appendPath("unfollow_user")
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
                    jsonParam.put("User", mCurrentUser);
                    jsonParam.put("Following", mUsername);
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
                            //setButton(sb.toString() , button);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void checkIfFollowing(Button button) throws InterruptedException {
        List<Member> tempArray = new ArrayList<>();
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {

                try {
                    HttpURLConnection urlConnection = null;
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                            .appendPath("isUserFollowingMember")
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
                    jsonParam.put("User", mCurrentUser);
                    jsonParam.put("Following", mUsername);
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
                            setButton(sb.toString() , button);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }

    public void setButton(String result, Button button) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success")) {
                if(root.getBoolean("success")) {
                    mIsFollowing = true;
                    button.setText("Unfollow");
                } else {
                    mIsFollowing = false;
                    button.setText("Follow");
                }

            } else {
                Log.i("MSG", "No response");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getPostsFromUser(String user) throws InterruptedException {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {

                try {
                    HttpURLConnection urlConnection = null;
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                            .appendPath("get_posts_from_user")
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
                    jsonParam.put("User", user);
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
                            Log.i("MSG6", "PROFILE FRAGMENT: " +sb.toString());
                            getResults(sb.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }

    public void getResults(String result) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success") && root.getBoolean("success") ) {
                JSONArray data = root.getJSONArray("data");
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonPost = data.getJSONObject(i);
                    Post tempPost = new Post(jsonPost.getString("postlocation")
                            , jsonPost.getString("postdesc")
                            , jsonPost.getInt("likes")
                            , jsonPost.getInt("views")
                            , jsonPost.getInt("postid")
                            , jsonPost.getInt("memberid")
                    );
                    mUserPosts.add(tempPost);
                }
            } else {
                Log.i("MSG", "No response");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("MSG", e.getMessage());
        }
    }

    private void setProfileImage(ImageView imageView, String imageLocation) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(imageLocation);

        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Post post, List<Post> posts);
    }
}
