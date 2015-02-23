package ub.edu.pis2014.pis12.controlador;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Dani on 22/02/2015.
 */
public class ImageController implements ImageDownloaderDelegate {

    private static ImageController controller = null;
    private static Context context;
    private Context appContext;
    private GridView grid;

    public ImageController() {
        controller = getInstance();
    }

    public static ImageController getInstance() {
        createInstance();
        return controller;
    }

    private ImageController(Context pContext) {
        ImageController.context = pContext;
    }

    private static void createInstance() {
        if (controller == null) {
            if (ImageController.context == null) {
                controller = new ImageController(ImageController.context);
            }
        }
    }

    public void setImageWithURL(String imageURL, ImageView imageView) {
        if (imageURL != null) {
            //Get the image name
            Uri uri = Uri.parse(imageURL);
            String fileName = uri.getLastPathSegment();
            //Get the image path
            String imagePath = pathForImageWithName(fileName);
            //Check if folder can't create
            if (imagePath == null) {
                return;
            }
            //Check if the image is downloaded
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                /* De moment es innecesari, si no hi ha internet no fara la descarrega
                ConnectivityManager connMgr = (ConnectivityManager) MainActivity.activity.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (Utils.checkNetworkConnectionState(connMgr) == false) {
                    return;//No conection
                }
                */
                //Download the image asynchronously
                ImageDownloader imDown = new ImageDownloader(this, imageURL, imagePath, imageView, 0);
                imDown.execute("0");
            } else {
                //if the image exists in device, set it the imageView
                setImageWithPath(imageView, imagePath);
            }
        }
    }

    //Set the image in a ImageView with the device path, if not exist, download from the server and save on device
    public void setImageWithURL(GridView grid, Context context, String imageURL, ImageView imageView, int pos) {
        this.grid = grid;
        appContext = context;
        if (imageURL != null) {
            //Get the image name
            Uri uri = Uri.parse(imageURL);
            String fileName = uri.getLastPathSegment();
            //Get the image path
            String imagePath = pathForImageWithName(fileName);
            //Check if folder can't create
            if (imagePath == null) {
                return;
            }
            //Check if the image is downloaded
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                /* De moment es innecesari, si no hi ha internet no fara la descarrega
                ConnectivityManager connMgr = (ConnectivityManager) MainActivity.activity.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (Utils.checkNetworkConnectionState(connMgr) == false) {
                    return;//No conection
                }
                */
                //Download the image asynchronously
                ImageDownloader imDown = new ImageDownloader(this, imageURL, imagePath, imageView, pos);
                imDown.execute("0");
            } else {
                //if the image exists in device, set it the imageView
                setImageWithPath(imageView, imagePath);
            }
        }
    }

    private String pathForImageWithName(String name) {
        String imagesDirPath = getImagesDir();
        if (imagesDirPath.equals("-1")) {
            return imagesDirPath;
        }
        return imagesDirPath + "/" + name;
    }

    private String getImagesDir() {
        boolean success;
        ContextWrapper cont = new ContextWrapper(appContext);
        String appPath = cont.getFilesDir().getPath();
        File imageFolder = new File(appPath + "/images");
        if (!imageFolder.exists()) {
            success = imageFolder.mkdir();
            if (success) { //if folder can create
                return imageFolder.getPath();
            } else {
                return null;
            }
        } else {
            return imageFolder.getPath();
        }
    }

    @Override
    public void remoteOperationOk(Bitmap bmp, String imagePath, ImageView imageView, int position) {
        if (grid == null) {
            Bitmap imageBitmap = decodeFile(new File(imagePath));
            imageView.setImageBitmap(imageBitmap);
            imageView.setVisibility(View.VISIBLE);
        } else {
            int validPosition = grid.getFirstVisiblePosition() + grid.getChildCount();
            if (position <= validPosition && position >= grid.getFirstVisiblePosition()) {
                Bitmap imageBitmap = decodeFile(new File(imagePath));
                imageView.setImageBitmap(imageBitmap);
                imageView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setImageWithPath(ImageView imageView, String imagePath) {
        Bitmap imageBitmap = decodeFile(new File(imagePath));
        imageView.setImageBitmap(imageBitmap);
        imageView.setVisibility(View.VISIBLE);
    }

    //Decode a imageFile to bitmap, if its very large, do more smaller
    private Bitmap decodeFile(File f) {
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 70;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale++;
            }

            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

