package cjkim00.imagesharingapplicationfinal.Profile;


import android.content.Context;
import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cjkim00.imagesharingapplicationfinal.R;

import static android.app.Activity.RESULT_OK;


public class EditProfileFragment extends Fragment {

    private boolean mExists = false;
    private boolean isActivityCalled;

    private String mEmail;
    private String mUsername;
    private String mDescription;
    private String mLocation;

    private ImageView mEditProfileImage;
    private EditText mEditEmail;
    private EditText mEditUsername;
    private EditText mEditPassword;
    private EditText mEditDescription;

    private OnProfileUpdatedListener mChangeListener;
    private OnEmailUpdatedListener mEmailChangedListener;
    private OnFinishedWithFragmentListener mFinishedListener;
    private OnProfileImageChangedListener mProfileImageListener;

    public EditProfileFragment() {
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

        isActivityCalled = false;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mEditProfileImage =
                view.findViewById(R.id.imageView_edit_profile_image_fragment_edit_profile);
        mEditEmail = view.findViewById(R.id.editText_edit_email_fragment_edit_profile);
        mEditUsername = view.findViewById(R.id.editText_edit_username_fragment_edit_profile);
        mEditPassword = view.findViewById(R.id.editText_edit_password_fragment_edit_profile);
        mEditDescription = view.findViewById(R.id.editText_edit_description_fragment_edit_profile);
        Button mFinishEditButton
                = view.findViewById(R.id.button_complete_edit_fragment_edit_profile);

        setProfileImage(mEditProfileImage, mLocation);
        mEditEmail.setText(mEmail);
        mEditUsername.setText(mUsername);
        mEditDescription.setText(mDescription);

        mFinishEditButton.setOnClickListener(v -> {
            if(checkForChanges()) {
                mChangeListener.onProfileUpdated(mEditUsername.getText().toString(),
                        mEditDescription.getText().toString());
            } else {
                mFinishedListener.onFinishedButtonPressed();
            }
        });

        mEditProfileImage.setOnClickListener(v -> {
            isActivityCalled = true;

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            photoPickerIntent.putExtra("crop", "true");
            photoPickerIntent.putExtra("aspectX", 0);
            photoPickerIntent.putExtra("aspectY", 0);
            startActivityForResult(photoPickerIntent, 1);
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof OnProfileUpdatedListener) {
            mChangeListener = (OnProfileUpdatedListener) context;
        }
        if(context instanceof OnEmailUpdatedListener) {
            mEmailChangedListener = (OnEmailUpdatedListener) context;
        }
        if(context instanceof OnFinishedWithFragmentListener) {
            mFinishedListener = (OnFinishedWithFragmentListener) context;
        }
        if(context instanceof OnProfileImageChangedListener) {
            mProfileImageListener = (OnProfileImageChangedListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mChangeListener = null;
        mEmailChangedListener = null;
        mFinishedListener = null;
        mProfileImageListener = null;
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

    private boolean checkForChanges() {
        boolean exitFragment = false;
        String username = mEditUsername.getText().toString();
        if(username.length() > 0) {
            try {
                if(!checkIfUsernameExists(username)) {
                    updateUsername(username);
                    Log.i("MSG5", "Update username");
                    exitFragment = true;
                } else {
                    Log.i("MSG5", "Did not update username");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            mEditUsername.setError("Username cannot be empty;");
            Log.i("MSG5", "Did not update username");
        }

        String email = mEditEmail.getText().toString();
        if(isValidEmail(email)) {
            if(!email.equals(mEmail)) {
                updateEmail(email);
                Log.i("MSG5", "Update email");
                exitFragment = true;
            } else {
                Log.i("MSG5", "Did not update email");
            }
        } else {
            mEditEmail.setError("Input a valid email address.");
        }

        String description = mEditDescription.getText().toString();
        if(!description.equals(mDescription)) {
            updateDescription(description);
            Log.i("MSG5", "Update Description");
            exitFragment = true;
        } else {
            Log.i("MSG5", "Did not update Description");
        }

        return exitFragment;
    }

    private static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


    private void updateEmail(String newEmail) {
        Thread thread = new Thread(() -> {
            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                        .appendPath("update_email")
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
                jsonParam.put("New_Email", newEmail);
                jsonParam.put("Email", mEmail);
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

    private void updateUsername(String newUsername) {
        Thread thread = new Thread(() -> {
            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                        .appendPath("update_username")
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
                jsonParam.put("New_Username", newUsername);
                jsonParam.put("Email", mEmail);
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                Log.i("MSG2", "STATUS: " + conn.getResponseCode());
                Log.i("MSG2" , "MESSAGE: " + conn.getResponseMessage());
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


    private void updateDescription(String newDescription)  {
        Thread thread = new Thread(() -> {
            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                        .appendPath("update_description")
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
                jsonParam.put("New_Description", newDescription);
                jsonParam.put("Email", mEmail);
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                Log.i("MSG2", "STATUS: " + conn.getResponseCode());
                Log.i("MSG2" , "MESSAGE: " + conn.getResponseMessage());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        if (resultCode == RESULT_OK && isActivityCalled) {
            final Uri imageUri = data.getData();
            StorageReference reference = storageRef.child(getRealPathFromURI(imageUri));
            mEditProfileImage.setImageURI(imageUri);
            reference.putFile(Objects.requireNonNull(imageUri))
                    .addOnSuccessListener(taskSnapshot ->
                            Toast.makeText(Objects.requireNonNull(getActivity())
                                            .getApplicationContext()
                            ,getRealPathFromURI(imageUri),Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(exception -> Toast
                            .makeText(Objects.requireNonNull(getActivity()).getApplicationContext()
                            ,"Image not uploaded",Toast.LENGTH_SHORT).show())
                    .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                                uploadToDatabase(getRealPathFromURI(imageUri));
                                isActivityCalled = false;
                                mProfileImageListener.onProfileImageChanged(imageUri);
                            });
        }
    }

    private void uploadToDatabase(String location) {
        Thread thread = new Thread(() -> {

            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                        .appendPath("change_profile_picture")
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
                jsonParam.put("User", mEmail);
                jsonParam.put("ImageLocation", location);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                Log.i("MSG", "STATUS: " + conn.getResponseCode());
                Log.i("MSG" , "MESSAGE: " + conn.getResponseMessage());
                //conn.connect();



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


    private boolean checkIfUsernameExists(String username) throws InterruptedException {
        Thread thread = new Thread(() -> {

            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                        .appendPath("check_if_username_exists")
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
                jsonParam.put("User", username);
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                int status = conn.getResponseCode();
                Log.i("MSG", "STATUS: " + os.toString());
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
                        mExists = getResults(sb.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        return mExists;
    }

    private boolean getResults(String result) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success") && root.getBoolean("success") ) {
                //JSONObject response = root.getJSONObject("success");
                JSONArray data = root.getJSONArray("data");
                JSONObject jsonObject = data.getJSONObject(0);
                return jsonObject.getBoolean("exists");

            } else {
                Log.i("MSG", "No response");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("MSG", e.getMessage());
        }
        return false;
    }


    public interface OnProfileUpdatedListener {
        void onProfileUpdated(String newUsername, String newDescription);
    }

    public interface OnFinishedWithFragmentListener {
        void onFinishedButtonPressed();
    }

    public interface  OnEmailUpdatedListener {
        void onEmailUpdated(String newEmail);
    }

    public interface OnProfileImageChangedListener {
        void onProfileImageChanged(Uri imageUri);
    }

}


