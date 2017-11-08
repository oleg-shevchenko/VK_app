package com.olegsh.vkapp.utils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.olegsh.vkapp.R;

/**
 * Created by Oleg on 07.11.2017.
 */

public class Utils {
    public static DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.image_no_preview) // resource or drawable
            .showImageForEmptyUri(R.drawable.image_no_preview) // resource or drawable
            .showImageOnFail(R.drawable.image_no_preview) // resource or drawable
            .cacheInMemory(true)
            .cacheOnDisk(true) // default
            .build();
}
