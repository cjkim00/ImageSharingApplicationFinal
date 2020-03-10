package cjkim00.imagesharingapplicationfinal.Profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import cjkim00.imagesharingapplicationfinal.Post.ViewLikedPostsFragment;
import cjkim00.imagesharingapplicationfinal.Post.ViewUserPostsFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return new ViewUserPostsFragment();
            case 1:
                return new ViewLikedPostsFragment();
            default:
                return null;

        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0:
                return "Posts";
            case 1:
                return "Liked Posts";
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }
}
