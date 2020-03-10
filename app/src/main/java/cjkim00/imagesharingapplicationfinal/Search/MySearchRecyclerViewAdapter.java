package cjkim00.imagesharingapplicationfinal.Search;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cjkim00.imagesharingapplicationfinal.R;
import cjkim00.imagesharingapplicationfinal.Search.SearchFragment.OnListFragmentInteractionListener;


public class MySearchRecyclerViewAdapter extends RecyclerView.Adapter<MySearchRecyclerViewAdapter.ViewHolder> {
    private final List<Member> mValues;
    private final OnListFragmentInteractionListener mListener;


    public MySearchRecyclerViewAdapter(List<Member> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mUsername.setText(mValues.get(position).getUsername());
        holder.mFollowers.setText(String.valueOf(mValues.get(position).getFollowers()));
        holder.mFollowing.setText(String.valueOf(mValues.get(position).getFollowing()));
        holder.mProfileDesc = mValues.get(position).getDescription();
        setProfileImage(holder.mProfileImage, mValues.get(position).getProfileImageLocation());
        Log.i("MSG", "Username: " + mValues.get(position).getUsername());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }



    private void setProfileImage(ImageView imageView, String imageLocation) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(imageLocation);

        final long ONE_MEGABYTE = 1024 * 1024;
        final long FIFTEEN_MEGABYTES = 15360 * 15360;
        imageRef.getBytes(FIFTEEN_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mProfileImage;
        public final TextView mUsername;
        public final TextView mFollowers;
        public final TextView mFollowing;
        public String mProfileDesc;
        public Member mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mProfileImage = view.findViewById(R.id.imageView_profile_image_fragment_search);
            mUsername = view.findViewById(R.id.textView_username_fragment_search);
            mFollowers = view.findViewById(R.id.textView_followers_fragment_search);
            mFollowing = view.findViewById(R.id.textView_following_fragment_search);
            mProfileDesc = "";
        }

        @Override
        public String toString() {
            return "";
        }
    }
}
