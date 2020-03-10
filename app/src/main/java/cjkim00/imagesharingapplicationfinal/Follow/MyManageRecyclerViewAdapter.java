package cjkim00.imagesharingapplicationfinal.Follow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cjkim00.imagesharingapplicationfinal.R;
import cjkim00.imagesharingapplicationfinal.Search.Member;
import cjkim00.imagesharingapplicationfinal.Follow.ManageFragment.OnListFragmentInteractionListener;

public class MyManageRecyclerViewAdapter extends RecyclerView.Adapter<MyManageRecyclerViewAdapter.ViewHolder> {

    public String mEmail;
    private RecyclerView mRecyclerView;
    private final List<Member> mValues;
    private final OnListFragmentInteractionListener mListener;


    MyManageRecyclerViewAdapter(List<Member> items, OnListFragmentInteractionListener listener, RecyclerView recyclerView, String email) {
        mValues = items;
        mListener = listener;
        mRecyclerView = recyclerView;
        mEmail = email;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mUsername.setText(mValues.get(position).getUsername());
        holder.mProfileDesc = mValues.get(position).getDescription();
        setProfileImage(holder.mProfileImage, mValues.get(position).getProfileImageLocation());
        holder.mRemoveButton.setOnClickListener(v -> {
            unfollowUser(mValues.get(position).getUsername());
            updateRecyclerView(position);
        });

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void setProfileImage(ImageView imageView, String imageLocation) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(imageLocation);

        //final long ONE_MEGABYTE = 1024 * 1024;
        final long FIFTEEN_MEGABYTES = 15360 * 15360;
        imageRef.getBytes(FIFTEEN_MEGABYTES).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
    }

    private void updateRecyclerView(int position) {
        mValues.remove(position);
        mRecyclerView.removeViewAt(position);
        this.notifyItemRemoved(position);
        this.notifyItemRangeChanged(position, mValues.size());

    }

    private void unfollowUser(String username) {
        Thread thread = new Thread(() -> {
            try {
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
                jsonParam.put("User", mEmail);
                jsonParam.put("Following", username);
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
        });
        thread.start();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView mProfileImage;
        public final TextView mUsername;
        String mProfileDesc;
        Button mRemoveButton;
        Member mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mProfileImage = view.findViewById(R.id.imageView_profile_image_fragment_manage);
            mUsername = view.findViewById(R.id.textView_username_fragment_manage);
            mRemoveButton = view.findViewById(R.id.button_remove_fragment_manage);
            mProfileDesc = "";
        }

        @NonNull
        @Override
        public String toString() {
            return "";
        }
    }
}
