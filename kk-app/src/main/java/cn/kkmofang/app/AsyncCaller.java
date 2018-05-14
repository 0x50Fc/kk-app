package cn.kkmofang.app;


import android.os.Handler;
import android.renderscript.Script;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;

import cn.kkmofang.duktape.*;
import cn.kkmofang.script.IScriptFunction;
import cn.kkmofang.script.ScriptContext;

/**
 * Created by zhanghailong on 2018/5/13.
 */

public class AsyncCaller implements IRecycle {

    public final IScriptFunction SetTimeoutFunc;
    public final IScriptFunction ClearTimeoutFunc;
    public final IScriptFunction SetIntervalFunc;
    public final IScriptFunction ClearIntervalFunc;

    public final Handler handler;
    private Map<Long,Task> _tasks;
    private long _id;

    public AsyncCaller() {

        _id = 0;
        _tasks = new TreeMap<>();
        handler = new Handler();

        final WeakReference<AsyncCaller> caller = new WeakReference<AsyncCaller>(this);

        SetTimeoutFunc = new IScriptFunction() {
            @Override
            public int call() {

                AsyncCaller v = caller.get();

                if(v == null) {
                    return 0;
                }

                cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

                int top = ctx.getTop();

                if(top >0 && ctx.isFunction(-top)) {

                    Heapptr fn = new Heapptr(ctx,ctx.getHeapptr(-top));
                    long tv = 0;

                    if(top > 1 && ctx.isNumber(-top + 1)) {
                        tv = ctx.toInt(-top + 1);
                    }

                    long id = v.newId();

                    Task task = new Task(id,v,fn,tv,false);

                    v.add(task);

                    ctx.push(String.valueOf(id));

                    return 1;
                }

                return 0;
            }
        };

        ClearTimeoutFunc = new IScriptFunction() {
            @Override
            public int call() {

                AsyncCaller v = caller.get();

                if(v == null) {
                    return 0;
                }

                cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

                int top = ctx.getTop();

                if(top >0 && ctx.isString(-top)) {

                    try {
                        long id = Long.valueOf(ctx.toString(-top));
                        v.cancel(id);
                    } catch (Throwable e) {}

                }

                return 0;
            }
        };

        SetIntervalFunc = new IScriptFunction() {
            @Override
            public int call() {

                AsyncCaller v = caller.get();

                if(v == null) {
                    return 0;
                }

                cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

                int top = ctx.getTop();

                if(top >0 && ctx.isFunction(-top)) {

                    Heapptr fn = new Heapptr(ctx,ctx.getHeapptr(-top));
                    long tv = 0;

                    if(top > 1 && ctx.isNumber(-top + 1)) {
                        tv = ctx.toInt(-top + 1);
                    }

                    long id = v.newId();

                    Task task = new Task(id,v,fn,tv,true);

                    v.add(task);

                    ctx.push(String.valueOf(id));

                    return 1;
                }

                return 0;
            }
        };

        ClearIntervalFunc = new IScriptFunction() {
            @Override
            public int call() {
                AsyncCaller v = caller.get();

                if(v == null) {
                    return 0;
                }

                cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

                int top = ctx.getTop();

                if(top >0 && ctx.isString(-top)) {

                    try {
                        long id = Long.valueOf(ctx.toString(-top));
                        v.cancel(id);
                    } catch (Throwable e) {}

                }

                return 0;
            }
        };

    }

    @Override
    protected void finalize() throws Throwable {
        recycle();
        super.finalize();
    }


    @Override
    public void recycle() {

        if(_tasks != null) {
            for(Task v : _tasks.values()) {
                v.cancel();
            }
            _tasks = null;
        }

    }

    private void add(Task task) {
        if(_tasks != null) {
            _tasks.put(task.id,task);
            handler.postDelayed(task,task.tv);
        }
    }

    private void remove(Task task ){

        if(_tasks != null) {
            _tasks.remove(task.id);
        }

    }

    private void cancel(long id){

        if(_tasks != null && _tasks.containsKey(id)) {
            Task task = _tasks.get(id);
            task.cancel();
            _tasks.remove(id);
        }

    }

    private long newId() {
        return ++ _id;
    }

    private static class Task implements Runnable {

        private final WeakReference<AsyncCaller> _caller;
        private boolean _canceled = false;
        private Heapptr _fn;
        public final long tv;
        private boolean _repeat;
        public final long id;

        public Task(long id,AsyncCaller caller,Heapptr fn,long tv,boolean repeat) {
            this.id=  id;
            _caller = new WeakReference<>(caller);
            _fn = fn;
            this.tv = tv;
            _repeat = repeat;
        }

        @Override
        public void run() {

            if(_canceled || _fn == null) {
                return;
            }

            cn.kkmofang.duktape.Context ctx = _fn.context();

            ScriptContext.pushContext(ctx);

            ctx.pushHeapptr(_fn.heapptr());

            if(ctx.isFunction(-1)) {

                if(cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS != ctx.pcall(0)) {
                    Log.d(Context.TAG,ctx.getErrorString(-1));
                }

            }

            ctx.pop();

            ScriptContext.popContext();

            if(_repeat && !_canceled && _fn != null) {
                AsyncCaller caller = _caller.get();
                if(caller != null) {
                    caller.handler.postDelayed(this,tv);
                    return;
                }
            }

            AsyncCaller caller = _caller.get();

            if(caller != null) {
                caller.remove(this);
            }

        }

        public void cancel() {
            _canceled = true;
            _fn = null;
        }

    }

}
