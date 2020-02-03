package cjkim00.imagesharingapplicationfinal.ImageView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cjkim00.imagesharingapplicationfinal.Post.Post;
import cjkim00.imagesharingapplicationfinal.Post.PostFragment;
import cjkim00.imagesharingapplicationfinal.ProfileFragment;
import cjkim00.imagesharingapplicationfinal.R;
import cjkim00.imagesharingapplicationfinal.Search.Member;
import cjkim00.imagesharingapplicationfinal.Search.SearchFragment;
import cjkim00.imagesharingapplicationfinal.Settings.ManageFragment;

public class ImageViewerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PostFragment.OnListFragmentInteractionListener,
        SearchFragment.OnListFragmentInteractionListener,
        ProfileFragment.OnListFragmentInteractionListener {

    private StorageReference mStorageRef;
    public String[] test = {"one", "two", "three", "four", "five"};
    public String mEmail;

    FirebaseUser mUser;

    ImageView mUserProfileImage;
    TextView mUserUsername;
    TextView mUserFollowInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        mUserProfileImage = findViewById(R.id.imageView_user_profile_image_nav_header_image_viewer);
        mUserUsername = findViewById(R.id.textView_user_username_nav_header_image_viewer);
        mUserFollowInfo = findViewById(R.id.textView_user_follow_info_nav_header_image_viewer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        mUser = (FirebaseUser) intent.getExtras().get("User");
        mEmail = mUser.getEmail();

        setUserInfo();

        addFragment(new PostFragment());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_viewer, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            replaceFragment(new PostFragment());
        } else if (id == R.id.nav_profile) {
            //replaceFragment(new ProfileFragment());
        } else if (id == R.id.nav_view_followers) {
            replaceFragment(new ManageFragment());
        } else if (id == R.id.nav_search) {
            replaceFragment(new SearchFragment());
        } else if (id == R.id.nav_liked_posts) {

        } else if (id == R.id.nav_settings) {
            //replaceFragment(new SettingsFragment());
        } else if (id == R.id.nav_log_out) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setUserInfo() {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {

                try {
                    HttpURLConnection urlConnection = null;
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                            .appendPath("get_user_info")
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
                    jsonParam.put("User", mUser.getEmail());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());
                    os.flush();
                    os.close();

                    int status = conn.getResponseCode();
                    Log.i("MSG1", "STATUS: " + os.toString());
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
                            Log.i("MSG1", sb.toString());
                            getResults(sb.toString());
                    }

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

    public void getResults(String result) {
        try {
            JSONObject root = new JSONObject(result);
            //if (root.has("success") && root.getBoolean("success") ) {
                //JSONObject response = root.getJSONObject("success");
                JSONArray data = root.getJSONArray("data");
                for(int i = 0; i < data.length(); i++) {
                    JSONObject jsonObject = data.getJSONObject(0);
                    mUserUsername.setText(jsonObject.getString("username"));
                    mUserFollowInfo.setText("Followers: " + jsonObject.getInt("followingtotal")
                            + " Following: " + jsonObject.getInt("followingtotal"));
                    getUserProfileImage(jsonObject.getString("profileimagelocation"));
                    Log.i("MSG1", "Username: " + jsonObject.getString("Username"));
                }

            //} else {
                Log.i("MSG1", "No response");
            //}
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("MSG1", e.getMessage());
        }
    }

    public void getUserProfileImage(String location) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(location);
        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mUserProfileImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //create empty bitmap to prevent crashes
            }
        });
    }

    public void replaceFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager)
                .beginTransaction();
        fragmentTransaction.replace(R.id.layout_image_viewer, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    public void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager)
                .beginTransaction();
        fragmentTransaction.add(R.id.layout_image_viewer, fragment);
        //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    public void replaceSearchFragment(Fragment fragment, Bundle args) {
        if(args != null) {
            fragment.setArguments(args);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager)
                .beginTransaction();
        fragmentTransaction.add(R.id.layout_image_viewer, fragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        //Toast.makeText(getApplicationContext(),"Image uploaded",Toast.LENGTH_SHORT).show();

        if (resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            StorageReference riversRef = mStorageRef.child(getRealPathFromURI(imageUri));


            riversRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext()
                                    ,getRealPathFromURI(imageUri),Toast.LENGTH_SHORT).show();
                            //downloadFileTest(getRealPathFromURI(imageUri));

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(getApplicationContext()
                                    ,"Image not uploaded",Toast.LENGTH_SHORT).show();

                            // Handle unsuccessful uploads
                            // ...
                        }
                    }).addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    //downloadFileTest(getRealPathFromURI(imageUri));
                    uploadToDatabase(getRealPathFromURI(imageUri));

                }
            });

        }
    }

    public void uploadToDatabase(String location) {
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {

                try {
                    HttpURLConnection urlConnection = null;
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
                    jsonParam.put("Description", "Temporary Value");
                    jsonParam.put("Likes", 1);
                    jsonParam.put("Views", 1);
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());
                    os.flush();
                    os.close();

                    Log.i("MSG", "STATUS: " + String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , "MESSAGE: " + conn.getResponseMessage());
                    //conn.connect();



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver()
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


    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
        View focusedView = this.getCurrentFocus();

        if(focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }


    @Override
    public void onListFragmentInteraction(Post post) {
        Bundle args = new Bundle();
        args.putString("Description", post.getDescription());
        args.putString("Location", post.getImageLocation());
        args.putInt("Views", post.getViews());
        args.putInt("Likes", post.getLikes());
        args.putInt("PostID", post.getPostID());
        args.putByteArray("Array", post.getByteArray());

        //FullPostFragment fullPostFragment = new FullPostFragment();
        //fullPostFragment.setArguments(args);
        //replaceFragment(fullPostFragment);
    }

    @Override
    public void onListFragmentInteraction(Member member) {
        hideSoftKeyboard();

        EditText search = findViewById(R.id.editText_search_fragment_search);
        search.clearFocus();
        Bundle args = new Bundle();
        args.putString("CurrentUser", mUser.getEmail());
        args.putString("Username", member.getUsername());
        args.putString("Description", member.getDescription());
        args.putString("Location", member.getProfileImageLocation());
        args.putInt("Followers", member.getFollowers());
        args.putInt("Following", member.getFollowing());
        replaceSearchFragment(new ProfileFragment(), args);
    }

    @Override
    public void onListFragmentInteraction(Post post, List<Post> posts) {
//        ShowProfileImagesFragment showProfileImagesFragment = new ShowProfileImagesFragment();
//        Bundle args = new Bundle();
//        args.putSerializable("post", post);
//        args.putSerializable("posts", (Serializable) posts);
//        showProfileImagesFragment.setArguments(args);
//        replaceFragment(showProfileImagesFragment);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /*
    @Override
    public void onListFragmentInteraction(Post post) {
        //populate full post fragment
    }
    */
}
