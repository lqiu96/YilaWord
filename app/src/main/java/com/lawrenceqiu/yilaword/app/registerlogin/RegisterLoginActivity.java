package com.lawrenceqiu.yilaword.app.registerlogin;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.lawrenceqiu.yilaword.app.R;
import com.lawrenceqiu.yilaword.app.viewpagerslidingtab.SlidingTabLayout;

/**
 * Created by Lawrence on 6/10/2015.
 */
public class RegisterLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_login_main);
        RegisterLoginActivity.LoginRegisterAdapter loginRegisterAdapter = new LoginRegisterAdapter(getSupportFragmentManager());
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(loginRegisterAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.registerLoginToolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        /*
        Based on the google IO conference free class file
         */
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.slidingTabLayout);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        slidingTabLayout.setViewPager(viewPager);
    }

    /**
     * Adapter for the ViewPager to navigate between the two fragments
     * FragmentPageAdapter is used because there is always only 2 pages
     */
    private class LoginRegisterAdapter extends FragmentPagerAdapter {
        private final int NUM_FRAGMENTS = 2;

        public LoginRegisterAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new LoginFragment();
                case 1:
                    return new RegistrationFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }

        /**
         * Sets the title based on the position
         *
         * @param position Position of the Screen
         * @return String of the title
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Login";
                case 1:
                    return "Registration";
                default:
                    return null;
            }
        }
    }
}
