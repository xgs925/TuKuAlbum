package com.tukualbum.app.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.orhanobut.hawk.Hawk;
import com.tukualbum.app.adapters.AlbumsAdapter;
import com.tukualbum.app.data.HandlingAlbums;
import com.tukualbum.app.data.sort.SortingMode;
import com.tukualbum.app.util.Measure;
import com.tukualbum.app.util.Security;
import com.tukualbum.app.util.preferences.Prefs;

import com.tukualbum.app.data.Album;
import com.tukualbum.app.data.AlbumsHelper;
import com.tukualbum.app.data.provider.CPHelper;
import com.tukualbum.app.data.sort.SortingOrder;
import com.tukualbum.app.util.AlertDialogsHelper;
import com.tukualbum.app.util.AnimationUtils;
import com.tukualbum.app.views.GridSpacingItemDecoration;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * Created by dnld on 3/13/17.
 */

public class AlbumsFragment extends BaseFragment {

    public static final String TAG = "AlbumsFragment";

    @BindView(com.tukualbum.app.R.id.albums) RecyclerView rv;
    @BindView(com.tukualbum.app.R.id.swipe_refresh) SwipeRefreshLayout refresh;

    private AlbumsAdapter adapter;
    private GridSpacingItemDecoration spacingDecoration;
    private AlbumClickListener listener;

    private boolean hidden = false;
    ArrayList<String> excuded = new ArrayList<>();

    public interface AlbumClickListener {
        void onAlbumClick(Album album);
    }

