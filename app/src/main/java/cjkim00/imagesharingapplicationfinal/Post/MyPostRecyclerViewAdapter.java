package cjkim00.imagesharingapplicationfinal.Post;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cjkim00.imagesharingapplicationfinal.Post.PostFragment.OnListFragmentInteractionListener;
import cjkim00.imagesharingapplicationfinal.R;


public class MyPostRecyclerViewAdapter extends RecyclerView.Adapter<MyPostRecyclerViewAdapter.ViewHolder> {

    private final List<Post> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Bitmap mBitmap;

    public MyPostRecyclerViewAdapter(List<Post> items, OnListFragmentInteractionListener listener) {
        mValues = (ArrayList<Post>) items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDescView.setText(mValues.get(position).getDescription());
        holder.mLikesView.setText(String.valueOf(mValues.get(position).getLikes()));
        holder.mViewsView.setText(String.valueOf(mValues.get(position).getViews()));
        holder.mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MSG2", "Test log for button");
            }
        });

        getImageFromStorage(holder.mImage, mValues.get(position).getImageLocation(), holder.mItem);


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.i("MSG", "Size: " + mValues.size());
        return mValues.size();

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
                mBitmap = bitmap;
                post.setByteArray(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //create empty bitmap to prevent crashes
            }
        });

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView mImage;
        final TextView mDescView;
        final TextView mLikesView;
        final TextView mViewsView;
        final ImageButton mLikeButton;

        Post mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mImage = view.findViewById(R.id.imageView_postImage_postfragment);
            mDescView = view.findViewById(R.id.textview_postdesc_fragmentpost);
            mLikesView = view.findViewById(R.id.textview_likes_fragmentpost);
            mViewsView = view.findViewById(R.id.textview_views_fragmentpost);
            mLikeButton = view.findViewById(R.id.imageButton_like_post_fragment_post);
        }

        //@Override
        //public String toString() {
        //    return super.toString() + " '" + mContentView.getText() + "'";
        //}
    }
}