package cn.kkmofang.app;

import android.util.Log;
import java.lang.ref.WeakReference;
import cn.kkmofang.duktape.*;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.IScriptFunction;
import cn.kkmofang.script.IScriptObject;
import cn.kkmofang.script.ScriptContext;

/**
 * Created by hailong11 on 2018/3/21.
 */

public class JSObserver implements IScriptObject {

    private final WeakReference<IObserver> _observer;

    public JSObserver(IObserver observer) {
        _observer = new WeakReference<IObserver>(observer);
    }

    public IObserver get() {
        return _observer.get();
    }

    private final static String[] _keys = new String[]{"get","set","on","off"};

    private final static IScriptFunction Get = new IScriptFunction() {
        @Override
        public int call() {

            cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

            String[] keys =null;

            int top = ctx.getTop();

            if(top >0 ) {
                if(ctx.isString(-top)) {
                    keys = ctx.toString(-top).split("\\.");
                }
                else if(ctx.isArray(-top)) {
                    int n = ctx.getLength(-top);
                    keys = new String[n];
                    for(int i=0;i<n;i++) {
                        ctx.push(i);
                        ctx.getProp(-top - 1);
                        keys[i] = ctx.toString(-1);
                        ctx.pop();
                    }
                }
            }

            if(keys == null) {
                return 0;
            }

            ctx.pushThis();

            JSObserver v = (JSObserver) ctx.toObject(-1);

            ctx.pop();

            if(v != null) {

                IObserver obs = v.get();

                if(obs != null) {
                    Object vv = obs.get(keys);
                    ctx.pushValue(vv);
                    return 1;
                }
            }

            return 0;
        }
    };

    private final static IScriptFunction Set = new IScriptFunction() {
        @Override
        public int call() {

            cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

            String[] keys =null;
            Object value = null;

            int top = ctx.getTop();

            if(top >0 ) {
                if(ctx.isString(-top)) {
                    keys = ctx.toString(-top).split("\\.");
                }
                else if(ctx.isArray(-top)) {
                    int n = ctx.getLength(-top);
                    keys = new String[n];
                    for(int i=0;i<n;i++) {
                        ctx.push(i);
                        ctx.getProp(-top - 1);
                        keys[i] = ctx.toString(-1);
                        ctx.pop();
                    }
                }
            }

            if(top > 1) {
                value = ctx.toValue(-top + 1);
            }

            if(keys == null) {
                return 0;
            }

            ctx.pushThis();

            JSObserver v = (JSObserver) ctx.toObject(-1);

            ctx.pop();

            if(v != null) {

                IObserver obs = v.get();

                if(obs != null) {
                    obs.set(keys,value);
                }
            }

            return 0;
        }
    };

    private final static IScriptFunction On = new IScriptFunction() {
        @Override
        public int call() {

            cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

            String[] keys =null;
            Heapptr heapptr = null;

            int top = ctx.getTop();

            if(top >0 ) {
                if(ctx.isString(-top)) {
                    keys = ctx.toString(-top).split("\\.");
                }
                else if(ctx.isArray(-top)) {
                    int n = ctx.getLength(-top);
                    keys = new String[n];
                    for(int i=0;i<n;i++) {
                        ctx.push(i);
                        ctx.getProp(-top - 1);
                        keys[i] = ctx.toString(-1);
                        ctx.pop();
                    }
                }
            }

            if(top > 1 && ctx.isFunction(-top + 1)) {
                heapptr = new Heapptr(ctx,ctx.getHeapptr(-top+1));
            }

            if(keys == null || heapptr == null) {
                return 0;
            }

            ctx.pushThis();

            JSObserver v = (JSObserver) ctx.toObject(-1);

            ctx.pop();

            if(v != null) {

                IObserver obs = v.get();

                if(obs != null) {

                    final Heapptr fn = heapptr;

                    obs.on(keys, new Listener<JSObserver>() {
                        @Override
                        public void onChanged(IObserver observer, String[] changedKeys, Object value, JSObserver weakObject) {

                            if(weakObject != null) {
                                cn.kkmofang.duktape.Context ctx = fn.context();
                                if(ctx != null) {
                                    cn.kkmofang.duktape.Context.pushContext(ctx);
                                    ctx.pushHeapptr(fn.heapptr());
                                    ctx.pushValue(value);
                                    ctx.pushValue(changedKeys);
                                    if(Context.DUK_EXEC_SUCCESS != ctx.pcall(2)) {
                                        Log.d(Context.TAG,ctx.getErrorString(-1));
                                    }
                                    ctx.pop();
                                    cn.kkmofang.duktape.Context.popContext();
                                }
                            }
                        }
                    },v, Observer.PRIORITY_NORMAL,false);
                }
            }

            return 0;
        }
    };

    private final static IScriptFunction Off = new IScriptFunction() {
        @Override
        public int call() {

            cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

            String[] keys = null;

            int top = ctx.getTop();

            if(top >0 ) {
                if(ctx.isString(-top)) {
                    keys = ctx.toString(-top).split("\\.");
                }
                else if(ctx.isArray(-top)) {
                    int n = ctx.getLength(-top);
                    keys = new String[n];
                    for(int i=0;i<n;i++) {
                        ctx.push(i);
                        ctx.getProp(-top - 1);
                        keys[i] = ctx.toString(-1);
                        ctx.pop();
                    }
                }
            }


            ctx.pushThis();

            JSObserver v = (JSObserver) ctx.toObject(-1);

            ctx.pop();

            if(v != null) {

                IObserver obs = v.get();

                if(obs != null) {
                    obs.off(keys,null,v);
                }
            }

            return 0;
        }
    };

    @Override
    public String[] keys() {
        return _keys;
    }

    @Override
    public Object get(String key) {
        if("get".equals(key)) {
            return Get;
        }
        if("set".equals(key)) {
            return Set;
        }
        if("on".equals(key)) {
            return On;
        }
        if("off".equals(key)) {
            return Off;
        }
        return null;
    }

    @Override
    public void set(String key, Object value) {

    }

    @Override
    protected void finalize() throws Throwable {
        IObserver v = _observer.get();
        if(v != null) {
            v.off(null,null,this);
        }
        super.finalize();
    }
}
