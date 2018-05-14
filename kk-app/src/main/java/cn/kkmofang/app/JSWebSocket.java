package cn.kkmofang.app;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.duktape.*;
import cn.kkmofang.duktape.Context;
import cn.kkmofang.script.IScriptFunction;
import cn.kkmofang.script.IScriptObject;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.websocket.WebSocket;

/**
 * Created by zhanghailong on 2018/5/13.
 */

public class JSWebSocket implements IScriptObject,IRecycle {

    private final static IScriptFunction On = new IScriptFunction() {
        @Override
        public int call() {

            cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

            ctx.pushThis();

            JSWebSocket v = (JSWebSocket) ctx.toObject(-1);

            ctx.pop();

            if(v != null && v.webSocket() != null) {

                int top = ctx.getTop();

                String name = null ;

                if(top > 0 && ctx.isString(-top)) {
                    name = ctx.toString(-top);
                }

                if("open".equals(name)) {

                    if(top > 1 && ctx.isFunction(-top + 1) ) {
                        v._onopen = new Heapptr(ctx,ctx.getHeapptr(-top +1));
                    } else {
                        v._onopen = null;
                    }
                } else if("data".equals(name)) {

                    if(top > 1 && ctx.isFunction(-top + 1) ) {
                        v._ondata = new Heapptr(ctx,ctx.getHeapptr(-top +1));
                    } else {
                        v._ondata = null;
                    }
                } else if("close".equals(name)) {

                    if(top > 1 && ctx.isFunction(-top + 1) ) {
                        v._onclose = new Heapptr(ctx,ctx.getHeapptr(-top +1));
                    } else {
                        v._onclose = null;
                    }
                }

            }

            return 0;
        }
    };

    private final static IScriptFunction Close = new IScriptFunction() {
        @Override
        public int call() {

            cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

            ctx.pushThis();

            JSWebSocket v = (JSWebSocket) ctx.toObject(-1);

            ctx.pop();

            if(v != null && v.webSocket() != null) {
                v.webSocket().disconnect();
            }

            return 0;
        }
    };

    private final static IScriptFunction Send = new IScriptFunction() {
        @Override
        public int call() {

            cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

            ctx.pushThis();

            JSWebSocket v = (JSWebSocket) ctx.toObject(-1);

            ctx.pop();

            if(v != null && v.webSocket() != null) {

                int top = ctx.getTop();

                if(top > 0) {
                    if(ctx.isString(-top)) {
                        v.webSocket().send(ctx.toString(-top));
                    } else if(ctx.isBytes(-top)) {
                        v.webSocket().send(ctx.toBytes(-top));
                    }
                }

            }

            return 0;
        }
    };

    private final static String[] keys = new String[]{"on","close","send"};

    private Heapptr _onopen;
    private Heapptr _onclose;
    private Heapptr _ondata;

    private WebSocket _webSocket;
    private final OnCloseListener _cb;

    public WebSocket webSocket(){
        return _webSocket;
    }

    public JSWebSocket(String url,String protocol,OnCloseListener cb) {

        _cb =  cb;

        Map<String,String> headers = new TreeMap<>();

        if(protocol != null) {
            headers.put("Sec-WebSocket-Protocol",protocol);
        }

        final WeakReference<JSWebSocket> v = new WeakReference<>(this);

        _webSocket = new WebSocket(URI.create(url), new WebSocket.Listener() {
            @Override
            public void onConnect() {
                JSWebSocket vv = v.get();
                if(vv != null) {
                    vv.onOpen();
                }
            }

            @Override
            public void onMessage(String message) {
                JSWebSocket vv = v.get();
                if(vv != null) {
                    vv.onMessage(message);
                }
            }

            @Override
            public void onMessage(byte[] data) {
                JSWebSocket vv = v.get();
                if(vv != null) {
                    vv.onMessage(data);
                }
            }

            @Override
            public void onDisconnect(int code, String reason) {
                JSWebSocket vv = v.get();
                if(vv != null) {
                    vv.onClose(reason);
                }
            }

            @Override
            public void onError(Exception error) {

                JSWebSocket vv = v.get();

                if(vv != null) {
                    vv.onClose(error == null ? null : error.getLocalizedMessage());
                }
            }
        },headers);
    }

