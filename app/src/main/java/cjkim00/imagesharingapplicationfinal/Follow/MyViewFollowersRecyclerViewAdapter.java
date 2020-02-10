package cjkim00.imagesharingapplicationfinal.Follow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import cjkim00.imagesharingapplicationfinal.Follow.ViewFollowersFragment.OnListFragmentInteractionListener;
import cjkim00.imagesharingapplicationfinal.R;
import cjkim00.imagesharingapplicationfinal.Search.Member;

public class MyViewFollowersRecyclerViewAdapter extends RecyclerView.Adapter<MyViewFollowersRecyclerViewAdapter.ViewHolder> {


    public String mEmail;
    private RecyclerView mRecyclerView;
    private final List<Member> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyViewFollowersRecyclerViewAdapter(List<Member> items, OnListFragmentInteractionListener listener, RecyclerView recyclerView, String email) {
        mValues = items;
        mListener = listener;
        mRecyclerView = recyclerView;
        mEmail = email;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_view_followers, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mUsername.setText(mValues.get(position).getUsername());
        holder.mProfileDesc = mValues.get(position).getDescription();
        setProfileImage(holder.mProfileImage, mValues.get(position).getProfileImageLocation());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mProfileImage;
        public final TextView mUsername;
        public String mProfileDesc;
        public Member mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mProfileImage = view.findViewById(R.id.imageView_profile_image_fragment_manage);
            mUsername = view.findViewById(R.id.textView_username_fragment_manage);
            mProfileDesc = "";
        }
    }
}
