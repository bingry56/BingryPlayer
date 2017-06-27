package com.player.bingry.bingryplayer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

import com.player.bingry.bingryplayer.BuildConfig;
import com.player.bingry.bingryplayer.provider.BingryContent;

import java.lang.ref.WeakReference;

/**
 * Created by bingry on 2017-06-03.
 */

public abstract class ContentWorker {

    private static final String TAG = "ContentWorker";
    private static final int FADE_IN_TIME = 200;

    private ContentCache mContentCache;
    private final Object mPauseWorkerLock = new Object();

    protected Resources mResources;
    protected boolean mPauseWork = false;
    protected boolean mExitTaskEarly = false;
    protected ContentWorker(Context context) {

        mResources = context.getResources();
    }

    public void loadContent(Object data, ContentView contentView) {
        if (data == null) {
            return;
        }

        ContentPlayable value = null;

        if(mContentCache != null){
            value = mContentCache.getContentFromMemCache(String.valueOf(data));
        } else if(cancelPotentialWork(data, contentView)) {

        }
    }

    public static boolean cancelPotentialWork(Object data, ContentView contentView) {
        final ContentWorkerTask contentWorkerTask = getContentWorkerTask(contentView);

        return true;
    }

    private static ContentWorkerTask getContentWorkerTask(ContentView contentView) {

        return null;
    }

    private class ContentWorkerTask extends AsyncTask<Void, Void, ContentPlayable> {
        private Object mData;
        private final WeakReference<ContentView> contentViewReference;



        public ContentWorkerTask(Object data, ContentView contentView){
            mData = data;
            contentViewReference = new WeakReference<ContentView>(contentView);
        }

        @Override
        protected ContentPlayable doInBackground(Void... params) {

            if(BuildConfig.DEBUG){
                Log.d(TAG, "doingBackground - starting ContentWork");
            }

            final String dataString = String.valueOf(mData);
            BingryContent content = null;
            ContentPlayable playable = null;

            synchronized(mPauseWorkerLock){
                while(mPauseWork && !isCancelled()){
                    try{
                        mPauseWorkerLock.wait();
                    } catch(InterruptedException e){}
                }
            }
            if(mContentCache != null && isCancelled() && getAttachedContentView() != null
                    && !mExitTaskEarly){
                content = mContentCache.getContentFromDiskCache(dataString);

            }
            return null;
        }

        private ContentView getAttachedContentView() {
            final ContentView contentView = contentViewReference.get();
            final ContentWorkerTask contentWorkerTask = getContentWorkerTask(contentView);

            if(this == contentWorkerTask){
                return contentView;
            }
            return null;
        }


    }
}
