package com.tukualbum.app.activity;

import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.animation.OvershootInterpolator;

import com.tukualbum.app.R;
import com.tukualbum.app.activities.SingleMediaActivity;
import com.tukualbum.app.adapters.MediaAdapter;
import com.tukualbum.app.common.BaseActivity;
import com.tukualbum.app.data.Album;
import com.tukualbum.app.data.filter.MediaFilter;
import com.tukualbum.app.data.parser.ApiHelper;
import com.tukualbum.app.data.parser.AppApiHelper;
import com.tukualbum.app.data.parser.model.BaseResult;
import com.tukualbum.app.data.parser.model.MeiZiTu;
import com.tukualbum.app.data.provider.CPHelper;
import com.tukualbum.app.items.ActionsListener;
import com.tukualbum.app.util.AnimationUtils;
import com.tukualbum.app.util.Measure;
import com.tukualbum.app.views.GridSpacingItemDecoration;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class OnlineMediaActivity extends BaseActivity {
    @BindView(R.id.rv_media) RecyclerView mRecyclerView;
    private MediaAdapter mAdapter;
    private Album mAlbum;
    private GridSpacingItemDecoration mItemDecoration;
    private ApiHelper dataManager=new AppApiHelper();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_media);
    }

    @Override
    protected void initView() {
//        initAdapter();
        initRecyclerView();
    }

    private void initAdapter() {
        mAdapter = new MediaAdapter(getContext(), mAlbum.settings.getSortingMode(), mAlbum.settings.getSortingOrder(), new ActionsListener() {
            @Override
            public void onItemSelected(int position) {

            }

            @Override
            public void onSelectMode(boolean selectMode) {

            }

            @Override
            public void onSelectionCountChanged(int selectionCount, int totalCount) {

            }
        });
    }
    private void initRecyclerView() {
        mItemDecoration = new GridSpacingItemDecoration(4, Measure.pxToDp(2, getContext()), true);
        mRecyclerView.addItemDecoration(mItemDecoration);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mRecyclerView.setItemAnimator(
                AnimationUtils.getItemAnimator(
                        new LandingAnimator(new OvershootInterpolator(1f))
                ));


        dataManager.listMeiZiTu(tag, page, pullToRefresh)
                .map(new Function<BaseResult<List<MeiZiTu>>, List<MeiZiTu>>() {
                    @Override
                    public List<MeiZiTu> apply(BaseResult<List<MeiZiTu>> baseResult) throws Exception {
                        if (page == 1) {
                            totalPage = baseResult.getTotalPage();
                        }
                        return baseResult.getData();
                    }
                })
                .compose(RxSchedulersHelper.<List<MeiZiTu>>ioMainThread())
                .compose(provider.<List<MeiZiTu>>bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribe(new CallBackWrapper<List<MeiZiTu>>() {

                    @Override
                    public void onBegin(Disposable d) {
                        ifViewAttached(new ViewAction<MeiZiTuView>() {
                            @Override
                            public void run(@NonNull MeiZiTuView view) {
                                view.showLoading(pullToRefresh);
                            }
                        });
                    }

                    @Override
                    public void onSuccess(final List<MeiZiTu> meiZiTus) {
                        ifViewAttached(new ViewAction<MeiZiTuView>() {
                            @Override
                            public void run(@NonNull MeiZiTuView view) {
                                if (page == 1) {
                                    view.setData(meiZiTus);
                                    view.showContent();
                                } else {
                                    view.setMoreData(meiZiTus);
                                }
                                //已经最后一页了
                                if (page >= totalPage) {
                                    view.noMoreData();
                                } else {
                                    page++;
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final String msg, int code) {
                        ifViewAttached(new ViewAction<MeiZiTuView>() {
                            @Override
                            public void run(@NonNull MeiZiTuView view) {
                                view.showError(msg);
                            }
                        });
                    }
                });



        mRecyclerView.setAdapter(mAdapter);
        display();
    }

    private void display() {
        mAdapter.setupFor(mAlbum);
        CPHelper.getMedia(getContext(), mAlbum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(media -> MediaFilter.getFilter(mAlbum.filterMode()).accept(media))
                .subscribe(media -> mAdapter.add(media),
                        throwable -> {
                            Log.wtf("asd", throwable);
                        },
                        () -> mAlbum.setCount(mAdapter.getItemCount()));
    }
}
