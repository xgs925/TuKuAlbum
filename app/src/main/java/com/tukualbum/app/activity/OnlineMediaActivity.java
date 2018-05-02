package com.tukualbum.app.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.tukualbum.app.R;
import com.tukualbum.app.adapters.MediaAdapter;
import com.tukualbum.app.common.BaseActivity;
import com.tukualbum.app.data.Album;
import com.tukualbum.app.data.filter.MediaFilter;
import com.tukualbum.app.data.parser.AppApiHelper;
import com.tukualbum.app.data.parser.model.MeiZiTu;
import com.tukualbum.app.data.parser.model.Mm99;
import com.tukualbum.app.data.parser.rxjava.CallBackWrapper;
import com.tukualbum.app.data.parser.rxjava.RxSchedulersHelper;
import com.tukualbum.app.data.provider.CPHelper;
import com.tukualbum.app.fragment.AllMediaFragment;
import com.tukualbum.app.fragment.ImageFragment;
import com.tukualbum.app.fragment.OnlineImageFragment;
import com.tukualbum.app.fragment.OnlineMediaFragment;
import com.tukualbum.app.items.ActionsListener;
import com.tukualbum.app.util.AnimationUtils;
import com.tukualbum.app.util.Measure;
import com.tukualbum.app.views.GridSpacingItemDecoration;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class OnlineMediaActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.vp_container)
    ViewPager mViewPager;
    @BindView(R.id.tb_title)
    TabLayout mTabLayout;


    AppApiHelper dataManager = AppApiHelper.get();

    private String[] mTitles = new String[]{"index", "hot", "best","japan","taiwan","xinggan","mm"};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_media);
    }

    @Override
    protected void initView() {
        mToolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        for (int i = 0; i < mTitles.length; i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(mTitles[i]));
        }
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

    }
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            OnlineImageFragment fragment=new OnlineImageFragment();
            Bundle args=new Bundle();
            args.putString("tag",mTitles[position]);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }
}
