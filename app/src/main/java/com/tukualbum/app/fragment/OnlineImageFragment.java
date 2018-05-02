package com.tukualbum.app.fragment;


import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.animation.OvershootInterpolator;

import com.tukualbum.app.R;
import com.tukualbum.app.adapter.ImageListAdapter;
import com.tukualbum.app.common.BaseFragment;
import com.tukualbum.app.data.Media;
import com.tukualbum.app.data.parser.AppApiHelper;
import com.tukualbum.app.data.parser.model.MeiZiTu;
import com.tukualbum.app.data.parser.rxjava.CallBackWrapper;
import com.tukualbum.app.data.parser.rxjava.RxSchedulersHelper;
import com.tukualbum.app.util.AnimationUtils;
import com.tukualbum.app.util.Measure;
import com.tukualbum.app.views.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
public class OnlineImageFragment extends BaseFragment {
    private GridSpacingItemDecoration mItemDecoration;
    @BindView(R.id.rv_img)
    RecyclerView recyclerView;
    private ImageListAdapter mAdapter;
    AppApiHelper dataManager = AppApiHelper.get();
    private int page = 1;
    private Integer totalPage;
    private String mTag;

    @Override
    protected int setContentView() {
        return R.layout.fragment_online_image;
    }

    @Override
    protected void initView() {
        mTag = getArguments().getString("tag");
        initAdapter();
        initRecyclerView();
        initData();
    }

    private void initData() {
        dataManager.listMeiZiTu(mTag, page, false)
                .map(baseResult -> {
                    if (page == 1) {
                        totalPage = baseResult.getTotalPage();
                    }
                    return baseResult.getData();
                })
                .compose(RxSchedulersHelper.ioMainThread())
                .subscribe(new CallBackWrapper<List<MeiZiTu>>() {
                    @Override
                    public void onSuccess(final List<MeiZiTu> meiZiTus) {
                        System.out.println(meiZiTus.size());
                        List<Media> mediaList = new ArrayList<>();
                        for (MeiZiTu m : meiZiTus
                                ) {
                            mediaList.add(new Media(m.getThumbUrl()));
                        }
                        mAdapter.addData(mediaList);
                        page++;
                    }

                    @Override
                    public void onError(String msg, int code) {

                    }

                    @Override
                    public void onComplete() {
                        mAdapter.loadMoreComplete();
                    }
                });

    }

    private void initAdapter() {
        mAdapter = new ImageListAdapter(getContext(), new ArrayList<>());
        mAdapter.setOnLoadMoreListener(() -> {
            if (page > totalPage) {
                mAdapter.loadMoreEnd();
            } else {
                initData();
            }
        },recyclerView);
    }

    private void initRecyclerView() {
        mItemDecoration = new GridSpacingItemDecoration(2, Measure.pxToDp(2, getContext()), true);
        recyclerView.addItemDecoration(mItemDecoration);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setItemAnimator(
                AnimationUtils.getItemAnimator(
                        new LandingAnimator(new OvershootInterpolator(1f))
                ));

        recyclerView.setAdapter(mAdapter);

    }


}