    protected void onOpen() {

        if (_onopen != null) {

            cn.kkmofang.duktape.Context ctx = _onopen.context();

            ScriptContext.pushContext(ctx);

            ctx.pushHeapptr(_onopen.heapptr());

            if(ctx.isFunction(-1)) {

                if(ctx.pcall(0) != Context.DUK_EXEC_SUCCESS) {
                    Log.d(cn.kkmofang.app.Context.TAG,ctx.getErrorString(-1));
                }

            }

            ctx.pop();

            ScriptContext.popContext();
        }
    }

    protected void onMessage(String text) {

        if (_ondata != null) {

            cn.kkmofang.duktape.Context ctx = _ondata.context();

            ScriptContext.pushContext(ctx);

            ctx.pushHeapptr(_ondata.heapptr());

            if(ctx.isFunction(-1)) {

                ctx.push(text);

                if(ctx.pcall(1) != Context.DUK_EXEC_SUCCESS) {
                    Log.d(cn.kkmofang.app.Context.TAG,ctx.getErrorString(-1));
                }

            }

            ctx.pop();

            ScriptContext.popContext();
        }

    }

    protected void onMessage(byte[] data) {

        if (_ondata != null) {

            cn.kkmofang.duktape.Context ctx = _ondata.context();

            ScriptContext.pushContext(ctx);

            ctx.pushHeapptr(_ondata.heapptr());

            if(ctx.isFunction(-1)) {

                ctx.push(data);

                if(ctx.pcall(1) != Context.DUK_EXEC_SUCCESS) {
                    Log.d(cn.kkmofang.app.Context.TAG,ctx.getErrorString(-1));
                }

            }

            ctx.pop();

            ScriptContext.popContext();
        }

    }

    protected void onClose(String errmsg) {

        if (_ondata != null) {

            cn.kkmofang.duktape.Context ctx = _ondata.context();

            ScriptContext.pushContext(ctx);

            ctx.pushHeapptr(_ondata.heapptr());

            if(ctx.isFunction(-1)) {

                ctx.push(errmsg);

                if(ctx.pcall(1) != Context.DUK_EXEC_SUCCESS) {
                    Log.d(cn.kkmofang.app.Context.TAG,ctx.getErrorString(-1));
                }

            }

            ctx.pop();

            ScriptContext.popContext();
        }

        if(_cb != null) {
            _cb.onClose(this,errmsg);
        }
    }

    @Override
    public String[] keys() {
        return keys;
    }

    @Override
    public Object get(String key) {
        if("on".equals(key)) {
            return On;
        }
        if("send".equals(key)) {
            return Send;
        }
        if("close".equals(key)) {
            return Close;
        }
        return null;
    }

    @Override
    public void set(String key, Object value) {


    }

    @Override
    protected void finalize() throws Throwable {
        if(_webSocket != null) {
            _webSocket.disconnect();
        }
        super.finalize();
    }

    @Override
    public void recycle() {

        if(_webSocket != null) {
            _webSocket.disconnect();
            _webSocket = null;
        }

        _onclose = null;
        _onopen = null;
        _ondata = null;

    }

    public static void push(cn.kkmofang.duktape.Context ctx,final RecycleContainer container) {

        ctx.pushObject();

        ctx.push("alloc");
        ctx.pushObject(new IScriptFunction(){
            @Override
            public int call() {

                cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

                int top = ctx.getTop();

                String url = null;
                String protocol = null;

                if(top > 0 && ctx.isString(-top)) {
                    url = ctx.toString(-top);
                }

                if(top > 1 && ctx.isString(-top +1)) {
                    protocol = ctx.toString(-top+1);
                }

                if(url != null ){

                    JSWebSocket v = new JSWebSocket(url, protocol, new OnCloseListener() {

                        @Override
                        public void onClose(JSWebSocket jsWebSocket, String errmsg) {
                            container.removeRecycle(jsWebSocket);
                            jsWebSocket.recycle();
                        }
                    });

                    v.webSocket().connect();

                    ctx.pushObject(v);

                    return 1;
                }

                return 0;
            }
        });
        ctx.putProp(-3);

    }

    public interface OnCloseListener {
        void  onClose(JSWebSocket jsWebSocket,String errmsg);
    }
}
