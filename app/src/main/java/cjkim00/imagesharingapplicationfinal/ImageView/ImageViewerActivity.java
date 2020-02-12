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
import java.util.ArrayList;
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
import cjkim00.imagesharingapplicationfinal.Follow.ManageFragment;
import cjkim00.imagesharingapplicationfinal.Follow.ViewFollowersFragment;
import cjkim00.imagesharingapplicationfinal.Post.Post;
import cjkim00.imagesharingapplicationfinal.Post.PostFragment;
import cjkim00.imagesharingapplicationfinal.Post.ViewLikedPostsFragment;
import cjkim00.imagesharingapplicationfinal.Post.ViewUserPostsFragment;
import cjkim00.imagesharingapplicationfinal.Profile.ProfileFragment;
import cjkim00.imagesharingapplicationfinal.R;
import cjkim00.imagesharingapplicationfinal.Search.Member;
import cjkim00.imagesharingapplicationfinal.Search.SearchFragment;
import cjkim00.imagesharingapplicationfinal.Profile.UserProfileFragment;

public class ImageViewerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PostFragment.OnListFragmentInteractionListener,
        SearchFragment.OnListFragmentInteractionListener,
        ProfileFragment.OnListFragmentInteractionListener,
        ManageFragment.OnListFragmentInteractionListener,
        ViewFollowersFragment.OnListFragmentInteractionListener,
        ViewUserPostsFragment.OnListFragmentInteractionListener,
        ViewLikedPostsFragment.OnListFragmentInteractionListener {

    private StorageReference mStorageRef;
    public String[] test = {"one", "two", "three", "four", "five"};
    public static String mEmail;
    private String mProfileDescription;
    private String mUsername;
    private String mProfileImageLocation;
    private int mFollowers;
    private int mFollowing;

    FirebaseUser mUser;

    ImageView mUserProfileImage;
    TextView mUserUsername;
    TextView mUserFollowInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        mUserProfileImage = headerView.findViewById(R.id.imageView_user_profile_image_nav_header_image_viewer);
        mUserUsername = headerView.findViewById(R.id.textView_user_username_nav_header_image_viewer);
        mUserFollowInfo = headerView.findViewById(R.id.textView_user_follow_info_nav_header_image_viewer);


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

        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        mUser = (FirebaseUser) intent.getExtras().get("User");
        mEmail = mUser.getEmail();
        Log.i("MSG1", "Email: " + mUser.getEmail() + ", " + mEmail);
        setUserInfo();
        PostFragment postFragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString("Email", mEmail);
        postFragment.setArguments(args);
        addFragment(postFragment);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Bundle args = new Bundle();
            args.putString("Email", mEmail);
            PostFragment postFragment = new PostFragment();
            postFragment.setArguments(args);
            replaceFragment(postFragment);
        } else if (id == R.id.nav_profile) {
            Bundle args = new Bundle();
            args.putString("Email", mEmail);
            args.putString("Username", mUsername);
            args.putString("Location", mProfileImageLocation);
            args.putString("Description", mProfileDescription);
            args.putInt("Followers", mFollowers);
            args.putInt("Following", mFollowing);
            UserProfileFragment userProfileFragment =  new UserProfileFragment();
            userProfileFragment.setArguments(args);
            replaceFragment(userProfileFragment);
        } else if (id == R.id.nav_view_following) {
            Bundle args = new Bundle();
            args.putString("Email", mEmail);
            ManageFragment manageFragment = new ManageFragment();
            manageFragment.setArguments(args);
            replaceFragment(manageFragment);
        } else if (id == R.id.nav_view_followers) {
            Bundle args = new Bundle();
            args.putString("Email", mEmail);
            ViewFollowersFragment viewFollowersFragment = new ViewFollowersFragment();
            viewFollowersFragment.setArguments(args);
            replaceFragment(viewFollowersFragment);

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
                            Log.i("MSG1", "Array in String format: " + sb.toString());
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


            JSONObject jsonObject = data.getJSONObject(0);

            mUsername = jsonObject.getString("username");
            mProfileDescription = jsonObject.getString("profiledescription");
            mProfileImageLocation = jsonObject.getString("profileimagelocation");
            mFollowers = jsonObject.getInt("followerstotal");
            mFollowing = jsonObject.getInt("followingtotal");

            mUserUsername.setText(mUsername);
            mUserFollowInfo.setText("Followers: " + mFollowers
                    + " Following: " + mFollowing);
            getUserProfileImage(mProfileImageLocation);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getUserProfileImage(String location) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(location);
        final long ONE_MEGABYTE = 1024 * 1024;
        final long FIFTEEN_MEGABYTES = 15360 * 15360;
        imageRef.getBytes(FIFTEEN_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    public void replaceFragmentAndAddToBackStack(Fragment fragment) {
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
        if(search != null) {
            search.clearFocus();
        }

        Bundle args = new Bundle();
        args.putString("CurrentUser", mUser.getEmail());
        args.putString("Username", member.getUsername());
        args.putString("Description", member.getDescription());
        args.putString("Location", member.getProfileImageLocation());
        args.putInt("Followers", member.getFollowers());
        args.putInt("Following", member.getFollowing());
        Log.i("MSG" , "Search fragment interaction listener");
        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(args);
        replaceFragmentAndAddToBackStack(profileFragment);
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

    @Override
    public void onListFragmentInteraction(Post post, ArrayList<Post> list) {

    }

    /*
    @Override
    public void onListFragmentInteraction(Post post) {
        //populate full post fragment
    }
    */
}
