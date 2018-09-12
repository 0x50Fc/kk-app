package cn.kkmofang.app;

/**
 * Created by hailong11 on 2018/6/26.
 */

public interface ILooper {
    boolean post(Runnable runnable);
    boolean postDelayed(Runnable r, long delayMillis);
}
