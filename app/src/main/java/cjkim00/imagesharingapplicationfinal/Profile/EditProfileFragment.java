package cjkim00.imagesharingapplicationfinal.Profile;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cjkim00.imagesharingapplicationfinal.R;
import cjkim00.imagesharingapplicationfinal.Search.Member;
import cjkim00.imagesharingapplicationfinal.Search.MySearchRecyclerViewAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    private boolean mExists = false;

    private String mEmail;
    private String mUsername;
    private String mDescription;
    private String mLocation;

    private ImageView mEditProfileImage;
    private EditText mEditEmail;
    private EditText mEditUsername;
    private EditText mEditPassword;
    private EditText mEditDescription;
    private Button mFinishEditButton;

    private OnProfileUpdatedListener mChangeListener;
    private OnEmailUpdatedListener mEmailChangedListener;
    private OnFinishedWithFragmentListener mFinishedListener;

    public EditProfileFragment() {
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
        mFinishEditButton = view.findViewById(R.id.button_complete_edit_fragment_edit_profile);

        setProfileImage(mEditProfileImage, mLocation);
        mEditEmail.setText(mEmail);
        mEditUsername.setText(mUsername);
        mEditDescription.setText(mDescription);

        mFinishEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check for changes
                //commit those changes to firebase and the server
                //go back to the user's profile page with the edited information by creating a new user profile fragmenmt
                //updateUsername(mEditUsername.getText().toString());
                //updateDescription(mEditDescription.getText().toString());
//                mEmailChangedListener.onEmailUpdated(mEditEmail.getText().toString());
//                updateEmail(mEditEmail.getText().toString());
                if(checkForChanges()) {
                    mChangeListener.onProfileUpdated(mEditUsername.getText().toString(), mEditDescription.getText().toString());
                } else {
                    mFinishedListener.onFinishedButtonPressed();
                }
            }
        });

        mEditProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //activity to select image from gallery
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mChangeListener = null;
        mEmailChangedListener = null;
        mFinishedListener = null;
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

    public boolean checkForChanges() {
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

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


    public void updateEmail(String newEmail) {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection urlConnection = null;
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
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void updateUsername(String newUsername) {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection urlConnection = null;
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

                    Log.i("MSG2", "STATUS: " + String.valueOf(conn.getResponseCode()));
                    Log.i("MSG2" , "MESSAGE: " + conn.getResponseMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void updateDescription(String newDescription)  {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection urlConnection = null;
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

                    Log.i("MSG2", "STATUS: " + String.valueOf(conn.getResponseCode()));
                    Log.i("MSG2" , "MESSAGE: " + conn.getResponseMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
//    public void updateDescription(String newDescription) {
//        Thread thread = new Thread( new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//                    HttpURLConnection urlConnection = null;
//                    Uri uri = new Uri.Builder()
//                            .scheme("https")
//                            .appendPath("cjkim00-image-sharing-app.herokuapp.com")
//                            .appendPath("update_description")
//                            .build();
//
//                    URL url = new URL(uri.toString());
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    conn.setRequestMethod("POST");
//                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
//                    conn.setRequestProperty("Accept","application/json");
//                    conn.setDoOutput(true);
//                    conn.setDoInput(true);
//                    conn.setConnectTimeout(15000);
//                    conn.setReadTimeout(15000);
//
//                    JSONObject jsonParam = new JSONObject();
//                    jsonParam.put("New_Description", newDescription);
//                    jsonParam.put("Email", mEmail);
//                    Log.i("MSG5", "STATUS: " + String.valueOf(conn.getResponseCode()));
//                    Log.i("MSG5" , "MESSAGE: " + conn.getResponseMessage());
//                    Log.i("MSG5" , "EMAIL: " + mEmail + ", DESC: " + newDescription);
//                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//                    os.writeBytes(jsonParam.toString());
//                    os.flush();
//                    os.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    private boolean checkIfUsernameExists(String username) throws InterruptedException {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {

                try {
                    HttpURLConnection urlConnection = null;
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
//                            if(!exists) {
//                                updateUsername(username);
//                                Log.i("MSG5", "username changed");
//                            } else {
//                                mEditUsername.setError("Username already exists.");
//                                Log.i("MSG5", "username exists");
//                            }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
        return mExists;
    }

    public boolean getResults(String result) {
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

}


