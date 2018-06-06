package cn.kkmofang.app;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by hailong11 on 2018/6/6.
 */

public class IO {

    private static HandlerThread _thread = null;
    private static Handler _handler;

    public static Handler getHandler() {
        if(_thread == null) {
            _thread = new HandlerThread("cn.kkmofang.app.IO");
            _thread.start();
            _handler = new Handler(_thread.getLooper());
        }
        return _handler;
    }

}
