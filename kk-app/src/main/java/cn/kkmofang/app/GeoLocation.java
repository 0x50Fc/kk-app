package cn.kkmofang.app;

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

    public static void getLocation(IObserver observer,final String[] keys,final Map<String,Object> data) {

        final WeakReference<IObserver> obs = new WeakReference<IObserver>(observer);

        /*
        {
            IObserver v = obs.get();
            if(v != null) {
                data.put("lat",0);
                data.put("lng",0);
                v.set(keys,data);
            }
        }
        */

    }

    public static void openlibs() {


        Protocol.main.addOpenApplication(new Protocol.OpenApplication() {
            @Override
            public void open(Application app) {

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

                                getLocation(observer,sKeys,(Map<String,Object>) data);
                            }
                        }
                    }

                },app, Observer.PRIORITY_NORMAL,false);

            }
        });

    }
}
