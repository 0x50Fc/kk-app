package cn.kkmofang.app;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by zhanghailong on 2018/5/13.
 */

public class RecycleContainer implements IRecycle {

    private Set<IRecycle> _recycles;

    public void addRecycle(IRecycle recycle) {
        if(_recycles == null){
            _recycles = new TreeSet<>();
        }
        _recycles.add(recycle);
    }

    public void removeRecycle(IRecycle recycle) {
        if(_recycles != null) {
            _recycles.remove(recycle);
        }
    }

    @Override
    public void recycle() {

        if(_recycles != null) {
            for(IRecycle v : _recycles) {
                v.recycle();
            }
            _recycles = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        recycle();
        super.finalize();
    }
}
