package cn.kkmofang.app;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.view.value.V;

public class PermissionsProtocol {

    public static final String STATUS_OK = "OK";
    public static final String STATUS_LOADING = "LOADING";
    public static final String STATUS_CANCEL = "CANCEL";

    public static final String LOCATION = "location";

    public enum PermissionCode{
        LOCATION("location", 1, "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"),
        CAMERA("camera", 2, "android.permission.CAMERA"),
        NONE("null", -1);

        PermissionCode(String name, int code, String... permissions) {
            this.code = code;
            this.pname = name;
            this.permissions = permissions;
        }

        private int code;
        private String pname;
        private String[] permissions;

        public static PermissionCode vOf(Object v){
            if (v instanceof String){
                switch ((String) v){
                    case "location":return LOCATION;
                    default:return NONE;
                }
            }

            if (v instanceof Integer){
                switch ((int)v){
                    case 1: return LOCATION;
                    default:return NONE;
                }
            }
            return NONE;

        }

        public int getCode() {
            return code;
        }

        public String getPname() {
            return pname;
        }

        public String[] getPermissions() {
            return permissions;
        }
    }


    public static void openlibs() {

        Protocol.main.addOpenApplication(new Protocol.OpenApplication() {
            @Override
            public void open(Application app) {

                final WeakReference<Application> a = new WeakReference<>(app);

                app.observer().on(new String[]{"permissions","get"}, new Listener<Application>() {

                    @Override
                    public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                        if(value != null && value instanceof Map){

                            Map<String,Object> data = (Map<String,Object>) value;

                            if(data.containsKey(LOCATION)) {

                                String[] permissions = PermissionCode.vOf(LOCATION).permissions;

                                boolean granted = false;
                                //check permissions
                                if (weakObject != null){
                                    for (String permission : permissions) {
                                        boolean b = ContextCompat.checkSelfPermission(weakObject.context(), permission) == PackageManager.PERMISSION_GRANTED;
                                        if (!b){
                                            granted = false;
                                            break;
                                        }
                                        granted = true;
                                    }
                                    data.put(LOCATION, granted?STATUS_OK:STATUS_LOADING);
                                    if (!granted && weakObject.shell() != null){
                                        ActivityCompat.requestPermissions(weakObject.shell().topActivity(), permissions, PermissionCode.vOf(LOCATION).code);
                                    }
                                }
                            }

                        }
                    }

                },app, Observer.PRIORITY_NORMAL,false);

            }
        });
    }
}
