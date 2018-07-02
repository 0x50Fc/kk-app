package cn.kkmofang.app;

public interface IGeoManager extends IRecycle {

    void startLocation(ILocationListener listener);

    void startLocation(long timeLimit, ILocationListener listener);

    boolean isLocationError();
}
