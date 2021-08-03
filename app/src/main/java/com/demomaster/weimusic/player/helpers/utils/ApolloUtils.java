/**
 * 
 */

package com.demomaster.weimusic.player.helpers.utils;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.androidquery.util.AQUtility;
import com.demomaster.weimusic.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.demomaster.weimusic.constant.Constants.MIME_TYPE;


/**
 * @author Andrew Neal
 * @Note Various methods used to help with specific Apollo statements
 */
public class ApolloUtils {


    /**
     * @param context
     * @return whether there is an active data connection
     */
    public static boolean isOnline(Context context) {
        boolean state = false;
        ConnectivityManager cm = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null) {
            state = wifiNetwork.isConnectedOrConnecting();
        }

        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null) {
            state = mobileNetwork.isConnectedOrConnecting();
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            state = activeNetwork.isConnectedOrConnecting();
        }
        return state;
    }

    /**
     * @param context
     * @return if a Tablet is the device being used
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * UP accordance without the icon
     * 
     * @param actionBar
     */
    public static void showUpTitleOnly(ActionBar actionBar) {
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE,
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
                        | ActionBar.DISPLAY_SHOW_HOME);
    }

    /**
     * Header used in the track browser
     * 
     * @param fragment
     * @param view
     * @param string
     */
    public static void listHeader(Fragment fragment, View view, String string) {
        if (fragment.getArguments() != null) {
            TextView mHeader = (TextView)view.findViewById(R.id.title);
            String mimetype = fragment.getArguments().getString(MIME_TYPE);
            if (Audio.Artists.CONTENT_TYPE.equals(mimetype)) {
                mHeader.setVisibility(View.VISIBLE);
                mHeader.setText(string);
            } else if (Audio.Albums.CONTENT_TYPE.equals(mimetype)) {
                mHeader.setVisibility(View.VISIBLE);
                mHeader.setText(string);
            }
        }
    }

    /**
     * Sets the ListView paddingLeft for the header
     * 
     * @param fragment
     * @param mListView
     */
    public static void setListPadding(Fragment fragment, ListView mListView, int left, int top,
            int right, int bottom) {
        if (fragment.getArguments() != null) {
            String mimetype = fragment.getArguments().getString(MIME_TYPE);
            if (Audio.Albums.CONTENT_TYPE.equals(mimetype)) {
                mListView.setPadding(AQUtility.dip2pixel(fragment.getActivity(), left), top,
                        AQUtility.dip2pixel(fragment.getActivity(), right), bottom);
            } else if (Audio.Artists.CONTENT_TYPE.equals(mimetype)) {
                mListView.setPadding(AQUtility.dip2pixel(fragment.getActivity(), left), top,
                        AQUtility.dip2pixel(fragment.getActivity(), right), bottom);
            }
        }
    }

    // Returns if we're viewing an album
    public static boolean isAlbum(String mimeType) {
        return Audio.Albums.CONTENT_TYPE.equals(mimeType);
    }

    // Returns if we're viewing an artists albums
    public static boolean isArtist(String mimeType) {
        return Audio.Artists.CONTENT_TYPE.equals(mimeType);
    }

    // Returns if we're viewing a genre
    public static boolean isGenre(String mimeType) {
        return Audio.Genres.CONTENT_TYPE.equals(mimeType);
    }

    /**
     * @param artistName
     * @param id
     * @param key
     * @param context
     */
    public static void setArtistId(String artistName, long id, String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences(key, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(artistName, id);
        editor.commit();
    }

    /**
     * @param artistName
     * @param key
     * @param context
     * @return artist ID
     */
    public static Long getArtistId(String artistName, String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences(key, 0);
        return settings.getLong(artistName, 0);
    }

    /**
     * @param artistName
     */
    public static void shopFor(Context mContext, String artistName) {
        String str = "https://market.android.com/search?q=%s&c=music&featured=MUSIC_STORE_SEARCH";
        Intent shopIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(str,
                Uri.encode(artistName))));
        shopIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shopIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(shopIntent);
    }

    /**
     * Replace the characters not allowed in file names with underscore
     * @param name
     * @return
     */
    public static String escapeForFileSystem(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]+", "_");
    }

    /**
     * Static utility function to download the file from the specified URL to the specified file.
     * @param urlString
     * @param outFile
     * @return true if the download succeeded false otherwise
     */
    public static boolean downloadFile(String urlString, File outFile) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;

        try {
            File dir = outFile.getParentFile();
            if (!dir.exists() && !dir.mkdirs())
                return false;

            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            final InputStream in =
                    new BufferedInputStream(urlConnection.getInputStream());
            out = new BufferedOutputStream(new FileOutputStream(outFile));

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }

        } catch (final IOException e) {
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    return false;
                }
            }
        }
        return true;
    }

}
