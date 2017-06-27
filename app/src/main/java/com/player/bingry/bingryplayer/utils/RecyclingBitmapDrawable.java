package com.player.bingry.bingryplayer.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.player.bingry.bingryplayer.BuildConfig;

/**
 * Created by bingry on 2017-06-07.
 */

public class RecyclingBitmapDrawable extends BitmapDrawable {

    static final String TAG = "CountingBitmapDrawable";

    private int mCacheRefCount=0;
    private int mDisplayRefCount = 0;
    private boolean mHasBeenDisplayed;

    public RecyclingBitmapDrawable(Resources res, Bitmap bitmap){
        super(res, bitmap);
    }
    public void setIsDisplayed(boolean isDisplayed) {
        //BEGIN_INCLUDE(set_is_displayed)
        synchronized (this) {
            if (isDisplayed) {
                mDisplayRefCount++;
                mHasBeenDisplayed = true;
            } else {
                mDisplayRefCount--;
            }
        }

        // Check to see if recycle() can be called
        checkState();
        //END_INCLUDE(set_is_displayed)
    }


    public void setIsCached(boolean isCached) {
        // TODO Auto-generated method stub
        synchronized(this){
            if(isCached){
                mCacheRefCount++;
            } else {
                mCacheRefCount--;
            }
        }

        checkState();

    }


    private synchronized void checkState() {
        // TODO Auto-generated method stub
        if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed
                && hasValidBitmap()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No longer being used or cached so recycling. "
                        + toString());
            }

            getBitmap().recycle();
        }
    }

    private synchronized boolean hasValidBitmap() {
        // TODO Auto-generated method stub
        Bitmap bitmap = getBitmap();
        return bitmap != null && !bitmap.isRecycled();
    }

}

