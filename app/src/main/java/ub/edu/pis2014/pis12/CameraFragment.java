package ub.edu.pis2014.pis12;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

import org.json.JSONException;
import org.json.JSONObject;

import ub.edu.pis2014.pis12.controlador.ScreenSlidePagerAdapter;
import ub.edu.pis2014.pis12.model.Dades;
import ub.edu.pis2014.pis12.model.Element;
import ub.edu.pis2014.pis12.model.Obra;
import ub.edu.pis2014.pis12.model.TIPUS_ELEMENT;
import ub.edu.pis2014.pis12.utils.AlphaBoth;
import ub.edu.pis2014.pis12.utils.MultipartUtility;
import ub.edu.pis2014.pis12.utils.PositionToggle;
import ub.edu.pis2014.pis12.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Mostra la pantalla de QR
 * 
 */
public class CameraFragment extends Fragment implements ZBarScannerView.ResultHandler {
	private Context context = null;
	private FrameLayout framePreview = null;
	private ZBarScannerView mScannerView;
	private View takeShot = null;
	private View loadingFade = null;
	private TextView loadingText = null;
	
	public enum CAMERA_MODE
	{
		QR_MODE,
		RECON_MODE
	};
	public CAMERA_MODE mode = CAMERA_MODE.QR_MODE;
	private float maxX = 0;
	private PositionToggle animation = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		context = getActivity();
		mScannerView = new ZBarScannerView(context);    // Programmatically initialize the scanner view
		
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_camera, container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {

		// Save view
		framePreview = (FrameLayout) view.findViewById(R.id.fragment_qr_frame);
		framePreview.addView(mScannerView);
		mScannerView.setQREnabled(true);
		
		// Save loading views
		loadingFade = view.findViewById(R.id.api_loading);
		loadingText = (TextView)view.findViewById(R.id.api_loading_text);

		// Guardem refer�ncies al frame del login i a la barra de registre
		final LinearLayout optionsFrame = (LinearLayout) view.findViewById(R.id.fragment_camera_options);
		final ImageView selectImage = (ImageView) view.findViewById(R.id.fragment_camera_select);

		// Programem un observer per quan, un cop carregat el layout, obtenir el
		// tamany (height)
		final ViewTreeObserver observer = optionsFrame.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				maxX = optionsFrame.getWidth() / 2;
				
				LayoutParams lp = selectImage.getLayoutParams();
				lp.width = (int) maxX;
				selectImage.setLayoutParams(lp);
				
				// Animacio
				animation = new PositionToggle(null, selectImage, optionsFrame.getWidth() / 2, 500);
				
				// Eliminem l'observer
				optionsFrame.getViewTreeObserver()
						.removeGlobalOnLayoutListener(this);
			}
		});
		
		view.findViewById(R.id.fragment_camera_qr).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (mode != CAMERA_MODE.QR_MODE)
				{
					mode = CAMERA_MODE.QR_MODE;
					mScannerView.setQREnabled(true);
					mScannerView.setReconEnabled(false);
					takeShot.setVisibility(View.GONE);
					
					// Animacio
					animation.execute();
				}
			}
		});
		
		view.findViewById(R.id.fragment_camera_recon).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (mode != CAMERA_MODE.RECON_MODE)
				{
					mode = CAMERA_MODE.RECON_MODE;
					mScannerView.setQREnabled(false);
					mScannerView.setReconEnabled(true);
					takeShot.setVisibility(View.VISIBLE);
					
					// Animacio
					animation.execute();
				}
			}
		});
		
		takeShot = view.findViewById(R.id.camera_take_shot);
		takeShot.setVisibility(View.GONE);
		takeShot.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (mode == CAMERA_MODE.RECON_MODE)
				{
					mScannerView.takeShot();
				}
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
	}
	
	@Override
	public void onPause() {
		super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
	}
	
	public void setCameraFocused(boolean focused)
	{
		mScannerView.setCameraFocused(focused);
		mScannerView.setAutoFocus(focused);
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);

		if (menuVisible)
		{
			setCameraFocused(true);
		}
	}
	
	@Override
	public void handleResult(Result rawResult) {
		ViewPager pager = (ViewPager) getActivity().findViewById(R.id.main_pager);
		pager.setCurrentItem(1, true);
		setCameraFocused(false);
		
		if (rawResult.getBarcodeFormat() == BarcodeFormat.QRCODE) {
			String contents = rawResult.getContents();

			try {
				JSONObject qr = new JSONObject(contents);
				if (qr.getInt("Verification") != 1337) {
					Toast.makeText(getActivity(), R.string.toast_invalid_qr, Toast.LENGTH_LONG).show();
					return;
				}

				int id = qr.getInt("ID");
				int tipus = qr.getInt("Type");
				Element element = null;

				TIPUS_ELEMENT tipusElement = TIPUS_ELEMENT.values()[tipus];
				switch (tipusElement) {
				case TIPUS_OBRA:
					element = Dades.getObra(id);
					break;
				case TIPUS_MUSEU:
					element = Dades.getMuseu(id);
					break;

				case TIPUS_AUTOR:
					element = Dades.getAutor(id);
					break;

				default:
					break;
				}

				if (element == null) {
					Toast.makeText(getActivity(), R.string.toast_wrong_qr,
							Toast.LENGTH_LONG).show();
					return;
				}
				
				openInformation(element, tipusElement);

			} catch (JSONException e) {
				Toast.makeText(getActivity(), R.string.toast_invalid_qr,
						Toast.LENGTH_LONG).show();
			} catch (ArrayIndexOutOfBoundsException e) {
				Toast.makeText(getActivity(), R.string.toast_invalid_qr,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void handleRecon(final Bitmap bitmap) {
		loadingFade.setVisibility(View.VISIBLE);
		loadingText.setText(R.string.API_sending_image);
		
		if (bitmap == null)
		{
			loadingFade.setVisibility(View.GONE);
			Toast.makeText(context, R.string.RECON_error_bitmap, Toast.LENGTH_LONG).show();
			return;
		}
		
		new AsyncTask<Void, Void, List<String>>() {

			@Override
			protected List<String> doInBackground(Void... params) {
				String charset = "UTF-8";
		        String requestURL = "http://161.116.52.239:8080/android";
		 
		        try {
		            MultipartUtility multipart = new MultipartUtility(requestURL, charset);
		             
		            multipart.addHeaderField("User-Agent", "eMuseum");             
		            multipart.addFileBytesPart("upload", bitmap);
		            
		            return multipart.finish();
		        } catch (IOException ex) {
		            return new ArrayList<String>();
		        }
			}
			
			protected void onPostExecute(List<String> response)
			{
				loadingFade.setVisibility(View.GONE);
				
				if (response.size() > 0)
				{
					try
					{
						int id = Integer.parseInt(response.get(0));
						Obra obra = Dades.getObra(id);
						
						if (obra != null)
						{
							openInformation(obra, TIPUS_ELEMENT.TIPUS_OBRA);
						}
						else
						{
							Toast.makeText(context, R.string.RECON_error_unknown, Toast.LENGTH_LONG).show();
						}
					}
					catch (NumberFormatException e)
					{
						Toast.makeText(context, R.string.RECON_error_unknown, Toast.LENGTH_LONG).show();
					}
				}
				else
				{
					Toast.makeText(context, R.string.RECON_error_unknown, Toast.LENGTH_LONG).show();
				}
			};
		}.execute();		
	}
	
	void openInformation(Element element, TIPUS_ELEMENT tipusElement)
	{
		ViewPager pager = (ViewPager) getActivity().findViewById(R.id.main_pager);
		pager.setCurrentItem(1, true);
		setCameraFocused(false);

		if (Utils.isExpandedMode(getActivity())) {
			ScreenSlidePagerAdapter adapter = (ScreenSlidePagerAdapter) pager.getAdapter();

			new AlphaBoth(null, pager, 500).execute();

			Fragment fragment = InfoFragment.newInstance(element);
			adapter.customize(fragment);
			pager.setCurrentItem(1, true);
		}
		// Sino, actuem mitjan�ant intents
		else {
			Intent i = new Intent(getActivity(), InfoActivity.class);
			i.putExtra("id", element.getId());
			i.putExtra("tipus", tipusElement);
			startActivityForResult(i,
					LlistaObresFragment.ACTIVITY_CREATE);
		}
	}
}
