package cjkim00.imagesharingapplicationfinal.Post;


import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cjkim00.imagesharingapplicationfinal.Profile.UserProfileFragment;
import cjkim00.imagesharingapplicationfinal.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UploadPostFragment extends Fragment {

    private EditText mPostDescription;
    private Bitmap mBitmap;
    private String mEmail;
    private String mUsername;
    private String mLocation;
    private String mDescription;
    private int mFollowers;
    private int mFollowing;
    private String mUri;
    public UploadPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mEmail = Objects.requireNonNull(args).getString("Email");
        mUsername = args.getString("Username");
        mLocation = args.getString("Location");
        mDescription = args.getString("Description");
        mFollowers = args.getInt("Followers");
        mFollowing = args.getInt("Following");
        mUri = args.getString("Uri");

        byte[] bytes = args.getByteArray("Image");
        mBitmap = BitmapFactory.decodeByteArray(bytes, 0, Objects.requireNonNull(bytes).length);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upload_post, container, false);

        ImageView mNewImage = view.findViewById(R.id.imageView_upload_image_fragment_upload_post);
        mPostDescription = view.findViewById(R.id.editText_post_description_fragment_upload_post);
        mNewImage.setImageBitmap(mBitmap);

        Button uploadButton = view.findViewById(R.id.button_upload_post_fragment_upload_post);
        uploadButton.setOnClickListener(v -> {
            Uri uri = Uri.parse(mUri);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference reference = storageRef.child(getRealPathFromURI(uri));
            reference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext()
                        ,getRealPathFromURI(uri),Toast.LENGTH_SHORT).show())
                .addOnFailureListener(exception -> Log.i("MSG7", "UPLOADED: " + uri.toString())).addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {

                    uploadToDatabase(getRealPathFromURI(uri), mPostDescription.getText().toString());

                    Bundle args = new Bundle();
                    args.putString("Email", mEmail);
                    args.putString("Username", mUsername);
                    args.putString("Location", mLocation);
                    args.putString("Description", mDescription);
                    args.putInt("Followers", mFollowers);
                    args.putInt("Following", mFollowing);

                    UserProfileFragment userProfileFragment = new UserProfileFragment();
                    userProfileFragment.setArguments(args);
                    replaceFragment(userProfileFragment);
                });
        });
        return view;
    }

    private void uploadToDatabase(String location, String description) {
        Thread thread = new Thread(() -> {
            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                        .appendPath("InsertPost")
                        .build();

                URL url = new URL(uri.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("PostLocation", location);
                jsonParam.put("Email", mEmail);
                jsonParam.put("Description", description);
                jsonParam.put("Likes", 1);
                jsonParam.put("Views", 1);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = Objects.requireNonNull(getActivity()).getContentResolver()
                .query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
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
