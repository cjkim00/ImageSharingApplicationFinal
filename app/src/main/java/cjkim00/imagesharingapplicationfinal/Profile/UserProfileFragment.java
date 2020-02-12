package cjkim00.imagesharingapplicationfinal.Profile;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cjkim00.imagesharingapplicationfinal.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserProfileFragment extends Fragment {

    private String mEmail;
    private String mUsername;
    private String mDescription;
    private String mLocation;
    private int mFollowers;
    private int mFollowing;
    View mFragment;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mEmail = args.getString("Email");
        mUsername = args.getString("Username");
        mDescription = args.getString("Description");
        mLocation = args.getString("Location");
        mFollowers = args.getInt("Followers");
        mFollowing = args.getInt("Following");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        TextView username = view.findViewById(R.id.textView_username_fragment_user_profile);
        TextView description = view.findViewById(R.id.textView_profile_description_fragment_user_profile);
        TextView followers = view.findViewById(R.id.textView_followers_fragment_user_profile);
        TextView following = view.findViewById(R.id.textView_following_fragment_user_profile);
        ImageView profileImage = view.findViewById(R.id.imageView_profile_image_fragment_user_profile);
        Log.i("MSG", "LOCATION : " + mLocation);
        setProfileImage(profileImage, mLocation);

        username.setText(mUsername);
        description.setText(mDescription);
        followers.setText("Followers: " + mFollowers);
        following.setText("Following: " + mFollowing);

        return view;
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

}
