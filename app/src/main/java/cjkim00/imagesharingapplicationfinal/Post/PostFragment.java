package cjkim00.imagesharingapplicationfinal.Post;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cjkim00.imagesharingapplicationfinal.ImageView.ImageViewerActivity;
import cjkim00.imagesharingapplicationfinal.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PostFragment extends Fragment {
    private String mEmail;
    private OnListFragmentInteractionListener mListener;
    private List<Post> mPosts;
    private List<Post> mLikedPosts;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosts = new ArrayList<>();
        mLikedPosts = new ArrayList<>();
        mEmail = ImageViewerActivity.mEmail;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            // TODO: Customize parameters
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            try {
                getPosts();
                getLikedPosts();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recyclerView.setAdapter(new MyPostRecyclerViewAdapter(mPosts, mLikedPosts, mListener));

        }
        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
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

    private void getPosts() throws InterruptedException {
        List<Post> tempArray = new ArrayList<>();
        Thread thread = new Thread(() -> {

            try {
                Log.i("MSG", "START");
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                        .appendPath("GetAllPosts")
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
                jsonParam.put("User", mEmail);
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                int status = conn.getResponseCode();
                Log.i("MSG", "" + status);
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
                        Log.i("MSG", sb.toString());

                        getResults(sb.toString() , tempArray);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        mPosts = tempArray;
    }

    private void getLikedPosts() throws InterruptedException {
        Thread thread = new Thread(() -> {

            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                        .appendPath("get_liked_posts")
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
                jsonParam.put("User", mEmail);
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
                        Log.i("get posts: ", sb.toString());
                        getLikedPostsResults(sb.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
    }

    private void getLikedPostsResults(String result) {
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

                    Log.i("EMAIL", "Email: " + jsonPost.getString("postlocation"));
                    mLikedPosts.add(tempPost);

                }
            } else {
                Log.i("MSG", "No response");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("MSG", e.getMessage());
        }
    }

    private void getResults(String result, List<Post> arr) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success") && root.getBoolean("success") ) {
                //JSONObject response = root.getJSONObject("success");
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
                    arr.add(tempPost);
                }
            } else {
                Log.i("MSG", "No response");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("MSG", e.getMessage());
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Post item);
    }
}
