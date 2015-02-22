package me.dm7.barcodescanner.core;

import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

@SuppressLint("NewApi")
public abstract class BarcodeScannerView extends FrameLayout implements Camera.PreviewCallback  {
    private Camera mCamera;
    private CameraPreview mPreview;
    private ViewFinderView mViewFinderView;
	private RelativeLayout mRelative;
    private Rect mFramingRectInPreview;
    
    private static AsyncTask<Void, Void, Camera> getCameraInst = null;
    private static final Semaphore available = new Semaphore(1, true);
    
    protected boolean focused;
    protected boolean qrEnabled;
    protected boolean reconEnabled;
    protected boolean takeShot;

    public BarcodeScannerView(Context context) {
        super(context);
        setupLayout();
    }

    public BarcodeScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupLayout();
    }

    public void setupLayout() {
        mPreview = new CameraPreview(getContext());
        mViewFinderView = new ViewFinderView(getContext());
        mRelative = new RelativeLayout(getContext());
    }

	private Camera.PreviewCallback getPreviewCallback() {
		return this;
	}
	
	public void setCameraFocused(boolean focused)
	{
		this.focused = focused;
		setOneShotPreview(focused);
	}
    
	public void setReconEnabled(boolean enabled)
	{
		reconEnabled = enabled;
	}
	
    public void setQREnabled(boolean enabled)
    {
    	qrEnabled = enabled;
		mViewFinderView.setDrawEnabled(enabled);
    }
    
    public void takeShot()
    {
    	takeShot = true;
    	setOneShotPreview(true);
    }

    private void setOneShotPreview(boolean enabled)
    {
    	if (enabled && mCamera != null)
    	{
    		mCamera.setOneShotPreviewCallback(this);
    	}
    }
    
    public void startCamera() {
		getCameraInst = new AsyncTask<Void, Void, Camera>() {
			// Get a camera instance on a worked thread
			@Override
			protected void onPreExecute() {
				removeAllViews();
				mRelative.removeAllViews();
			}
			
			@Override
			protected Camera doInBackground(Void... param) {
				// Intentem obtenir permis per engegar la camera
				// No retorna fins que no el tenim, es a dir, fins que 
				// no acaba la AsyncTask de stopCamera
				try {
					available.acquire();
				} catch (InterruptedException e) {
					return null;
				}

				mCamera = CameraUtils.getCameraInstance();
				return mCamera;
			}

			@Override
			protected void onPostExecute(Camera mCamera) {
				if (mCamera != null) {
					mViewFinderView.setupViewFinder();
					mPreview.setCamera(mCamera, getPreviewCallback());
					mPreview.initCameraPreview();
					
					mRelative.setGravity(Gravity.CENTER);
					mRelative.setBackgroundColor(Color.BLACK);
					mRelative.addView(mPreview);
					
					addView(mRelative);
			        addView(mViewFinderView);
				}
				
				// Indiquem que hem finalitzat i alliberem el semaphore
				getCameraInst = null;
				available.release();
			}
		};
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getCameraInst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{});
		} else {
			getCameraInst.execute();
		}
    }

    public void stopCamera() {
    	// Intentem obtenir permis, si no ens el dona retorna al moment false
    	// Aix� implicaria que actualment hi ha una AsnycTask de startCamera en curs
    	final boolean acquired = available.tryAcquire();
    	
    	// Cal parar si hi ha camera i/o si hi ha una AsyncTask de startCamera
    	final boolean mustStop = getCameraInst != null || mCamera != null;
    	
        if(mustStop) {        	
        	AsyncTask<Void, Void, Camera> releaseCameraInst = new AsyncTask<Void, Void, Camera>() {

        		@Override
        		protected void onPreExecute() {
					mPreview.stopCameraPreview();
		            mPreview.setCamera(null, null);
        		}
        		
				@Override
				protected Camera doInBackground(Void... params) {
					// Si abans no hem aconseguit permis, esperem
					// a tenir-l'ho (a que acabi la AsyncTask)
					if (!acquired)
					{
						try
						{
							available.acquire();
						} catch (InterruptedException e){}
					}
					
		            mCamera.release();
		            mCamera = null;
		            
		            // Alliberem la semaphore
		            available.release();
		            
					return null;
				}
				
				@Override
				protected void onPostExecute(Camera result) {
					// Si al principi no haviem aconseguit la semaphore, aixo vol dir
					// que entre que s'executava i no doInBackground, el thread de startCamera
					// ha cridat el metode mPreview.startCameraPreview, pel que ara hem de tornar
					// a parar-l'ho
					if (!acquired)
					{
						mPreview.stopCameraPreview();
					}
				}
        	};
        	
        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        		releaseCameraInst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{});
    		} else {
    			releaseCameraInst.execute();
    		}
        }
    }

    public synchronized Rect getFramingRectInPreview(int width, int height) {
        if (mFramingRectInPreview == null) {
            Rect framingRect = mViewFinderView.getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            Point screenResolution = DisplayUtils.getScreenResolution(getContext());
            Point cameraResolution = new Point(width, height);

            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null;
            }

            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;

            mFramingRectInPreview = rect;
        }
        return mFramingRectInPreview;
    }

    public void setFlash(boolean flag) {
        if(CameraUtils.isFlashSupported(getContext()) && mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if(flag) {
                if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            mCamera.setParameters(parameters);
        }
    }

    public boolean getFlash() {
        if(CameraUtils.isFlashSupported(getContext()) && mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void toggleFlash() {
        if(CameraUtils.isFlashSupported(getContext()) && mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            mCamera.setParameters(parameters);
        }
    }

    public void setAutoFocus(boolean state) {
        if(mPreview != null) {
            mPreview.setAutoFocus(state);
        }
    }
}
