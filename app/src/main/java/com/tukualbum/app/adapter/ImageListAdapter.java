package com.tukualbum.app.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tukualbum.app.R;
import com.tukualbum.app.data.Media;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by gus on 2018/4/19.
 */

public class ImageListAdapter extends BaseQuickAdapter<Media,ImageListAdapter.ViewHolder> {
    private Context mContext;
    public ImageListAdapter(Context context,@Nullable List<Media> data) {
        super(R.layout.item_image,data);
        mContext=context;
    }


    @Override
    protected void convert(ViewHolder helper, Media item) {
        helper.bind(item);
    }



    class ViewHolder extends BaseViewHolder {

        @BindView(R.id.iv_img)
        ImageView imageView;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Media media){
            Glide.with(mContext).load(buildGlideUrl(media.getPath())).into(imageView);
        }
        private GlideUrl buildGlideUrl(String url) {
            if (TextUtils.isEmpty(url)) {
                return null;
            } else {
                return new GlideUrl(url, new LazyHeaders.Builder()
                        .addHeader("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8")
                        .addHeader("Host", "i.meizitu.net")
                        .addHeader("Referer", "http://www.mzitu.com/")
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                        .build());
            }
        }
    }
}
