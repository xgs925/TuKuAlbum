package com.tukualbum.app.common;

import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by gus on 2018/4/23.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        initView();
    }

    protected abstract void initView();
}
