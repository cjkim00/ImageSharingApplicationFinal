package cjkim00.imagesharingapplicationfinal.Profile;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
        mEmail = Objects.requireNonNull(args).getString("Email");
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
        Button mEditProfileButton = view.findViewById(R.id.button_edit_profile_fragment_user_profile);

        setProfileImage(profileImage, mLocation);
        username.setText(mUsername);
        description.setText(mDescription);
        followers.setText("Followers: " + mFollowers);
        following.setText("Following: " + mFollowing);

        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("Email", mEmail);
                args.putString("Username", mUsername);
                args.putString("Location", mLocation);
                args.putString("Description", mDescription);

                EditProfileFragment editProfileFragment = new EditProfileFragment();
                editProfileFragment.setArguments(args);

                replaceFragment(editProfileFragment);
            }
        });

        return view;
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


    private void replaceFragment(Fragment fragment) {

        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager)
                .beginTransaction();
        fragmentTransaction.replace(R.id.layout_image_viewer, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

}
