package com.player.bingry.bingryplayer;


import android.Manifest;
import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Trace;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;


import com.player.bingry.bingryplayer.provider.BingryContent;
import com.player.bingry.bingryplayer.ui.ContentDetailActivity;
import com.player.bingry.bingryplayer.ui.NungryExoplayer;
import com.player.bingry.bingryplayer.ui.PlayerListFragment;
import com.player.bingry.bingryplayer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.AUTHORITY;

//public class MainActivity extends AppCompatActivity implements PlayerListFragment.OnListFragmentInteractionListener {
public class MainActivity extends FragmentActivity implements PlayerListFragment.OnListFragmentInteractionListener{
    private static final String TAG = "PlayerListActivity";
    private static final String AUTHORITY=
            BuildConfig.APPLICATION_ID+".provider";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        3188);
             }
        }

        if(getSupportFragmentManager().findFragmentByTag(TAG) == null){
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, new PlayerListFragment());
            ft.commit();
        }

        makeVodList();

        for (int i = 0; i < BingryContent.VOD_ITEMS.size(); i++) {
            BingryContent.VodData item = BingryContent.VOD_ITEMS.get(i);
            String Title = item.mVideoTitle;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 3188: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    private void makeVodList(){
        ArrayList<BingryContent.VodData> arr_ListItems = new ArrayList<BingryContent.VodData>();
        String[] proj = {
                MediaStore.Video.VideoColumns.ALBUM,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED};

        Cursor videocursor;
        videocursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                proj, null, null, null);

        if(videocursor.moveToFirst()){
            int n_album = videocursor.getColumnIndex(MediaStore.Video.VideoColumns.ALBUM);
            int n_id = videocursor.getColumnIndex(MediaStore.Video.Media._ID);
            int n_title = videocursor.getColumnIndex(MediaStore.Video.Media.TITLE);
            int n_data  = videocursor.getColumnIndex(MediaStore.Video.Media.DATA);
            int n_displayname = videocursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            int n_size    = videocursor.getColumnIndex(MediaStore.Video.Media.SIZE);
            int n_regdttm   = videocursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED);
            //int n_resolution = videocursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION);

            do{
                BingryContent.VodData item;
                String vod_album  = videocursor.getString(n_album);
                String vod_id   = videocursor.getString(n_id);
                String vod_title  = videocursor.getString(n_title);
                String vod_data  = videocursor.getString(n_data);
                String vod_size  = videocursor.getString(n_size);
                String vod_regdttm  = videocursor.getString(n_regdttm);
                //String vod_resolution = videocursor.getString(n_resolution);
                //Trace.d("REGDTTM : " +  vod_regdttm);
                item = new BingryContent.VodData(
                        vod_album,
                        vod_id,
                        vod_title,
                        vod_data,
                        vod_size,
                        vod_regdttm
                );
                //arr_ListItems.add(item);
                BingryContent.addVodItem(item);
            }while(videocursor.moveToNext());
        }

        //videocursor.close();
    }

    @Override
    public void onListFragmentInteraction(PlayerListFragment.BingryItemRecyclerViewAdapter.ViewHolder holder) {

        Intent intent = new Intent(this, NungryExoplayer.class);
        intent.putExtra("path", holder.mVodItem.mData);
        startActivity(intent);
        if(isIntentSafe(intent))
            startActivity(intent);
        else {
            Log.e("GUN", "실행할 수 있는 앱이 없습니다 -_-");
        }
      //  Intent i=new Intent(this, NungryExoplayer.class);
       // i.setAction("com.player.bingry.bingryplayer.action.VIEW");

       // File file = new File(holder.mVodItem.mData);

       // Uri outputUri=FileProvider.getUriForFile(this, AUTHORITY, file);
//        Intent i=new Intent("com.player.bingry.bingryplayer.action.VIEW", Uri.parse("content://"+ AUTHORITY + holder.mVodItem.mData) );
//        i.putExtra("path", holder.mVodItem.mData);
       // i.setData(outputUri);
       // Intent i=new Intent("com.player.bingry.bingryplayer.action.VIEW", Uri.parse(outputUri.toString()));
       // Uri test = i.getData();
       // i.setData(test);
       // Uri uri = Uri.parse("content://" + holder.mVodItem.mData);
       // i.setData(uri);
        //i.setDataAndType(outputUri, "image/jpeg");
      //  i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
       // i.putExtra("path", holder.mVodItem.mData);
//        if(isIntentSafe(i))
//            startActivity(i);
//        else {
//            startActivity(i);
//            Log.e("GUN", "실행할 수 있는 앱이 없습니다 -_-");
//        }
       //startActivity(i);

//        final Intent i = new Intent(getBaseContext(), ContentDetailActivity.class);
//        String id = (String) holder.mIdView.getText();
//
//        i.putExtra(ContentDetailActivity.EXTRA_IMAGE, (int)Integer.parseInt(id));
//
//        if(Utils.hasJellyBean()){
//            ActivityOptions options =
//                    ActivityOptions.makeScaleUpAnimation(holder.mView, 0, 0, holder.mView.getWidth(), holder.mView.getHeight());
//            //getApplicationContext().startActivity(i, options.toBundle());
//            startActivity(i);
//        } else {
//            startActivity(i);
//        }

    }

    private boolean isIntentSafe(Intent intent)
    {
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return (activities.size() > 0);
    }


}
