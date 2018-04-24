package com.tukualbum.app.common;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tukualbum.app.R;

import butterknife.ButterKnife;

/**
 * Created by gus on 2018/4/23.
 */

public abstract class BaseFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(setContentView(), container, false);
        ButterKnife.bind(this, rootView);
        initView();
        return rootView;
    }

    protected abstract int setContentView();

    protected abstract void initView();
}
