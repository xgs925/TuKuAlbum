package com.tukualbum.app.common;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import org.horaapps.liz.ThemedActivity;

import butterknife.ButterKnife;

/**
 * Created by gus on 2018/4/23.
 */

public abstract class BaseActivity extends ThemedActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        initView();
    }

    protected Context getContext(){
        return this;
    }
    protected abstract void initView();
}
