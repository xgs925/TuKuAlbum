package com.tukualbum.app.data.parser;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tukualbum.app.data.parser.model.Mm99;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flymegoc
 * @date 2018/2/1
 */

public class Mm99Adapter extends BaseQuickAdapter<Mm99, BaseViewHolder> {
    private Map<String, Integer> heightMap = new HashMap<>();
    private int width;

    public Mm99Adapter(int layoutResId) {
        super(layoutResId);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final Mm99 item) {


    }
}
