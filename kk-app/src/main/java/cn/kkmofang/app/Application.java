package cn.kkmofang.app;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.http.IHttp;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.IScriptFunction;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.Element;
import cn.kkmofang.view.IViewContext;
import cn.kkmofang.view.ViewContext;
import cn.kkmofang.unity.R;

/**
 * Created by zhanghailong on 2018/3/12.
 */

public class Application extends RecycleContainer {

    public final static double Kernel = 1.0;
    private static long _autoId = 0;

    private final long _id;
    private final IObserver _observer;
    private final JSObserver _jsObserver;
    private final Context _jsContext;
    private final IResource _resource;
    private final android.content.Context _context;
    private IHttp _http;
    private final IViewContext _viewContext;
    private final AsyncCaller _caller;
    private final JSWebSocket _jsWebSocket;
    private JSHttp _jsHttp;

    public Application(android.content.Context context,IResource resource,IViewContext viewContext) {
        _jsContext = new Context();
        _observer = new Observer(_jsContext);
        _jsObserver = new JSObserver(_observer);
        _caller = new AsyncCaller();
        _jsWebSocket = new JSWebSocket();
        _resource = resource;
        _context = context;
        _viewContext = viewContext;
        _id = ++ _autoId;

        _applications.put(_id,new WeakReference<>(this));

        final WeakReference<Application> app = new WeakReference<Application>(this);

        ScriptContext.pushContext(_jsContext);

        _jsContext.pushGlobalObject();

        _jsContext.push("kk");
        _jsContext.pushObject();

        _jsContext.push("kernel");
        _jsContext.push(Kernel);
        _jsContext.putProp(-3);

        _jsContext.push("platform");
        _jsContext.push("android");
        _jsContext.putProp(-3);

        _jsContext.push("getString");
        _jsContext.pushFunction(new IScriptFunction() {
            @Override
            public int call() {

                Application v = app.get();

                if(v != null) {

                    Context ctx = (Context) ScriptContext.currentContext();

                    int top = ctx.getTop();

                    if(top > 0 && ctx.isString(-top)) {

                        String name = ctx.toString(-top);

                        String vv = v.resource().getString(name);

                        ctx.push(vv);

                        return 1;
                    }

                }


                return 0;
            }
        });
        _jsContext.putProp(-3);

        _jsContext.push("compile");
        _jsContext.pushFunction(new IScriptFunction() {
            @Override
            public int call() {

                Application v = app.get();

                if(v != null) {

                    Context ctx = (Context) ScriptContext.currentContext();

                    int top = ctx.getTop();

                    if(top > 0 && ctx.isString(-top)) {

                        String name = ctx.toString(-top);

                        String vv = v.resource().getString(name);

                        StringBuffer code = new StringBuffer();

                        if(top > 1 && ctx.isString(-top + 1)) {
                            code.append(ctx.toString(-top  +1));
                        }

                        if(vv != null) {
                            code.append(vv);
                        }

                        if(top > 2 && ctx.isString(-top + 2)) {
                            code.append(ctx.toString(-top  +2));
                        }

                        ctx.compile(code.toString(),name);

                        return 1;
                    }

                }


                return 0;
            }
        });
        _jsContext.putProp(-3);


        _jsContext.putProp(-3);


        _jsContext.push("View");
        _jsContext.pushFunction(View.Func);
        _jsContext.putProp(-3);

        _jsContext.push("app");
        _jsContext.pushObject(_jsObserver);
        _jsContext.putProp(-3);

        _jsContext.push("setTimeout");
        _jsContext.pushFunction(_caller.SetTimeoutFunc);
        _jsContext.putProp(-3);

        _jsContext.push("clearTimeout");
        _jsContext.pushFunction(_caller.ClearTimeoutFunc);
        _jsContext.putProp(-3);

        _jsContext.push("setInterval");
        _jsContext.pushFunction(_caller.SetIntervalFunc);
        _jsContext.putProp(-3);

        _jsContext.push("clearInterval");
        _jsContext.pushFunction(_caller.ClearIntervalFunc);
        _jsContext.putProp(-3);

        _jsContext.push("WebSocket");
        _jsContext.pushObject(_jsWebSocket);
        _jsContext.putProp(-3);

        _jsContext.push("http");
        _jsContext.pushObject(jsHttp());
        _jsContext.putProp(-3);


        _jsContext.pop();

        {
            String code = null;
            InputStream in = _context.getResources().openRawResource(R.raw.require);
            try {
                code = FileResource.getString(in);
            }
            finally {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.d(Context.TAG,Log.getStackTraceString(e));
                }
            }
            if(code != null) {
                execCode(code, "kk.require.js",null);
            }
        }

