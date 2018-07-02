package cn.kkmofang.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GeoManager implements IGeoManager {
    public static final long LocationUpdateInterval = 60 * 60 * 1000;
    private LocationManager lm;
    private ILocationListener _locationListener;
    private Context _context;


    public GeoManager(Context context) {
        if (context != null) {
            _context = context.getApplicationContext();
        }
        initLocationManager();
    }

    private void initLocationManager() {
        if (_context != null) {
            lm = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
        }
    }


    private boolean checkPermissions(String pName) {
        boolean p = false;
        if (_context != null) {
            try {
                p = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(_context, pName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    private Map<String, LocationListener> _locationListeners;
    @Override
    public void startLocation(ILocationListener listener){
        startLocation(LocationUpdateInterval, listener);//默认超时时间
    }

    @Override
    public void startLocation(long timeLimit, ILocationListener listener) {
        _locationListener = listener;
        if (timeLimit <= 0)timeLimit = LocationUpdateInterval;
        if (!checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            || !checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)){
            if (_locationListener != null){
                _locationListener.onError( -1, "location permission is rejected");
            }
            return;
        }
        if (lm != null) {
            List<String> providers = lm.getAllProviders();
            if (providers != null) {
                if (_locationListeners == null){
                    _locationListeners = new TreeMap<>();
                }

                for (final String provider : providers) {
                    if (lm.isProviderEnabled(provider)){
                        if (!_locationListeners.containsKey(provider)){
                            _locationListeners.put(provider, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    if (_locationListener != null){
                                        _locationListener.onLocation(location.getLatitude(), location.getLongitude());
                                    }
                                }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {
                                    switch (status){
                                        case LocationProvider.OUT_OF_SERVICE://0
                                            if (_locationListener != null){
                                                _locationListener.onError(status, provider + " is out of service");
                                                removeLocationListener(provider);
                                            }
                                            break;
                                        case LocationProvider.TEMPORARILY_UNAVAILABLE://1
                                            if (_locationListener != null){
                                                _locationListener.onError(status, provider + " is unavailable");
                                                removeLocationListener(provider);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }

                                @Override
                                public void onProviderEnabled(String provider) {

                                }

                                @Override
                                public void onProviderDisabled(String provider) {
                                    if (_locationListener != null){
                                        _locationListener.onError(-2, provider + " is disabled");
                                        removeLocationListener(provider);
                                    }
                                }
                            });
                        }

                        Location location = lm.getLastKnownLocation(provider);
                        if (location != null){
                            long lastFixTime = location.getTime();
                            if (System.currentTimeMillis() - lastFixTime >= timeLimit){
                                lm.requestLocationUpdates(provider, timeLimit,
                                        0, _locationListeners.get(provider), Looper.myLooper());
                            }else {
                                if (_locationListener != null){
                                    _locationListener.onLocation(location.getLatitude(), location.getLongitude());
                                    return;
                                }
                            }

                        }else {
                            lm.requestLocationUpdates(provider, timeLimit,
                                    0, _locationListeners.get(provider), Looper.myLooper());
                        }

                    }
                }
            }
        }
    }


    private void removeLocationListeners(){
        if (lm != null){
            if (_locationListeners != null){
                Iterator<Map.Entry<String, LocationListener>> i = _locationListeners.entrySet().iterator();
                if (i.hasNext()){
                    Map.Entry<String, LocationListener> n = i.next();
                    removeLocationListener(n.getKey());
                }
            }
        }
        if (_locationListeners != null){
            _locationListeners.clear();
        }
    }

    public void removeLocationListener(String key){
        if (lm != null){
            if (_locationListeners != null){
                if (checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                        && checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)){
                    LocationListener listener = _locationListeners.get(key);
                    if (listener != null){
                        lm.removeUpdates(listener);
                    }
                }
                _locationListeners.remove(key);
            }
        }
    }

    @Override
    public boolean isLocationError(){
        return _locationListeners==null||_locationListeners.isEmpty();
    }


    @Override
    public void recycle() {
        removeLocationListeners();
        _locationListener = null;
        lm = null;
    }
}
