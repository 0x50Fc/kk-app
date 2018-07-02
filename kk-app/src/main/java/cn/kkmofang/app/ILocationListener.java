package cn.kkmofang.app;

import android.location.Location;

public interface ILocationListener {
    void onLocation(double lat, double lon);

    void onError(int errcode, String errmsg);
}
