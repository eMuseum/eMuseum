package ub.edu.pis2014.pis12.controlador;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by Dani on 22/02/2015.
 */
public interface ImageDownloaderDelegate {
    public void remoteOperationOk(Bitmap bmp, String imagePath, ImageView imageView, int position);
}
