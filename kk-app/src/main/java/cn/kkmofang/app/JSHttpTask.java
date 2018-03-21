package cn.kkmofang.app;

import java.lang.ref.WeakReference;

import cn.kkmofang.http.HttpOptions;
import cn.kkmofang.http.IHttp;
import cn.kkmofang.http.IHttpTask;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.script.IScriptFunction;
import cn.kkmofang.script.IScriptObject;
import cn.kkmofang.script.ScriptContext;

/**
 * Created by hailong11 on 2018/3/21.
 */

public class JSHttpTask implements IScriptObject {

    private final IHttpTask _httpTask;

    public JSHttpTask(IHttpTask httpTask) {
        _httpTask = httpTask;
    }

    public IHttpTask get() {
        return _httpTask;
    }

    private final static String[] _keys = new String[]{"cancel"};

    private final static IScriptFunction Cancel = new IScriptFunction() {
        @Override
        public int call() {
            cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

            ctx.pushThis();

            JSHttpTask v = (JSHttpTask) ctx.toObject(-1);

            ctx.pop();

            if(v != null ) {
               v.get().cancel();
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
        if("cancel".equals(key)) {
            return Cancel;
        }
        return null;
    }

    @Override
    public void set(String key, Object value) {

    }
}
