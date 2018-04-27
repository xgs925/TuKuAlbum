package com.tukualbum.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.animation.OvershootInterpolator;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.tukualbum.app.R;
import com.tukualbum.app.activities.SingleMediaActivity;
import com.tukualbum.app.adapters.MediaAdapter;
import com.tukualbum.app.common.BaseActivity;
import com.tukualbum.app.data.Album;
import com.tukualbum.app.data.filter.MediaFilter;
import com.tukualbum.app.data.provider.CPHelper;
import com.tukualbum.app.items.ActionsListener;
import com.tukualbum.app.util.AnimationUtils;
import com.tukualbum.app.util.Measure;
import com.tukualbum.app.views.GridSpacingItemDecoration;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class AlbumActivity extends BaseActivity {
    public static final String BUNDLE_ALBUM = "album";
    @BindView(R.id.rv_img)
    RecyclerView mRecyclerView;
    private MediaAdapter mAdapter;
    private GridSpacingItemDecoration mItemDecoration;
    private Album mAlbum;
    @BindView(com.tukualbum.app.R.id.toolbar)
    Toolbar toolbar;
    private static final String TAG="AlbumActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
    }

    @Override
    protected void initView() {
        setSupportActionBar(toolbar);
        toolbar.bringToFront();
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        mAlbum=getIntent().getParcelableExtra(BUNDLE_ALBUM);
        initAdapter();
        initRecyclerView();
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
                        () -> mAlbum.setCount(mAdapter.getItemCount()));
    }
}
