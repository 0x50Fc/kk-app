package cn.kkmofang.app;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.duktape.*;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.value.V;

/**
 * Created by hailong11 on 2018/6/19.
 */

public class GeoLocation {

    private static final String TAG = "GeoLocation";
    public static void getLocation(final WeakReference<Application> a,final String[] keys,final Map<String,Object> data) {

        Application v = a.get();
        if (v != null){
            GeoManager geo = (GeoManager) v.getRecycle("GeoLocation");
            if (geo == null){
                geo = new GeoManager(v.context());
            }

            geo.startLocation();
            final WeakReference<GeoManager> weakGeo = new WeakReference<>(geo);
            geo.setOnLocationListener(new GeoManager.KKLocationListener() {

                @Override
                public void onLocation(String provider, Location location) {
                    if (location != null){
                        Application fv = a.get();
                        if (fv != null){
                            Log.d(TAG, "updateLocation: " + location.getLatitude() + ":" + location.getLongitude() + ":" + provider);
                            data.put("lat", location.getLatitude());
                            data.put("lng", location.getLongitude());
                            IObserver observer = fv.observer();
                            if (observer != null){
                                observer.set(keys, data);
                                GeoManager geo = weakGeo.get();
                                if (geo != null){
                                    geo.recycle();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onError(String provider, int errcode, String errmsg) {
                    Application fv = a.get();
                    if (fv != null){
                        data.put("errmsg", errmsg);
                        data.put("errno", errcode);
                        IObserver observer = fv.observer();
                        if (observer != null){
                            observer.set(keys, data);
                            GeoManager geo = weakGeo.get();
                            if (geo != null){
                                geo.removeLocationListener(provider);
                            }
                        }
                    }
                }
            });

        }

    }

    public static void openlibs() {


        Protocol.main.addOpenApplication(new Protocol.OpenApplication() {
            @Override
            public void open(Application app) {

                final WeakReference<Application> a = new WeakReference<>(app);

                app.observer().on(new String[]{"geo","location"}, new Listener<Application>() {

                    @Override
                    public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                        if(value != null && value instanceof Map){

                            Object keys = V.get(value,"keys");

                            if(keys != null && keys instanceof List) {

                                List<String> lKeys = (List<String>) keys;
                                String[] sKeys = lKeys.toArray(new String[lKeys.size()]);

                                Object data = V.get(value,"data");

                                if(data == null || !(data instanceof Map)) {
                                    data = new TreeMap<String,Object>();
                                }

                                getLocation(a,sKeys,(Map<String,Object>) data);
                            }
                        }
                    }

                },app, Observer.PRIORITY_NORMAL,false);

            }
        });

    }


    public class GeoListener implements LocationListener,IRecycle{

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void recycle() {

        }
    }
}
