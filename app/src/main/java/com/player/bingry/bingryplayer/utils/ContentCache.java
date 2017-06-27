package com.player.bingry.bingryplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.BuildConfig;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.player.bingry.bingryplayer.provider.BingryContent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * Created by bingry on 2017-06-03.
 */

public class ContentCache {

    private static final String TAG = "ContentCache";
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024*5;
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024*1024*10;
    private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT =  Bitmap.CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final int DISK_CACHE_INDEX = 0;

    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = false;
    private  DiskLruCache mDiskLruCache;
    private LruCache<String, ContentPlayable> mMemoryCache;

    private ContentCacheParams mCacheParams;

    private Set<SoftReference<Bitmap>> mReusableContents;

    private ContentCache(ContentCacheParams cacheParams){
        init(cacheParams);
    }

    public static ContentCache getInstance(FragmentManager fragmentManager,
                                           ContentCacheParams cacheParams) {
        // TODO Auto-generated method stub
        final RetainFragment mRetainFragment = findOrCreateRetainFragment(fragmentManager);

        ContentCache contentCache= (ContentCache) mRetainFragment.getObject();

        if(contentCache == null){
            contentCache = new ContentCache(cacheParams);
            mRetainFragment.setObject(contentCache);
        }
        return contentCache;
    }

    private void init(ContentCacheParams cacheParams) {
        // TODO Auto-generated method stub
        mCacheParams = cacheParams;

        if(mCacheParams.memoryCacheEnabled){
            if(BuildConfig.DEBUG){
                Log.d(TAG, "Memory cache created (size = " + mCacheParams.memCacheSize + ")");
            }


            if(Utils.hasHoneycomb()){
                mReusableContents=
                        Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
            }

            mMemoryCache = new LruCache<String, ContentPlayable>(mCacheParams.memCacheSize) {

                @Override
                protected void entryRemoved(boolean evicted, String key,
                                            ContentPlayable oldValue, ContentPlayable newValue) {
                    if(RecyclingContentPlayable.class.isInstance(oldValue)){
                        ((RecyclingContentPlayable) oldValue).setIsCached(false);
                    } else {
                        if (Utils.hasHoneycomb()) {
                            BingryContent bc = oldValue.getBingryContent();
                            if(bc.mType == 0) {
                                mReusableContents.add(new SoftReference<Bitmap>(bc.mBitmap));
                            }
                        }
                    }
                }

                @Override
                protected int sizeOf(String key, ContentPlayable value){
                    final int bitmapSize = getContentSize(value)/1024;
                    return bitmapSize == 0 ? 1: bitmapSize;
                }
            };
        }

        if(cacheParams.initDiskCacheOnCreate){
            initDiskCache();
        }
    }

    public void initDiskCache() {

        synchronized(mDiskCacheLock){
            if(mDiskLruCache == null || mDiskLruCache.isClosed()){
                File diskCacheDir = mCacheParams.diskCacheDir;
                if(mCacheParams.diskCacheEnabled && diskCacheDir !=null){
                    if(!diskCacheDir.exists()){
                        diskCacheDir.mkdirs();
                    }
                    if(getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize){
                        try{
                            mDiskLruCache = DiskLruCache.open(
                                    diskCacheDir, 1, 1, mCacheParams.diskCacheSize);

                            if(BuildConfig.DEBUG){
                                Log.d(TAG, "Disk cache initialized");
                            }
                        } catch (final IOException e) {
                            mCacheParams.diskCacheDir = null;
                            Log.e(TAG, "initDiskCache - " + e);
                        }
                    }
                }
            }
            mDiskCacheStarting =false;
            mDiskCacheLock.notifyAll();
        }
    }

