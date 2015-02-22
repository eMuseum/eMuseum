package me.dm7.barcodescanner.zbar;

import java.util.Collection;
import java.util.List;

import me.dm7.barcodescanner.core.BarcodeScannerView;
import me.dm7.barcodescanner.core.DisplayUtils;
import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

public class ZBarScannerView extends BarcodeScannerView {
    public interface ResultHandler {
        public void handleResult(Result rawResult);
        public void handleRecon(final Bitmap bitmap);
    }

    static {
        System.loadLibrary("iconv");
    }

    private ImageScanner mScanner;
    private List<BarcodeFormat> mFormats;
    private ResultHandler mResultHandler;

    public ZBarScannerView(Context context) {
        super(context);
        setupScanner();
    }

    public ZBarScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupScanner();
    }

    public void setFormats(List<BarcodeFormat> formats) {
        mFormats = formats;
        setupScanner();
    }

    public void setResultHandler(ResultHandler resultHandler) {
        mResultHandler = resultHandler;
    }

    public Collection<BarcodeFormat> getFormats() {
        if(mFormats == null) {
            return BarcodeFormat.ALL_FORMATS;
        }
        return mFormats;
    }

    public void setupScanner() {
    	focused = false;
    	qrEnabled = false;
    	reconEnabled = false;
    	takeShot = false;
    	
        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);

        mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
        for(BarcodeFormat format : getFormats()) {
            mScanner.setConfig(format.getId(), Config.ENABLE, 1);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    	if (!focused || (reconEnabled && !takeShot))
    	{
    		return;
    	}
    	
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;

        if(DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++)
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
            int tmp = width;
            width = height;
            height = tmp;
            data = rotatedData;
        }

        if (reconEnabled && takeShot)
        {
        	int outData[] = new int[data.length];
        	decodeYUV420SP(outData, data, width, height);
        	Bitmap bitmap = tryCreateScaledBitmap(outData, width, height, 250);
        	mResultHandler.handleRecon(bitmap);
        	takeShot = false;
        }
        
        if (!qrEnabled)
        {
        	return;
        }

        Image barcode = new Image(width, height, "Y800");
        barcode.setData(data);
        
        int result = mScanner.scanImage(barcode);

        if (result != 0) {
            if(mResultHandler != null) {
                SymbolSet syms = mScanner.getResults();
                Result rawResult = new Result();
                for (Symbol sym : syms) {
                    String symData = sym.getData();
                    if (!TextUtils.isEmpty(symData)) {
                        rawResult.setContents(symData);
                        rawResult.setBarcodeFormat(BarcodeFormat.getFormatById(sym.getType()));
                        break;
                    }
                }
                mResultHandler.handleResult(rawResult);
            }
        } else {
            camera.setOneShotPreviewCallback(this);
        }
    }
    
    private void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0; else if (r > 262143) r = 262143;
                if (g < 0) g = 0; else if (g > 262143) g = 262143;
                if (b < 0) b = 0; else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }
    
    private Bitmap tryCreateScaledBitmap(int[] data, int width, int height, int dstSize)
    {
    	Bitmap bitmap = createBitmap(data, width, height);
    	
    	float ratio = dstSize / (float)Math.min(width, height);
    	if (ratio < 1 && bitmap != null)
    	{
    		Matrix matrix = new Matrix();
    		matrix.postScale(ratio, ratio);
    		    		
        	try
        	{
        		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        	}
        	catch (OutOfMemoryError e)
        	{}
    	}
    	
    	return bitmap;
    }
    
    private Bitmap createBitmap(int[] data, int width, int height)
    {
    	Bitmap bitmap = null;
    	try
    	{
    		bitmap = Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
    	}
    	catch (OutOfMemoryError e)
    	{}
    	
    	return bitmap;
    }
    
}