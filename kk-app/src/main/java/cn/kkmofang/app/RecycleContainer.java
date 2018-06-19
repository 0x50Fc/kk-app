package cn.kkmofang.app;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by zhanghailong on 2018/5/13.
 */

public class RecycleContainer implements IRecycle {

    private Map<String,IRecycle> _recyclesWithKeys;
    private Set<IRecycle> _recycles;

    public void addRecycle(IRecycle recycle) {
        if(_recycles == null){
            _recycles = new HashSet<>();
        }
        _recycles.add(recycle);
    }

    public void setRecycle(IRecycle recycle,String key) {
        if(_recyclesWithKeys == null){
            _recyclesWithKeys = new TreeMap<>();
        }
        if(_recyclesWithKeys.containsKey(key)) {
            IRecycle v = _recyclesWithKeys.get(key);
            if(v != recycle) {
                v.recycle();
                _recyclesWithKeys.put(key,recycle);
            }
        }
    }

    public IRecycle getRecycle(String key) {
        if(_recyclesWithKeys != null && _recyclesWithKeys.containsKey(key)) {
            return _recyclesWithKeys.get(key);
        }
        return null;
    }

    public void removeRecycle(IRecycle recycle) {
        if(_recycles != null) {
            _recycles.remove(recycle);
        }
    }

    public void removeRecycle(String key) {
        if(_recyclesWithKeys != null && _recyclesWithKeys.containsKey(key)) {
            IRecycle v = _recyclesWithKeys.get(key);
            if(v != null) {
                v.recycle();
            }
            _recyclesWithKeys.remove(key);
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

        if(_recyclesWithKeys != null) {
            for(IRecycle v : _recyclesWithKeys.values()) {
                v.recycle();
            }
            _recyclesWithKeys = null;
        }
    }

}
