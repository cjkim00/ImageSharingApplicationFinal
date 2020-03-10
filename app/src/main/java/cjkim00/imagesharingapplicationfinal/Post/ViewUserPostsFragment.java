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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
public class ViewUserPostsFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 4;
    private String mEmail;
    private ArrayList<Post> mUserPosts;
    private OnListFragmentInteractionListener mListener;

    public ViewUserPostsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEmail = ImageViewerActivity.getEmail();
        mUserPosts = new ArrayList<>();
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        try {
            getPostsFromUser();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_user_posts_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView mRecyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            MyViewUserPostsRecyclerViewAdapter mAdapter = new MyViewUserPostsRecyclerViewAdapter(mUserPosts, mListener);
            mRecyclerView.setAdapter(mAdapter);
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

    @Override
    public void onResume() {
        super.onResume();
    }


    private void getPostsFromUser() throws InterruptedException {
        Thread thread = new Thread(() -> {

            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                        .appendPath("get_posts_from_user_using_email")
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
                        getResults(sb.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
    }

    private void getResults(String result) {
        try {
            JSONObject root = new JSONObject(result);
            Log.i("MSG6", root.toString());
            Log.i("MSG6", "BOOL: " + root.has("success") + ", " + root.getBoolean("success"));
            if (root.has("success") && root.getBoolean("success") ) {
                JSONArray data = root.getJSONArray("data");
                Log.i("MSG6", "DATA: " + data.toString());
                Log.i("MSG6", "DATA: " + data.length());
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonPost = data.getJSONObject(i);
                    Post tempPost = new Post(jsonPost.getString("postlocation")
                            , jsonPost.getString("postdesc")
                            , jsonPost.getInt("likes")
                            , jsonPost.getInt("views")
                            , jsonPost.getInt("postid")
                            , jsonPost.getInt("memberid")
                    );

                    Log.i("MSG6", "POST: ");
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
        void onListFragmentInteraction(Post post, ArrayList<Post> list);
    }
}
