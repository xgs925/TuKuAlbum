package com.tukualbum.app.data.filter;

import com.tukualbum.app.data.Media;

/**
 * Created by dnld on 4/10/17.
 */

public interface IMediaFilter {
    boolean accept(Media media);
}
