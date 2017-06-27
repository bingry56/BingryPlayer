package com.player.bingry.bingryplayer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.player.bingry.bingryplayer.BuildConfig;
/**
 * Created by bingry on 2017-06-08.
 */

public class ImageFetcher extends ImageResizer{
    private static final String TAG = "ImageFetcher";
    private static final int HTTP_CACHE_SIZE = 10*1024*1024;
    private static final String HTTP_CACHE_DIR = "http";
    private static final int IO_BUFFER_SIZE = 8*1024;

    private DiskLruCache mHttpDiskCache;
    private File mHttpCacheDir;
    private boolean mHttpDiskCacheStarting = true;
    private final Object mHttpDiskCacheLock = new Object();
    private static final int DISK_CACHE_INDEX = 0;
    private final Context mContext;

    public ImageFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
        mContext = context;
        init(context);
    }

    public ImageFetcher(Context context, int imageSize){
        super(context,imageSize);
        init(context);
        mContext = context;
    }

    private void init(Context context) {
        // TODO Auto-generated method stub
        checkConnection(context);
        mHttpCacheDir = ImageCache.getDiskCacheDir(context, HTTP_CACHE_DIR);
    }
    @Override
    protected void initDiskCacheInternal(){
        super.initDiskCacheInternal();
        initHttpDiskCache();
    }
    private void initHttpDiskCache() {
        // TODO Auto-generated method stub
        if(!mHttpCacheDir.exists()){
            mHttpCacheDir.mkdirs();
        }

        synchronized(mHttpDiskCacheLock){
            if(ImageCache.getUsableSpace(mHttpCacheDir) > HTTP_CACHE_SIZE){
                try{
                    mHttpDiskCache = DiskLruCache.open(mHttpCacheDir,1,1,HTTP_CACHE_SIZE);
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "HTTP cache Initialized");
                    }
                } catch (IOException e){
                    mHttpDiskCache = null;
                }
            }
            mHttpDiskCacheStarting = false;
            mHttpDiskCacheLock.notifyAll();
        }
    }

    @Override
    protected void clearCacheInternal(){
        super.clearCacheInternal();
        synchronized(mHttpDiskCacheLock){
            if(mHttpDiskCache != null && !mHttpDiskCache.isClosed()){
                try{
                    mHttpDiskCache.delete();
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "HTTP cache cleared");
                    }
                } catch ( IOException e){
                    Log.e(TAG, "clearCacheInternal - "+ e);
                }

                mHttpDiskCache = null;
                mHttpDiskCacheStarting = true;
                initHttpDiskCache();
            }
        }
    }

    @Override
    protected void flushCacheInternal(){
        super.flushCacheInternal();
        synchronized(mHttpDiskCacheLock){
            if(mHttpDiskCache != null){
                try{
                    mHttpDiskCache.flush();
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "Http cache flushed");

                    }
                } catch (IOException e) {
                    Log.e(TAG, "flush"+ e);
                }
            }
        }
    }

    @Override
    protected void closeCacheInternal(){
        super.closeCacheInternal();
        synchronized(mHttpDiskCacheLock){
            if(mHttpDiskCache !=null){
                try{
                    if(!mHttpDiskCache.isClosed()){
                        mHttpDiskCache.close();
                        mHttpDiskCache = null;
                        if(BuildConfig.DEBUG){
                            Log.d(TAG, "Http cache closed");
                        }
                    }
                } catch (IOException e){
                    Log.e(TAG, "closedInternal - " +e);
                }
            }
        }
    }
    private void checkConnection(Context context) {
        // TODO Auto-generated method stub
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isConnectedOrConnecting()){
            Toast.makeText(context, "R.string.no_network_connection_toast", Toast.LENGTH_LONG).show();
            Log.e(TAG, "checkConnection - no connection found");
        }
    }

    private Bitmap processBitmap(String data){

        if(BuildConfig.DEBUG){
            Log.d(TAG, "processBitmap");
        }
        final String key = ImageCache.hashKeyForDisk(data);
        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        DiskLruCache.Snapshot snapshot;

        int id = Integer.parseInt(data);

        ContentResolver crThumb = mContext.getContentResolver();
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
        return curThumb;


//        synchronized(mHttpDiskCacheLock){
//            while(mHttpDiskCacheStarting){
//                try{
//                    mHttpDiskCacheLock.wait();
//                } catch (InterruptedException e) {}
//            }
//
//            if(mHttpDiskCache !=null){
//                try{
//                    snapshot = mHttpDiskCache.get(key);
//                    if(snapshot ==null){
//                        if(BuildConfig.DEBUG){
//                            Log.d(TAG, "processBitmap, not found in http cache, downloading...");
//                        }
//                        DiskLruCache.Editor editor = mHttpDiskCache.edit(key);
//
//                        if(editor != null){
//                            if (downloadUrlToStream(data,
//                                    editor.newOutputStream(DISK_CACHE_INDEX))) {
//                                editor.commit();
//                            } else {
//                                editor.abort();
//                            }
//                        }
//                        snapshot = mHttpDiskCache.get(key);
//                    }
//                    if(snapshot !=null){
//                        fileInputStream =
//                                (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
//                        fileDescriptor = fileInputStream.getFD();
//                    }
//                } catch (IOException e){
//                    Log.e(TAG, "processBitmap - " + e);
//                } catch (IllegalStateException e){
//                    Log.e(TAG, "processBitmap - " +e);
//                } finally {
//                    if(fileDescriptor == null && fileInputStream != null){
//                        try{
//                            fileInputStream.close();
//                        } catch (IOException e) {}
//                    }
//                }
//            }
//        }
//
//        Bitmap bitmap = null;
//        if(fileDescriptor != null){
//            bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, mImageWidth,
//                    mImageHeight, getImageCache());
//        }
//
//        if(fileInputStream != null){
//            try{
//                fileInputStream.close();
//            } catch (IOException e) {}
//        }
 //       return bitmap;
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(String.valueOf(data));
    }

    public boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        // TODO Auto-generated method stub
        disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try{
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while((b = in.read()) !=-1){
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloading bitmap - " + e);
        } finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            try{
                if(out !=null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
            } catch (final IOException e) {}
        }
        return false;
    }

    public static void disableConnectionReuseIfNecessary() {
        // TODO Auto-generated method stub
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO){
            System.setProperty("http.keepAlive", "false");
        }
    }
}
