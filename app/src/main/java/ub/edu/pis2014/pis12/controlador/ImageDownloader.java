package ub.edu.pis2014.pis12.controlador;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import ub.edu.pis2014.pis12.utils.Utils;

/**
 * Created by Dani on 22/02/2015.
 */
public class ImageDownloader extends AsyncTask<String, Void, Boolean> {

    private ImageDownloaderDelegate delegate;
    private URL url;
    private Bitmap bm;
    private ImageView imageView;
    private String imagePath;
    private int position;

    public ImageDownloader(ImageDownloaderDelegate del, String url, String imgPath, ImageView imageViewProductImage, int pos) {
        delegate = del;
        imageView = imageViewProductImage;
        imagePath = imgPath;
        position = pos;
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Boolean doInBackground(String... arg0) {
        try {
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();
            FileOutputStream output = new FileOutputStream(imagePath);
            Utils.copyFile(input, output);
            input.close();
            input = null;
            output.flush();
            output.close();
            output = null;
        } catch (IOException e) {
            Log.e("Hub", "Error getting the image from server : " + e.getMessage().toString());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        //put bm in file
        if (success) {
            delegate.remoteOperationOk(bm, imagePath, imageView, position);
        }
        return;
    }

}

