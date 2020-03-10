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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import cjkim00.imagesharingapplicationfinal.LoadingFragment;
import cjkim00.imagesharingapplicationfinal.Post.Post;
import cjkim00.imagesharingapplicationfinal.Post.PostFragment;
import cjkim00.imagesharingapplicationfinal.Post.UploadPostFragment;
import cjkim00.imagesharingapplicationfinal.Post.ViewLikedPostsFragment;
import cjkim00.imagesharingapplicationfinal.Post.ViewUserPostsFragment;
import cjkim00.imagesharingapplicationfinal.Profile.EditProfileFragment;
import cjkim00.imagesharingapplicationfinal.Profile.ProfileFragment;
import cjkim00.imagesharingapplicationfinal.Profile.UserProfileFragment;
import cjkim00.imagesharingapplicationfinal.R;
import cjkim00.imagesharingapplicationfinal.Search.Member;
import cjkim00.imagesharingapplicationfinal.Search.SearchFragment;

public class ImageViewerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PostFragment.OnListFragmentInteractionListener,
        SearchFragment.OnListFragmentInteractionListener,
        ProfileFragment.OnListFragmentInteractionListener,
        ManageFragment.OnListFragmentInteractionListener,
        ViewFollowersFragment.OnListFragmentInteractionListener,
        ViewUserPostsFragment.OnListFragmentInteractionListener,
        ViewLikedPostsFragment.OnListFragmentInteractionListener,
        EditProfileFragment.OnProfileUpdatedListener,
        EditProfileFragment.OnEmailUpdatedListener,
        EditProfileFragment.OnFinishedWithFragmentListener,
        EditProfileFragment.OnProfileImageChangedListener,
        LoadingFragment.OnFragmentInteractionListener {

    private boolean isActivityCalled;
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

    FloatingActionButton mFab;




    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        isActivityCalled = false;
        //addLoadingFragment();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        mUserProfileImage = headerView.findViewById(R.id.imageView_user_profile_image_nav_header_image_viewer);
        mUserUsername = headerView.findViewById(R.id.textView_user_username_nav_header_image_viewer);
        mUserFollowInfo = headerView.findViewById(R.id.textView_user_follow_info_nav_header_image_viewer);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = findViewById(R.id.fab);
        mFab.setImageResource(R.drawable.ic_add_white_24dp);
        mFab.setOnClickListener(view -> {
            isActivityCalled = true;

            mFab.setVisibility(View.GONE);
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            photoPickerIntent.putExtra("crop", "true");
            photoPickerIntent.putExtra("aspectX", 0);
            photoPickerIntent.putExtra("aspectY", 0);
            startActivityForResult(photoPickerIntent, 1);


        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        mUser = (FirebaseUser) Objects.requireNonNull(intent.getExtras()).get("User");
        mEmail = Objects.requireNonNull(mUser).getEmail();
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            mFab.setVisibility(View.VISIBLE);
            Bundle args = new Bundle();
            args.putString("Email", mEmail);
            PostFragment postFragment = new PostFragment();
            postFragment.setArguments(args);
            replaceFragment(postFragment);
        } else if (id == R.id.nav_profile) {
            mFab.setVisibility(View.GONE);
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
            mFab.setVisibility(View.GONE);
            Bundle args = new Bundle();
            args.putString("Email", mEmail);
            ManageFragment manageFragment = new ManageFragment();
            manageFragment.setArguments(args);
            replaceFragment(manageFragment);
        } else if (id == R.id.nav_view_followers) {
            mFab.setVisibility(View.GONE);
            Bundle args = new Bundle();
            args.putString("Email", mEmail);
            ViewFollowersFragment viewFollowersFragment = new ViewFollowersFragment();
            viewFollowersFragment.setArguments(args);
            replaceFragment(viewFollowersFragment);

        } else if (id == R.id.nav_search) {
            mFab.setVisibility(View.GONE);
            replaceFragment(new SearchFragment());
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setUserInfo() {
        Thread thread = new Thread(() -> {

            try {
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
                        getResults(sb.toString());
                }
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

    public void getResults(String result) {
        try {
            JSONObject root = new JSONObject(result);
            JSONArray data = root.getJSONArray("data");
            JSONObject jsonObject = data.getJSONObject(0);

            mUsername = jsonObject.getString("username");
            mProfileDescription = jsonObject.getString("profiledescription");
            mProfileImageLocation = jsonObject.getString("profileimagelocation");
            mFollowers = jsonObject.getInt("followerstotal");
            mFollowing = jsonObject.getInt("followingtotal");
            Log.i("MSG3" , mEmail + ", "  + mUsername + ", " + mProfileDescription + ", "
                    + mProfileImageLocation + ", " + mFollowers
                    + ", " + mFollowing);

            mUserUsername.setText(mUsername);
            mUserFollowInfo.setText("Followers: " + mFollowers + " Following: " + mFollowing);
            getUserProfileImage(mProfileImageLocation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getUserProfileImage(String location) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(location);
        //final long ONE_MEGABYTE = 1024 * 1024;
        final long FIFTEEN_MEGABYTES = 15360 * 15360;
        imageRef.getBytes(FIFTEEN_MEGABYTES).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            mUserProfileImage.setImageBitmap(bitmap);
        }).addOnFailureListener(exception -> {
            //create empty bitmap to prevent crashes
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
    public void addLoadingFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_image_viewer, new LoadingFragment(), "LOAD")
                .addToBackStack(null)
                .commit();
    }
    @Override
    public void removeLoadingFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(Objects.requireNonNull(getSupportFragmentManager().findFragmentByTag("LOAD")))
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && isActivityCalled) {
            final Uri imageUri = data.getData();
            try {
                isActivityCalled = false;

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                bitmap.recycle();

                Bundle args = new Bundle();
                args.putByteArray("Image", byteArray);
                args.putString("Email", mEmail);
                args.putString("Username", mUsername);
                args.putString("Location", getRealPathFromURI(imageUri));
                args.putString("Description", mProfileDescription);
                args.putInt("Followers", mFollowers);
                args.putInt("Following", mFollowing);
                args.putString("Uri", Objects.requireNonNull(imageUri).toString());
                Log.i("MSG7", "ORIGINAL: " + imageUri.toString());

                UploadPostFragment uploadPostFragment = new UploadPostFragment();
                uploadPostFragment.setArguments(args);
                uploadPostFragment.setArguments(args);
                addFragment(uploadPostFragment);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
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
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        View focusedView = this.getCurrentFocus();

        if(focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static String getEmail() {
        return mEmail;
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

    @Override
    public void onProfileUpdated(String newUsername, String newDesctiption) {
        mUsername = newUsername;
        mProfileDescription = newDesctiption;
        mUserUsername.setText(mUsername);

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
    }

    @Override
    public void onFinishedButtonPressed() {
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
    }


    @Override
    public void onEmailUpdated(String newEmail) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.i("MSG4", "EMAIL: " + Objects.requireNonNull(user).getEmail());
        user.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("MSG4", "User email address updated.");
                    }
                });

        mUser = user;
        mEmail = mUser.getEmail();
    }

    @Override
    public void onProfileImageChanged(Uri imageUri) {
        mUserProfileImage.setImageURI(imageUri);
    }


    /*
    @Override
    public void onListFragmentInteraction(Post post) {
        //populate full post fragment
    }
    */
}
