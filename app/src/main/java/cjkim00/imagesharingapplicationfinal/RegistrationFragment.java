package cjkim00.imagesharingapplicationfinal;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cjkim00.imagesharingapplicationfinal.ImageView.ImageViewerActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationFragment extends Fragment {


    private final int PASSWORD_MIN_LENGTH = 8;

    private FirebaseAuth mAuth;

    private EditText mEmail;
    private EditText mUsername;
    private EditText mPassword;
    private EditText mSecondPassword;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_registration, container, false);

        mEmail = v.findViewById(R.id.editText_email_registration);
        mUsername = v.findViewById(R.id.editText_username_registration_fragment);
        mPassword = v.findViewById(R.id.editText_password_registration);
        mSecondPassword = v.findViewById(R.id.editText_second_password_registration);

        Button b = v.findViewById(R.id.button_finish_registration);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEditTextFields()) {
                    registerUser();
                }

            }
        });
        return v;
    }

    public void onLoginSuccess(String email) {
        Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
        intent.putExtra("email", email);
        getActivity().startActivity(intent);
    }

    public void registerUser() {
        String email = mEmail.getText().toString();
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //onLoginSuccess(user.getEmail());
                            uploadToDatabase();
                            replaceFragment(new LoginFragment());
                        } else {
                            // If sign in fails, display a message to the user.
                            FirebaseAuthException e = (FirebaseAuthException )task.getException();
                            Log.w("Authentication Failed", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed." + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    public boolean checkPasswordLength() {
        String password = mPassword.getText().toString();
        if(password.length() >= PASSWORD_MIN_LENGTH) {
            return true;
        }
        return false;
    }

    public boolean checkIfPasswordsMatch() {
        if(mPassword.getText().toString().equals(mSecondPassword.getText().toString())) {
            return true;
        }
        return false;

    }


    public boolean checkEditTextFields() {
        boolean returnBool = true;
        String value = mEmail.getText().toString();
        if(value.length() == 0) {
            mEmail.setError("Cannot be empty.");
            returnBool = false;
        }

        value = mUsername.getText().toString();
        if(value.length() == 0) {
            mUsername.setError("Cannot be empty.");
        }

        value = mPassword.getText().toString();
        if(!(value.length() == 0)) {
            if(checkPasswordLength()) {
                if(!checkIfPasswordsMatch()) {
                    mSecondPassword.setError("Passwords must match.");
                }
            } else {
                mPassword.setError("Password must be at least 8 characters.");
                returnBool = false;
            }
        } else {
            mPassword.setError("Cannot be empty.");
            returnBool = false;
        }

        value = mSecondPassword.getText().toString();
        if(value.length() == 0) {
            mSecondPassword.setError("Cannot be empty.");
            returnBool = false;
        }
        return returnBool;
    }

    public void uploadToDatabase() {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {

                try {
                    HttpURLConnection urlConnection = null;
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                            .appendPath("Registration")
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
                    jsonParam.put("Email", mEmail.getText().toString());
                    jsonParam.put("Username", mUsername.getText().toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());
                    os.flush();
                    os.close();

                    Log.i("MSG", "STATUS: " + String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , "MESSAGE: " + conn.getResponseMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void replaceFragment(androidx.fragment.app.Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager)
                .beginTransaction();

        fragmentTransaction.replace(((ViewGroup)(Objects.requireNonNull(getView()).getParent()))
                .getId(), fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
        fragmentManager.popBackStack();
    }

}
