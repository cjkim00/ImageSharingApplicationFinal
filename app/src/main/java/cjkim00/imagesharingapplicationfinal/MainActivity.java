package cjkim00.imagesharingapplicationfinal;


import android.os.Bundle;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceFragment(new LoginFragment());
    }

    //method to change fragment to the login fragment
    //should check if the device has been logged in before


    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager)
                .beginTransaction();
        fragmentTransaction.replace(R.id.layout_main, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }


}
