package cjkim00.imagesharingapplicationfinal.Profile;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import cjkim00.imagesharingapplicationfinal.R;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment extends Fragment {


    public TabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstatnceState) {
        super.onCreate(savedInstatnceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_tab, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout_profile_tabs_fragment_tab);

        ViewPager viewPager = view.findViewById(R.id.viewPager_fragment_tab);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        Log.i("TEST LOG ", "tab fragment on create view");
        return view;
    }

}
