package cn.kkmofang.app;

/**
 * Created by hailong11 on 2018/4/9.
 */

public interface Container {
    boolean isOpened();
    void open(Application app, Object action);
}
