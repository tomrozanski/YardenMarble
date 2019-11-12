package com.FinalProject.TomAlon.YardenShaish;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.FinalProject.TomAlon.YardenShaish.Dialogs.AddInstallDialogActivity;
import com.FinalProject.TomAlon.YardenShaish.Fragments.InstallListFragment;
import com.FinalProject.TomAlon.YardenShaish.Fragments.InstallersMapFragment;
import com.FinalProject.TomAlon.YardenShaish.Install.Install;
import com.FinalProject.TomAlon.YardenShaish.Install.StatusEnum;

public class AdminActivity extends FragmentActivity implements InstallListFragment.OnInstallUpdated {
    private final static String TAG = AdminActivity.class.getSimpleName();
    private static final int NUM_INSTALLS = 2;

    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Fade());
        getWindow().setEnterTransition(new Fade());

        setContentView(R.layout.activity_admin);


        mPager = findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);

        FloatingActionButton addInstallFab = findViewById(R.id.add_install_fab);
        addInstallFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "add fab clicked");
                openAddInstallDialog();
            }
        });
    }

    private void openAddInstallDialog() {
        Intent intent = new Intent(getApplicationContext(), AddInstallDialogActivity.class);
        startActivity(intent);
    }

    @Override
    public void updateHolderViaParentActivity(InstallListFragment.ContactViewHolder holder, Install install, int position) {
        StatusEnum status = install.getStatusEnum();
        holder.setStatusTextAndColor(status);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return InstallListFragment.newInstance();
                case 1:
                    return InstallersMapFragment.newInstance();
                default:
                    throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int getCount() {
            return NUM_INSTALLS;
        }
    }

}
