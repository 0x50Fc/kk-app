package cn.kkmofang.app;

import java.lang.ref.WeakReference;
import cn.kkmofang.http.HttpOptions;
import cn.kkmofang.http.IHttp;
import cn.kkmofang.http.IHttpTask;
import cn.kkmofang.script.IScriptFunction;
import cn.kkmofang.script.IScriptObject;
import cn.kkmofang.script.ScriptContext;

/**
 * Created by hailong11 on 2018/3/21.
 */

public class JSHttp implements IScriptObject{

    private final WeakReference<IHttp> _http;

    public JSHttp(IHttp http) {
        _http = new WeakReference<IHttp>(http);
    }

    public IHttp get() {
        return _http.get();
    }

    private final static String[] _keys = new String[]{"send"};

    private final static IScriptFunction Send = new IScriptFunction() {
        @Override
        public int call() {
            cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

            HttpOptions options = new HttpOptions();

            int top = ctx.getTop();

            if(top >0 && ctx.isObject(-top)) {


            }

            ctx.pushThis();

            JSHttp v = (JSHttp) ctx.toObject(-1);

            ctx.pop();

            if(v != null ) {

                IHttp http = v.get();

                if(http != null && options.url != null) {
                    IHttpTask task = http.send(options,v);
                    ctx.pushObject(new JSHttpTask(task));
                    return 1;
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
        if("send".equals(key)) {
            return Send;
        }
        return null;
    }

    @Override
    public void set(String key, Object value) {

    }

    @Override
    protected void finalize() throws Throwable {
        IHttp v = _http.get();
        if(v != null) {
            v.cancel(this);
        }
        super.finalize();
    }
}
