package com.player.bingry.bingryplayer.utils;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import com.player.bingry.bingryplayer.BuildConfig;
import com.player.bingry.bingryplayer.provider.BingryContent;

/**
 * Created by bingry on 2017-06-06.
 */

public class RecyclingContentPlayable extends ContentPlayable{

    static final String TAG = "RecyclingPlayable";

    private int mCacheRefCount=0;
    private int mDisplayRefCount = 0;
    private boolean mHasBeenDisplayed;

    public RecyclingContentPlayable(Resources res, BingryContent bingrycontent){
        super(res, bingrycontent);
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
//        if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed
//                && hasValidBitmap()) {
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "No longer being used or cached so recycling. "
//                        + toString());
//            }
//
//            getBitmap().recycle();
//        }
    }


}