        ScriptContext.popContext();

    }


    public long id() {
        return _id;
    }

    public Controller open(Object action) {

        Class<?> clazz = null;

        {
            String v = ScriptContext.stringValue(ScriptContext.get(action,"class"),null);

            if(v != null) {
                try {
                    clazz = Class.forName(v);
                } catch (ClassNotFoundException e) {
                    Log.d(Context.TAG,Log.getStackTraceString(e));
                }
            }
        }

        if(clazz == null) {

            String v = ScriptContext.stringValue(ScriptContext.get(action,"path"),null);

            if(v != null) {
                clazz = ViewController.class;
            }
        }

        if(clazz == null) {
            clazz = Controller.class;
        }

        Controller v = null;

        try {
            v = (Controller) clazz.newInstance();
        } catch (InstantiationException e) {
            Log.d(Context.TAG,Log.getStackTraceString(e));
        } catch (IllegalAccessException e) {
            Log.d(Context.TAG,Log.getStackTraceString(e));
        }

        if(v == null) {
            v = new Controller();
        }

        v.setApplication(this);
        v.setAction(action);

        return v;
    }


    public Context jsContext() {
        return _jsContext;
    }

    public IObserver observer() {
        return _observer;
    }

    public JSObserver jsObserver() {
        return _jsObserver;
    }

    public IResource resource() {
        return _resource;
    }

    public android.content.Context context() {
        return _context;
    }

    public IViewContext viewContext() {
        return _viewContext;
    }

    public IHttp http() {
        return _http;
    }

    public void setHttp(IHttp http) {
        _http = http;
    }

    public JSHttp jsHttp() {
        if(_jsHttp == null) {
            IHttp v = http();
            if(v != null) {
                _jsHttp = new JSHttp(v);
            }
        }
        return _jsHttp;
    }

    public void execCode(String code,String name,Map<String,Object> librarys) {
        ViewContext.push(_viewContext);
        ScriptContext.pushContext(_jsContext);

        if(librarys == null) {
            librarys = new TreeMap<>();
        }

        if(!librarys.containsKey("app")) {
            librarys.put("app",_jsObserver);
        }

        if(!librarys.containsKey("setTimeout")) {
            librarys.put("setTimeout",_caller.SetTimeoutFunc);
        }

        if(!librarys.containsKey("clearTimeout")) {
            librarys.put("clearTimeout",_caller.ClearTimeoutFunc);
        }

        if(!librarys.containsKey("setInterval")) {
            librarys.put("setInterval",_caller.SetIntervalFunc);
        }

        if(!librarys.containsKey("clearInterval")) {
            librarys.put("clearInterval",_caller.ClearIntervalFunc);
        }

        if(!librarys.containsKey("WebSocket")) {
            librarys.put("WebSocket",_jsWebSocket);
        }

        if(!librarys.containsKey("http")) {
            librarys.put("http",jsHttp());
        }

        List<Object> arguments = new ArrayList<>();

        StringBuilder execCode = new StringBuilder();

        execCode.append("(function(");

        for(String key : librarys.keySet()) {
            if(arguments.size() != 0) {
                execCode.append(",");
            }
            execCode.append(key);
            arguments.add(librarys.get(key));
        }

        execCode.append("){").append(code).append("})");

        _jsContext.compile(execCode.toString(),name);

        if(_jsContext.isFunction(-1)) {

            if(_jsContext.pcall(0) != cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS) {

                String v = _jsContext.getErrorString(-1);
                Log.d(Context.TAG,v);

            } else if(_jsContext.isFunction(-1)){

                for (Object v : arguments) {
                    _jsContext.pushValue(v);
                }

                if (_jsContext.pcall(arguments.size()) != cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS) {
                    String v = _jsContext.getErrorString(-1);
                    Log.d(Context.TAG, v);
                }

            }

            _jsContext.pop();

        } else {
            _jsContext.pop();
        }


        ScriptContext.popContext();
        ViewContext.pop();
    }

    public void exec(String name,Map<String,Object> librarys) {
        String code = _resource.getString(name);

        if(code != null) {
            execCode(code,name,librarys);
        }
    }

    public void run() {
        exec("main.js",null);
    }

    public Element element(String path,IObserver data) {

        Element root = new Element();

        ViewContext.push(_viewContext);
        ScriptContext.pushContext(_jsContext);

        String code = _resource.getString(path);

        if(code != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("(function(element,data){").append(code).append("})");
            _jsContext.eval(sb.toString());
            if(_jsContext.isFunction(-1)) {

                _jsContext.pushValue(root);
                _jsContext.pushValue(data);

                if(_jsContext.pcall(2) != cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS) {
                    Log.d(Context.TAG,_jsContext.getErrorString(-1));
                }

                _jsContext.pop();

            } else {
                _jsContext.pop();
            }
        }

        ScriptContext.popContext();
        ViewContext.pop();

        return root.lastChild();
    }

    public void post(final Runnable r) {

        final WeakReference<Application> app = new WeakReference<Application>(this);

        _jsContext.post(new Runnable() {
            @Override
            public void run() {
                Application v = app.get();
                if(v != null) {
                    ViewContext.push(v.viewContext());
                    r.run();
                    ViewContext.pop();
                }
            }
        });
    }

    private WeakReference<Shell> _shell;

    public Shell shell() {
        if(_shell != null) {
            return _shell.get();
        }
        return null;
    }

    public void setShell(Shell shell) {
        if(shell == null) {
            _shell = null;
        } else {
            _shell = new WeakReference<>(shell);
        }
    }

    public void recycle() {
        _jsWebSocket.recycle();
        _caller.recycle();
        _jsObserver.recycle();
        if(_jsHttp != null) {
            _jsHttp.cancel();
        }
        super.recycle();
    }


    private final static Map<Long,WeakReference<Application>> _applications = new TreeMap<>();

    public final static Application get(long id) {
        if(_applications.containsKey(id)) {
            WeakReference<Application> v = _applications.get(id);
            Application app = v.get();
            if(app == null) {
                _applications.remove(id);
            }
            return app;
        }
        return null;
    }
}
