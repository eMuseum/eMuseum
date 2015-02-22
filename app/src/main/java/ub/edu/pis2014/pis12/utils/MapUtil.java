package ub.edu.pis2014.pis12.utils;

import java.util.ArrayList;
import java.util.Iterator;

import ub.edu.pis2014.pis12.controlador.GridAdapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapUtil {
	// Crear un arrayList para guardar Location de cada museo
	final static float MIN_DISTANCE = 5;
	private static ArrayList<OnMapUpdate> listeners = new ArrayList<OnMapUpdate>();
	private static LocationListener locationListener = null;
	private static final long shortUpdateInterval = 100;
	private static final long longUpdateInterval = 180000;
	private static Location lastKnown = null;
	
	public MapUtil()
	{}
	
	
	public static boolean startGridListener(Context context){
		return startListener(context, longUpdateInterval, new OnLocationChanged() {
			
			@Override
			public void onChange(Location location) {
				LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

				for (int i=0; i<listeners.size(); i++){
					OnMapUpdate listener = listeners.get(i);

					double dist = location.distanceTo(listener.getLocation());
					listener.onUpdate(dist);
				}
			}
		});
	}
	
	public static void stopListener(Context context)
	{
		if (locationListener != null)
		{
			LocationManager gridLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			gridLocationManager.removeUpdates(locationListener);
		}
	}
	
	public static boolean startMapListener(Context context, OnLocationChanged callback)
	{
		return startListener(context, shortUpdateInterval, callback);
	}

	private static boolean startListener(final Context context, final long interval, final OnLocationChanged callback) {

		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS)
		{
			return false;
		}

		LocationManager gridLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean isGPSEnabled = gridLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isNetWorkEnabled = gridLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {

			// Metode que es crida automaticament el metode
			// requestLocationUpdate per actualitzar nostra localitzacio
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network
				// location provider.
				if (location != null) {
					lastKnown = location;
					callback.onChange(location);
				}
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
				switch (status) {
				case LocationProvider.AVAILABLE:
					break;
				case LocationProvider.OUT_OF_SERVICE:
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					break;
				}
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(true);
		String provider = gridLocationManager.getBestProvider(criteria, true);

		if (provider == "") {
			if (isGPSEnabled) {
				gridLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, MIN_DISTANCE, locationListener);
			} else if (isNetWorkEnabled) {
				gridLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, interval, MIN_DISTANCE, locationListener);
			}
			else{
				return false;
			}	
		} else {
			gridLocationManager.requestLocationUpdates(provider, interval, MIN_DISTANCE, locationListener);
		}
		
		return true;
	}
	
	public static String getDistanceFromLastKnown(double latitude, double longitude)
	{
		if (lastKnown == null)
			return "";
		
		Location location = new Location("temp");
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		
		double distance = lastKnown.distanceTo(location);
		if (distance < 1000)
			return (int)distance + "m";
		return (int)distance/1000 + "km";
	}
	
	public static void addListener(OnMapUpdate up){
		listeners.add(up);
	}
}