package cn.kkmofang.app;

import android.content.Context;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.view.value.V;

/**
 * Created by hailong11 on 2018/6/19.
 */

public class GeoLocation {
    private static String geoProvider;

    public static void getLocation(final WeakReference<Application> a,final String[] keys,final Map<String,Object> data) {

        Application v = a.get();
        if (v != null){
            IGeoManager geo = (IGeoManager) v.getRecycle("GeoLocation");
            if (geo == null){
                if (TextUtils.isEmpty(geoProvider)){
                    geo = new GeoManager(v.context());
                }else {
                    try {
                        geo = (IGeoManager) Class.forName(geoProvider).getConstructor(Context.class).newInstance(v.context());
                    } catch (Exception e){
                        geo = new GeoManager(v.context());
                    }
                }
            }

            long timeLimit = 0;
            if (data != null){
                timeLimit = V.longValue(data.get("locTimeLimit"), 0);
            }
            final WeakReference<IGeoManager> weakGeo = new WeakReference<>(geo);
            ILocationListener listener = new ILocationListener() {


                @Override
                public void onLocation(double lat, double lon) {
                    Application fv = a.get();
                    if (fv != null) {
                        if (data != null){
                            data.put("lat", lat);
                            data.put("lng", lon);
                        }

                        IObserver observer = fv.observer();
                        if (observer != null) {
                            observer.set(keys, data);
                            IGeoManager geo = weakGeo.get();
                            if (geo != null) {
                                geo.recycle();
                            }
                        }
                    }
                }

                @Override
                public void onError(int errcode, String errmsg) {
                    Application fv = a.get();
                    if (fv != null) {
                        if (data != null){
                            data.put("errmsg", errmsg);
                            data.put("errno", errcode);
                        }
                        IGeoManager geo = weakGeo.get();
                        if (geo != null) {
                            if (geo.isLocationError()) {
                                IObserver observer = fv.observer();
                                if (observer != null) {
                                    observer.set(keys, data);
                                }
                            }
                        }

                    }
                }
            };
            geo.startLocation(timeLimit, listener);

        }

    }

    public static void openlibs(Class provider) {

        geoProvider = provider.getName();
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
}