    public void setListener(AlbumClickListener clickListener) {
        this.listener = clickListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        excuded = db().getExcludedFolders(getContext());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!clearSelected())
            updateToolbar();
        setUpColumns();
    }

    public void displayAlbums(boolean hidden) {
        this.hidden = hidden;
        displayAlbums();
    }

    private void displayAlbums() {
        adapter.clear();
        SQLiteDatabase db = HandlingAlbums.getInstance(getContext().getApplicationContext()).getReadableDatabase();
        CPHelper.getAlbums(getContext(), hidden, excuded, sortingMode(), sortingOrder())
                .subscribeOn(Schedulers.io())
                .map(album -> album.withSettings(HandlingAlbums.getSettings(db, album.getPath())))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        album -> adapter.add(album),
                        throwable -> {
                            refresh.setRefreshing(false);
                            throwable.printStackTrace();
                        },
                        () -> {
                            db.close();
                            if (getNothingToShowListener() != null)
                                getNothingToShowListener().changedNothingToShow(getCount() == 0);
                            refresh.setRefreshing(false);

                            Hawk.put(hidden ? "h" : "albums", adapter.getAlbumsPaths());
                        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        displayAlbums();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpColumns();
    }

    public void setUpColumns() {
        int columnsCount = columnsCount();

        if (columnsCount != ((GridLayoutManager) rv.getLayoutManager()).getSpanCount()) {
            rv.removeItemDecoration(spacingDecoration);
            spacingDecoration = new GridSpacingItemDecoration(columnsCount, Measure.pxToDp(3, getContext()), true);
            rv.addItemDecoration(spacingDecoration);
            rv.setLayoutManager(new GridLayoutManager(getContext(), columnsCount));
        }
    }

    public int columnsCount() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? Prefs.getFolderColumnsPortrait()
                : Prefs.getFolderColumnsLandscape();
    }

    private void updateToolbar() {
        if (getEditModeListener() != null) {
            if (editMode())
                getEditModeListener().changedEditMode(true, adapter.getSelectedCount(), adapter.getItemCount(), v -> adapter.clearSelected(), null);
            else getEditModeListener().changedEditMode(false, 0, 0, null, null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(com.tukualbum.app.R.layout.fragment_albums, container, false);
        ButterKnife.bind(this, v);

        int spanCount = columnsCount();
        spacingDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getContext()), true);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(spacingDecoration);
        rv.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        if(Prefs.animationsEnabled()) {
            rv.setItemAnimator(
                    AnimationUtils.getItemAnimator(
                            new LandingAnimator(new OvershootInterpolator(1f))
                    ));
        }

        adapter = new AlbumsAdapter(getContext(), this);

        refresh.setOnRefreshListener(this::displayAlbums);
        rv.setAdapter(adapter);
        return v;
    }

    public SortingMode sortingMode() {
        return adapter.sortingMode();
    }

    public SortingOrder sortingOrder() {
        return adapter.sortingOrder();
    }

    private HandlingAlbums db() {
        return HandlingAlbums.getInstance(getContext().getApplicationContext());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(com.tukualbum.app.R.menu.grid_albums, menu);

        menu.findItem(com.tukualbum.app.R.id.select_all).setIcon(ThemeHelper.getToolbarIcon(getContext(), GoogleMaterial.Icon.gmd_select_all));
        menu.findItem(com.tukualbum.app.R.id.delete).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_delete)));
        menu.findItem(com.tukualbum.app.R.id.sort_action).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_sort)));
        menu.findItem(com.tukualbum.app.R.id.search_action).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_search)));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean editMode = editMode();
        boolean oneSelected = getSelectedCount() == 1;

        menu.setGroupVisible(com.tukualbum.app.R.id.general_album_items, !editMode);
        menu.setGroupVisible(com.tukualbum.app.R.id.edit_mode_items, editMode);
        menu.setGroupVisible(com.tukualbum.app.R.id.one_selected_items, oneSelected);

        menu.findItem(com.tukualbum.app.R.id.select_all).setTitle(
                getSelectedCount() == getCount()
                        ? com.tukualbum.app.R.string.clear_selected
                        : com.tukualbum.app.R.string.select_all);

        if (editMode) {
            menu.findItem(com.tukualbum.app.R.id.hide).setTitle(hidden ? com.tukualbum.app.R.string.unhide : com.tukualbum.app.R.string.hide);
        } else {
            menu.findItem(com.tukualbum.app.R.id.ascending_sort_order).setChecked(sortingOrder() == SortingOrder.ASCENDING);
            switch (sortingMode()) {
                case NAME:  menu.findItem(com.tukualbum.app.R.id.name_sort_mode).setChecked(true); break;
                case SIZE:  menu.findItem(com.tukualbum.app.R.id.size_sort_mode).setChecked(true); break;
                case DATE: default:
                    menu.findItem(com.tukualbum.app.R.id.date_taken_sort_mode).setChecked(true); break;
                case NUMERIC:  menu.findItem(com.tukualbum.app.R.id.numeric_sort_mode).setChecked(true); break;
            }
        }

        if (oneSelected) {
            Album selectedAlbum = adapter.getFirstSelectedAlbum();
            menu.findItem(com.tukualbum.app.R.id.pin_album).setTitle(selectedAlbum.isPinned() ? getString(com.tukualbum.app.R.string.un_pin) : getString(com.tukualbum.app.R.string.pin));
            menu.findItem(com.tukualbum.app.R.id.clear_album_cover).setVisible(selectedAlbum.hasCover());
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Album selectedAlbum = adapter.getFirstSelectedAlbum();
        switch (item.getItemId()) {

            case com.tukualbum.app.R.id.select_all:
                if (adapter.getSelectedCount() == adapter.getItemCount())
                    adapter.clearSelected();
                else adapter.selectAll();
                return true;

            case com.tukualbum.app.R.id.pin_album:
                if (selectedAlbum != null) {
                    boolean b = selectedAlbum.togglePinAlbum();
                    db().setPined(selectedAlbum.getPath(), b);
                    adapter.clearSelected();
                    adapter.sort();
                }
                return true;

            case com.tukualbum.app.R.id.clear_album_cover:
                if (selectedAlbum != null) {
                    selectedAlbum.removeCoverAlbum();
                    db().setCover(selectedAlbum.getPath(), null);
                    adapter.clearSelected();
                    adapter.notifyItemChanaged(selectedAlbum);
                    // TODO: 4/5/17 updateui
                    return true;
                }

                return false;

            case com.tukualbum.app.R.id.hide:
                final AlertDialog hideDialog = AlertDialogsHelper.getTextDialog(((ThemedActivity) getActivity()),
                        hidden ? com.tukualbum.app.R.string.unhide : com.tukualbum.app.R.string.hide,
                        hidden ? com.tukualbum.app.R.string.unhide_album_message : com.tukualbum.app.R.string.hide_album_message);

                hideDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(hidden ? com.tukualbum.app.R.string.unhide : com.tukualbum.app.R.string.hide).toUpperCase(), (dialog, id) -> {
                    ArrayList<String> hiddenPaths = AlbumsHelper.getLastHiddenPaths();

                    for (Album album : adapter.getSelectedAlbums()) {
                        if (hidden) { // unhide
                            AlbumsHelper.unHideAlbum(album.getPath(), getContext());
                            hiddenPaths.remove(album.getPath());
                        } else { // hide
                            AlbumsHelper.hideAlbum(album.getPath(), getContext());
                            hiddenPaths.add(album.getPath());
                        }
                    }
                    AlbumsHelper.saveLastHiddenPaths(hiddenPaths);
                    adapter.removeSelectedAlbums();
                    updateToolbar();
                });

                if (!hidden) {
                    hideDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(com.tukualbum.app.R.string.exclude).toUpperCase(), (dialog, which) -> {
                        for (Album album : adapter.getSelectedAlbums()) {
                            db().excludeAlbum(album.getPath());
                            excuded.add(album.getPath());
                        }
                        adapter.removeSelectedAlbums();
                    });
                }
                hideDialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(com.tukualbum.app.R.string.cancel).toUpperCase(), (dialogInterface, i) -> hideDialog.dismiss());
                hideDialog.show();
                return true;

            case com.tukualbum.app.R.id.shortcut:
                AlbumsHelper.createShortcuts(getContext(), adapter.getSelectedAlbums());
                adapter.clearSelected();
                return true;

            case com.tukualbum.app.R.id.name_sort_mode:
                adapter.changeSortingMode(SortingMode.NAME);
                AlbumsHelper.setSortingMode(SortingMode.NAME);
                item.setChecked(true);
                return true;

            case com.tukualbum.app.R.id.date_taken_sort_mode:
                adapter.changeSortingMode(SortingMode.DATE);
                AlbumsHelper.setSortingMode(SortingMode.DATE);
                item.setChecked(true);
                return true;

            case com.tukualbum.app.R.id.size_sort_mode:
                adapter.changeSortingMode(SortingMode.SIZE);
                AlbumsHelper.setSortingMode(SortingMode.SIZE);
                item.setChecked(true);
                return true;

            case com.tukualbum.app.R.id.numeric_sort_mode:
                adapter.changeSortingMode(SortingMode.NUMERIC);
                AlbumsHelper.setSortingMode(SortingMode.NUMERIC);
                item.setChecked(true);
                return true;

            case com.tukualbum.app.R.id.ascending_sort_order:
                item.setChecked(!item.isChecked());
                SortingOrder sortingOrder = SortingOrder.fromValue(item.isChecked());
                adapter.changeSortingOrder(sortingOrder);
                AlbumsHelper.setSortingOrder(sortingOrder);
                return true;

            case com.tukualbum.app.R.id.exclude:
                final AlertDialog.Builder excludeDialogBuilder = new AlertDialog.Builder(getActivity(), getDialogStyle());

                final View excludeDialogLayout = LayoutInflater.from(getContext()).inflate(com.tukualbum.app.R.layout.dialog_exclude, null);
                TextView textViewExcludeTitle = excludeDialogLayout.findViewById(com.tukualbum.app.R.id.text_dialog_title);
                TextView textViewExcludeMessage = excludeDialogLayout.findViewById(com.tukualbum.app.R.id.text_dialog_message);
                final Spinner spinnerParents = excludeDialogLayout.findViewById(com.tukualbum.app.R.id.parents_folder);

                spinnerParents.getBackground().setColorFilter(getIconColor(), PorterDuff.Mode.SRC_ATOP);

                ((CardView) excludeDialogLayout.findViewById(com.tukualbum.app.R.id.message_card)).setCardBackgroundColor(getCardBackgroundColor());
                textViewExcludeTitle.setBackgroundColor(getPrimaryColor());
                textViewExcludeTitle.setText(getString(com.tukualbum.app.R.string.exclude));

                if(adapter.getSelectedCount() > 1) {
                    textViewExcludeMessage.setText(com.tukualbum.app.R.string.exclude_albums_message);
                    spinnerParents.setVisibility(View.GONE);
                } else {
                    textViewExcludeMessage.setText(com.tukualbum.app.R.string.exclude_album_message);
                    spinnerParents.setAdapter(getThemeHelper().getSpinnerAdapter(adapter.getFirstSelectedAlbum().getParentsFolders()));
                }

                textViewExcludeMessage.setTextColor(getTextColor());
                excludeDialogBuilder.setView(excludeDialogLayout);

                excludeDialogBuilder.setPositiveButton(this.getString(com.tukualbum.app.R.string.exclude).toUpperCase(), (dialog, id) -> {

                    if (adapter.getSelectedCount() > 1) {
                        for (Album album : adapter.getSelectedAlbums()) {
                            db().excludeAlbum(album.getPath());
                            excuded.add(album.getPath());
                        }
                        adapter.removeSelectedAlbums();

                    } else {
                        String path = spinnerParents.getSelectedItem().toString();
                        db().excludeAlbum(path);
                        excuded.add(path);
                        adapter.removeAlbumsThatStartsWith(path);
                        adapter.forceSelectedCount(0);
                    }
                    updateToolbar();
                });
                excludeDialogBuilder.setNegativeButton(this.getString(com.tukualbum.app.R.string.cancel).toUpperCase(), null);
                excludeDialogBuilder.show();
                return true;

            case com.tukualbum.app.R.id.delete:
               /* class DeleteAlbums extends AsyncTask<String, Integer, Boolean> {

                    //private AlertDialog dialog;
                    List<Album> selectedAlbums;
                    DeleteAlbumsDialog newFragment;


                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        newFragment = new DeleteAlbumsDialog();
                        Bundle b = new Bundle();
                        b.putParcelableArrayList("albums", ((ArrayList<Album>) adapter.getSelectedAlbums()));

                        newFragment.setArguments(b);
                        newFragment.show(getFragmentManager(), "dialog");
                        //newFragment.setTitle("asd");

                        //dialog = AlertDialogsHelper.getProgressDialog(((ThemedActivity) getActivity()), getString(R.string.delete), getString(R.string.deleting_images));
                        //dialog.show();


                    }

                    @Override
                    protected Boolean doInBackground(String... arg0) {

                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        *//*if (result) {
                            if (albumsMode) {
                                albumsAdapter.clearSelected();
                                //albumsAdapter.notifyDataSetChanged();
                            } else {
                                if (getAlbum().getMedia().size() == 0) {
                                    getAlbums().removeCurrentAlbum();
                                    albumsAdapter.notifyDataSetChanged();
                                    displayAlbums();
                                } else
                                    oldMediaAdapter.swapDataSet(getAlbum().getMedia());
                            }
                        } else requestSdCardPermissions();

                        supportInvalidateOptionsMenu();
                        checkNothing();
                        dialog.dismiss();*//*
                    }
                }*/


                final AlertDialog alertDialog = AlertDialogsHelper.getTextDialog(((ThemedActivity) getActivity()), com.tukualbum.app.R.string.delete, com.tukualbum.app.R.string.delete_album_message);

                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(com.tukualbum.app.R.string.cancel).toUpperCase(), (dialogInterface, i) -> alertDialog.dismiss());

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(com.tukualbum.app.R.string.delete).toUpperCase(), (dialog1, id) -> {
                    if (Security.isPasswordOnDelete()) {

                        Security.authenticateUser(((ThemedActivity) getActivity()), new Security.AuthCallBack() {
                            @Override
                            public void onAuthenticated() {
                                /*new DeleteAlbums().execute();*/
                            }

                            @Override
                            public void onError() {
                                Toast.makeText(getContext(), com.tukualbum.app.R.string.wrong_password, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }/* else new DeleteAlbums().execute();*/
                });
                alertDialog.show();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public int getCount() {
        return adapter.getItemCount();
    }

    public int getSelectedCount() {
        return adapter.getSelectedCount();
    }

    @Override
    public boolean editMode() {
        return adapter.selecting();
    }

    @Override
    public boolean clearSelected() {
        return adapter.clearSelected();
    }

    @Override
    public void onItemSelected(int position) {
        if (listener != null) listener.onAlbumClick(adapter.get(position));
    }

    @Override
    public void onSelectMode(boolean selectMode) {
        refresh.setEnabled(!selectMode);
        updateToolbar();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onSelectionCountChanged(int selectionCount, int totalCount) {
        getEditModeListener().onItemsSelected(selectionCount, totalCount);
    }

    @Override
    public void refreshTheme(ThemeHelper t) {
        rv.setBackgroundColor(t.getBackgroundColor());
        adapter.refreshTheme(t);
        refresh.setColorSchemeColors(t.getAccentColor());
        refresh.setProgressBackgroundColorSchemeColor(t.getBackgroundColor());
    }
}
