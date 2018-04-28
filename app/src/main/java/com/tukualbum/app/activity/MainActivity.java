package com.tukualbum.app.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.tukualbum.app.R;
import com.tukualbum.app.common.BaseActivity;
import com.tukualbum.app.fragment.AllMediaFragment;
import com.tukualbum.app.fragment.ImageFragment;
import com.tukualbum.app.fragment.OnlineMediaFragment;
import com.tukualbum.app.fragments.AlbumsFragment;

import butterknife.BindView;

public class MainActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.vp_container)
    ViewPager mViewPager;
    @BindView(R.id.tb_title)
    TabLayout mTabLayout;
    private String[] mTitles = new String[]{"最近", "相册", "在线"};
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
    }

    @Override
    protected void initView() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mToolbar.inflateMenu(R.menu.menu_black_white_list);
        for (int i = 0; i < mTitles.length; i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(mTitles[i]));
        }
        mTabLayout.setupWithViewPager(mViewPager);//给TabLayout设置关联ViewPager，如果设置了ViewPager，那么ViewPagerAdapter中的getPageTitle()方法返回的就是Tab上的标题
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private ImageFragment imageFragment;
        private AllMediaFragment allMediaFragment;
        private OnlineMediaFragment onlineMediaFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (allMediaFragment == null) allMediaFragment = new AllMediaFragment();
                    return allMediaFragment;
                case 1:
                    if (imageFragment == null) imageFragment = new ImageFragment();
                    return imageFragment;
                case 2:
                    if (onlineMediaFragment == null) onlineMediaFragment = new OnlineMediaFragment();
                    return onlineMediaFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }
}