    public void addContentToCache(String data, ContentPlayable value){
        if(data == null || value == null){
            return;
        }
        BingryContent bc = value.getBingryContent();

        if(mMemoryCache != null){
            if(RecyclingContentPlayable.class.isInstance(value)){
                ((RecyclingContentPlayable)value).setIsCached(true);
            }
            mMemoryCache.put(data, value);
        }

        synchronized(mDiskCacheLock){
            if(mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if(snapshot == null){
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if(editor !=null){
                            if(BuildConfig.DEBUG){
                                Log.d(TAG, "addBitmapToCache:" + data );
                            }

                            if(bc.mType ==0) {
                                out = editor.newOutputStream(DISK_CACHE_INDEX);

//                                value.getBitmap().compress(
//                                        mCacheParams.compressFormat, mCacheParams.compressQuality, out);
                                bc.mBitmap.compress(
                                        mCacheParams.compressFormat, mCacheParams.compressQuality, out);
                                editor.commit();
                                out.close();
                            }
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e){
                    Log.e(TAG, "addBitmapCache  " + e);
                } catch (Exception e){
                    Log.e(TAG, "addBitmapToCache-" +e);
                } finally {
                    try{
                        if(out != null){
                            out.close();
                        }
                    } catch(IOException e){}
                }
            }
        }
    }

    public ContentPlayable getContentFromMemCache(String data) {
        ContentPlayable memValue = null;

        if (mMemoryCache != null) {
            memValue = mMemoryCache.get(data);
        }

        if (BuildConfig.DEBUG && memValue != null) {
            Log.d(TAG, "Memory cache hit");
        }

        return memValue;
    }

    public BingryContent getContentFromDiskCache(String dataString) {
        final String key = hashKeyForDisk(dataString);
        BingryContent content = null;

        synchronized(mDiskCacheLock){
            while(mDiskCacheStarting){
                try{
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {

                }
            }
            if(mDiskLruCache != null){
                InputStream inputStream = null;
                try{
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                } catch(final IOException e){
                    Log.e(TAG, "getConentFromDiskCache");
                } finally {
                    try{
                        if(inputStream != null){
                            inputStream.close();
                        }
                    } catch (IOException e){

                    }
                }
            }
        }
        return content;
    }

    protected Bitmap getBitmapFromReusableSet(BingryContent bc, BitmapFactory.Options options) {
        // TODO Auto-generated method stub
        Bitmap bitmap = null;
        if(bc.mType == 0) {
            if (mReusableContents != null && !mReusableContents.isEmpty()) {
                synchronized (mReusableContents) {
                    final Iterator<SoftReference<Bitmap>> iterator = mReusableContents.iterator();

                    Bitmap item;

                    while (iterator.hasNext()) {
                        item = iterator.next().get();

                        if (null != item && item.isMutable()) {
                            if (canUseForInBitmap(item, options)) {
                                bitmap = item;

                                iterator.remove();
                                break;
                            }
                        } else {
                            iterator.remove();
                        }
                    }
                }
            }
        }
        return bitmap;
    }

    public void clearCache(){
        if(mMemoryCache != null){
            mMemoryCache.evictAll();
            if(BuildConfig.DEBUG){
                Log.d(TAG, "Memory Cache cleared");
            }
        }

        synchronized ( mDiskCacheLock){
            mDiskCacheStarting = true;
            if(mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try{
                    mDiskLruCache.delete();
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "DiskCache cleared");
                    }
                } catch (IOException e){
                    Log.e(TAG, "clearCache -  "+ e);
                }
                mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    public void flush() {
        synchronized (mDiskCacheLock){

            if(mDiskLruCache != null){
                try{
                    mDiskLruCache.flush();
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "Disk Cache flushed");
                    }
                } catch (IOException e){
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }
    public void close() {
        // TODO Auto-generated method stub
        synchronized(mDiskCacheLock){
            if(mDiskLruCache!=null){
                try{
                    if(!mDiskLruCache.isClosed()){
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                        if(BuildConfig.DEBUG){
                            Log.d(TAG, "Disk cache closed");
                        }
                    }
                } catch (IOException e){
                    Log.e(TAG, "close- " + e);
                }
            }
        }
    }

    public static class ContentCacheParams {
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public File diskCacheDir;
        public Bitmap.CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

        public ContentCacheParams(Context context, String diskCacheDirectoryName){
            diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
        }

        public void setMemCacheSizePercent(float percent){
            if (percent < 0.01f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                        + "between 0.01 and 0.8 (inclusive)");
            }
            memCacheSize = Math.round(percent*Runtime.getRuntime().maxMemory()/1024);
        }
    }

    private static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {
        //BEGIN_INCLUDE(can_use_for_inbitmap)
        if (!Utils.hasKitKat()) {
            // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
            return candidate.getWidth() == targetOptions.outWidth
                    && candidate.getHeight() == targetOptions.outHeight
                    && targetOptions.inSampleSize == 1;
        }

        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        int byteCount = width*height*getBytesPerPixel(candidate.getConfig());
        return byteCount <= candidate.getAllocationByteCount();
    }

    private static int getBytesPerPixel(Bitmap.Config config) {
        // TODO Auto-generated method stub
        if(config == Bitmap.Config.ARGB_8888){
            return 4;
        } else if(config == Bitmap.Config.RGB_565){
            return 2;
        } else if(config == Bitmap.Config.ARGB_4444){
            return 2;
        } else if(config == Bitmap.Config.ALPHA_8){
            return 1;
        }
        return 1;
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        // TODO Auto-generated method stub
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }


    private static String hashKeyForDisk(String key) {

        String cacheKey;
        try{
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e){
            cacheKey = String.valueOf(key.hashCode());
        }

        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i< bytes.length;i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);

            if(hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static int getContentSize(ContentPlayable value) {
        // TODO Auto-generated method stub
        //Bitmap bitmap = value.getContent();
        BingryContent bc = value.getBingryContent();
        Bitmap bitmap = bc.mBitmap;

        if(Utils.hasKitKat()){
            return bitmap.getAllocationByteCount();
        }

        if(Utils.hasHoneycombMR1()){
            return bitmap.getByteCount();
        }

        return bitmap.getRowBytes()*bitmap.getHeight();

    }
    public static boolean isExternalStorageRemovable() {
        // TODO Auto-generated method stub
        if(Utils.hasGingerBread()){
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }
    public static File getExternalCacheDir(Context context) {
        // TODO Auto-generated method stub
        if(Utils.hasFroyo()){
            return context.getExternalCacheDir();
        }
        final String cacheDir = "/Android/data" + context.getPackageName() + "cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }


    public static long getUsableSpace(File diskCacheDir) {
        // TODO Auto-generated method stub
        if(Utils.hasGingerBread()){
            return diskCacheDir.getUsableSpace();
        }
        final StatFs stats = new StatFs(diskCacheDir.getPath());

        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }
    private static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        RetainFragment mRetainFragment = (RetainFragment)fm.findFragmentByTag(TAG);
        if(mRetainFragment == null){
            mRetainFragment = new RetainFragment();
            fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
        }

        return mRetainFragment;
    }

    public static class RetainFragment extends Fragment {
        private Object mObject;

        public RetainFragment(){}

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            setRetainInstance(true);
        }

        public void setObject(Object object){mObject = object;}

        public Object getObject(){ return mObject;}

    }




}
