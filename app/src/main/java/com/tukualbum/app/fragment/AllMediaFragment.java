package com.tukualbum.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.animation.OvershootInterpolator;

import com.tukualbum.app.R;
import com.tukualbum.app.activities.SingleMediaActivity;
import com.tukualbum.app.adapters.MediaAdapter;
import com.tukualbum.app.common.BaseFragment;
import com.tukualbum.app.data.Album;
import com.tukualbum.app.data.filter.MediaFilter;
import com.tukualbum.app.data.provider.CPHelper;
import com.tukualbum.app.fragments.RvMediaFragment;
import com.tukualbum.app.items.ActionsListener;
import com.tukualbum.app.util.AnimationUtils;
import com.tukualbum.app.util.Measure;
import com.tukualbum.app.views.GridSpacingItemDecoration;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * Created by gus on 2018/4/19.
 */

public class AllMediaFragment extends BaseFragment {
    private static final String BUNDLE_ALBUM = "album";
    @BindView(R.id.srl_refresh)
    SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.rv_img)
    RecyclerView mRecyclerView;
    private MediaAdapter mAdapter;
    private GridSpacingItemDecoration mItemDecoration;
    private Album mAlbum;

    public static AllMediaFragment make(Album album) {
        AllMediaFragment fragment = new AllMediaFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_ALBUM, album);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            if (getArguments() != null)
                mAlbum = getArguments().getParcelable(BUNDLE_ALBUM);
        } else {
            mAlbum = savedInstanceState.getParcelable(BUNDLE_ALBUM);
        }
        if (mAlbum == null) mAlbum = Album.getAllMediaAlbum();
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initView() {
        initAdapter();
        initRefreshLayout();
        initRecyclerView();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(() -> {
            mAlbum = Album.getAllMediaAlbum();
            display();

        });
    }

    private void initAdapter() {
        mAdapter = new MediaAdapter(getContext(), mAlbum.settings.getSortingMode(), mAlbum.settings.getSortingOrder(), new ActionsListener() {
            @Override
            public void onItemSelected(int position) {
                Intent intent = new Intent(getContext(), SingleMediaActivity.class);
                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_ALBUM, mAlbum);
                try {
                    intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM);
                    intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, mAdapter.getMedia());
                    intent.putExtra(SingleMediaActivity.EXTRA_ARGS_POSITION, position);
                    startActivity(intent);
                } catch (Exception e) { // Putting too much data into the Bundle
                    // TODO: Find a better way to pass data between the activities - possibly a key to
                    // access a HashMap or a unique value of a singleton Data Repository of some sort.
                    intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM_LAZY);
                    intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, mAdapter.getMedia().get(position));
                    startActivity(intent);
                }
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
                        () -> {
                            mAlbum.setCount(mAdapter.getItemCount());
                            mRefreshLayout.setRefreshing(false);
                        });
    }
}
