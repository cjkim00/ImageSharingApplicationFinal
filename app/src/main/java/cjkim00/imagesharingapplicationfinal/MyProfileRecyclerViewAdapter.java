package cjkim00.imagesharingapplicationfinal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cjkim00.imagesharingapplicationfinal.Post.Post;
import cjkim00.imagesharingapplicationfinal.ProfileFragment.OnListFragmentInteractionListener;

public class MyProfileRecyclerViewAdapter extends RecyclerView.Adapter<MyProfileRecyclerViewAdapter.ViewHolder> {

    private final List<Post> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyProfileRecyclerViewAdapter(List<Post> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mPost = mValues.get(position);
        setThumbnail(holder.mImageView, holder.mPost.getImageLocation());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mPost, mValues);
                }
            }
        });
    }

    private void setThumbnail(ImageView imageView, String imageLocation) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(imageLocation);

        //final long ONE_MEGABYTE = 1024 * 1024;
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
        public final ImageView mImageView;
        public Post mPost;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.imageView_image_thumbnail_fragment_profilev2);
        }

//        @Override
//        public String toString() {
//            return super.toString() + " '" + mContentView.getText() + "'";
//        }
    }
}
