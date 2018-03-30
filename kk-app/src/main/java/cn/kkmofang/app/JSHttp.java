package cn.kkmofang.app;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Map;
import cn.kkmofang.duktape.*;
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
            Heapptr onload = null;
            Heapptr onfail = null;
            Heapptr onresponse = null;
            Heapptr onprocess = null;

            if(top >0 && ctx.isObject(-top)) {

                ctx.dup(-top);

                ctx.push("url");
                ctx.getProp(-2);;
                if(ctx.isString(-1)){
                    options.url = ctx.toString(-1);
                }
                ctx.pop();

                ctx.push("method");
                ctx.getProp(-2);;
                if(ctx.isString(-1)){
                    options.method = ctx.toString(-1);
                }
                ctx.pop();

                ctx.push("type");
                ctx.getProp(-2);;
                if(ctx.isString(-1)){
                    options.type = ctx.toString(-1);
                }
                ctx.pop();

                ctx.push("timeout");
                ctx.getProp(-2);;
                if(ctx.isNumber(-1)){
                    options.timeout = ctx.toInt(-1);
                }
                ctx.pop();

                ctx.push("data");
                ctx.getProp(-2);
                if(ctx.isString(-1)){
                    options.data = ctx.toString(-1);
                } else if(ctx.isObject(-1)) {
                    options.data = ctx.toValue(-1);
                }
                ctx.pop();

                ctx.push("headers");
                ctx.getProp(-2);
                if(ctx.isObject(-1)) {
                    Object v = ctx.toValue(-1);
                    if(v != null && v instanceof Map) {
                        options.headers = (Map<String,Object>) v;
                    }
                }
                ctx.pop();

                ctx.push("onload");
                ctx.getProp(-2);
                if(ctx.isFunction(-1)){
                    onload = new Heapptr(ctx,ctx.getHeapptr(-1));
                }
                ctx.pop();

                ctx.push("onfail");
                ctx.getProp(-2);
                if(ctx.isFunction(-1)){
                    onfail = new Heapptr(ctx,ctx.getHeapptr(-1));
                }
                ctx.pop();

                ctx.push("onresponse");
                ctx.getProp(-2);
                if(ctx.isFunction(-1)){
                    onresponse = new Heapptr(ctx,ctx.getHeapptr(-1));
                }
                ctx.pop();

                ctx.push("onprocess");
                ctx.getProp(-2);
                if(ctx.isFunction(-1)){
                    onprocess = new Heapptr(ctx,ctx.getHeapptr(-1));
                }
                ctx.pop();

                ctx.pop();

            }

            if(onload != null) {

                final Heapptr fn_onload = onload;

                options.onload = new HttpOptions.OnLoad() {
                    @Override
                    public void on(Object data, Exception error, Object weakObject) {
                        if(weakObject != null) {
                            cn.kkmofang.duktape.Context ctx = fn_onload.context();

                            ScriptContext.pushContext(ctx);

                            ctx.pushHeapptr(fn_onload.heapptr());

                            ctx.pushValue(data);

                            if(error != null) {
                                ctx.push(error.getMessage());
                            } else {
                                ctx.pushNull();
                            }

                            if(cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS != ctx.pcall(2)) {
                                Log.d(Context.TAG,ctx.getErrorString(-1));
                            }

                            ctx.pop();

                            ScriptContext.popContext();
                        }
                    }
                };
            }

            if(onfail != null) {

                final Heapptr fn_onfail = onfail;

                options.onfail = new HttpOptions.OnFail() {

                    @Override
                    public void on(Exception error, Object weakObject) {
                        if(weakObject != null) {
                            cn.kkmofang.duktape.Context ctx = fn_onfail.context();

                            ScriptContext.pushContext(ctx);

                            ctx.pushHeapptr(fn_onfail.heapptr());

                            if(error != null) {
                                ctx.push(error.getMessage());
                            } else {
                                ctx.pushNull();
                            }

                            if(cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS != ctx.pcall(1)) {
                                Log.d(Context.TAG,ctx.getErrorString(-1));
                            }

                            ctx.pop();

                            ScriptContext.popContext();
                        }
                    }
                };
            }

            if(onresponse != null) {

                final Heapptr fn_onresponse = onresponse;

                options.onresponse = new HttpOptions.OnResponse() {

                    @Override
                    public void on(int code,String status,Map<String,Object> headers,Object weakObject) {

                        if(weakObject != null) {

                            cn.kkmofang.duktape.Context ctx = fn_onresponse.context();

                            ScriptContext.pushContext(ctx);

                            ctx.pushHeapptr(fn_onresponse.heapptr());

                            ctx.push(code);
                            ctx.push(status);
                            ctx.pushValue(headers);

                            if(cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS != ctx.pcall(3)) {
                                Log.d(Context.TAG,ctx.getErrorString(-1));
                            }

                            ctx.pop();

                            ScriptContext.popContext();
                        }
                    }
                };
            }

            if(onprocess != null) {

                final Heapptr fn_onprocess = onprocess;

                options.onprocess = new HttpOptions.OnProcess() {

                    @Override
                    public void on(long value,long maxValue,Object weakObject) {

                        if(weakObject != null) {

                            cn.kkmofang.duktape.Context ctx = fn_onprocess.context();

                            ScriptContext.pushContext(ctx);

                            ctx.pushHeapptr(fn_onprocess.heapptr());

                            ctx.push(value);
                            ctx.push(maxValue);

                            if(cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS != ctx.pcall(2)) {
                                Log.d(Context.TAG,ctx.getErrorString(-1));
                            }

                            ctx.pop();

                            ScriptContext.popContext();
                        }
                    }
                };
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
