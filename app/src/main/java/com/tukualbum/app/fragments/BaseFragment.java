package com.tukualbum.app.fragments;


import com.tukualbum.app.items.ActionsListener;

import org.horaapps.liz.Themed;
import org.horaapps.liz.ThemedFragment;

/**
 * Created by dnld on 4/3/17.
 */

public abstract class BaseFragment extends ThemedFragment implements IFragment, Themed, ActionsListener {

    private EditModeListener editModeListener;
    private NothingToShowListener nothingToShowListener;

    public boolean onBackPressed(){
        if (editMode()){
            clearSelected();
            return true;
        }
        return false;
    }

    public EditModeListener getEditModeListener() {
        return editModeListener;
    }

    public void setEditModeListener(EditModeListener editModeListener) {
        this.editModeListener = editModeListener;
    }

    public NothingToShowListener getNothingToShowListener() {
        return nothingToShowListener;
    }

    public void setNothingToShowListener(NothingToShowListener nothingToShowListener) {
        this.nothingToShowListener = nothingToShowListener;
    }
}
