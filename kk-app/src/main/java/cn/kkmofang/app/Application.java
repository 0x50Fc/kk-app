package cn.kkmofang.app;

import android.app.Activity;
import android.os.Handler;
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

/**
 * Created by zhanghailong on 2018/3/12.
 */

public class Application {

    public final static double Kernel = 1.0;

    private final IObserver _observer;
    private final Context _context;
    private final IResource _resource;
    private final Activity _activity;
    private final IHttp _http;
    private final IViewContext _viewContext;

    public Application(Activity activity,IResource resource,IHttp http,IViewContext viewContext) {
        _context = new Context();
        _observer = new Observer(_context);
        _resource = resource;
        _activity = activity;
        _http = http;
        _viewContext = viewContext;

        final WeakReference<Application> app = new WeakReference<Application>(this);

        ScriptContext.pushContext(_context);

        _context.pushGlobalObject();

        _context.push("kk");
        _context.pushObject();

        _context.push("kernel");
        _context.push(Kernel);
        _context.putProp(-3);

        _context.push("platform");
        _context.push("android");
        _context.putProp(-3);

        _context.push("getString");
        _context.pushFunction(new IScriptFunction() {
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
        _context.putProp(-3);


        _context.putProp(-3);


        _context.push("View");
        _context.pushFunction(View.Func);
        _context.putProp(-3);

        _context.pop();

        {
            String code = null;
            InputStream in = _activity.getResources().openRawResource(R.raw.require);
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
                execCode(code, null);
            }
        }

        ScriptContext.popContext();

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


    public Context context() {
        return _context;
    }

    public IObserver observer() {
        return _observer;
    }

    public IResource resource() {
        return _resource;
    }

    public Activity activity() {
        return _activity;
    }

    public IViewContext viewContext() {
        return _viewContext;
    }

    public void execCode(String code,Map<String,Object> librarys) {
        ViewContext.push(_viewContext);
        ScriptContext.pushContext(context());

        if(librarys == null) {
            librarys = new TreeMap<String,Object>();
        }

        librarys.put("app",new JSObserver(_observer));

        if(_http != null) {
            librarys.put("http",new JSHttp(_http));
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

        _context.eval(execCode.toString());

        if(_context.isFunction(-1)) {

            for(Object v : arguments) {
                _context.pushValue(v);
            }

            if(_context.pcall(arguments.size()) != cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS) {
                String v = _context.getErrorString(-1);
                Log.d(Context.TAG,v);
            }

            _context.pop();

        } else {
            _context.pop();
        }


        ScriptContext.popContext();
        ViewContext.pop();
    }

    public void exec(String name,Map<String,Object> librarys) {
        String code = _resource.getString(name);

        if(code != null) {
            execCode(code,librarys);
        }
    }

    public void run() {
        exec("main.js",null);
    }

    public Element element(String path,IObserver data) {

        Element root = new Element();

        ViewContext.push(_viewContext);

        String code = _resource.getString(path);

        if(code != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("(function(element,data){").append(code).append("})");
            _context.eval(sb.toString());
            if(_context.isFunction(-1)) {

                _context.pushValue(root);
                _context.pushValue(data);

                if(_context.pcall(2) != cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS) {
                    Log.d(Context.TAG,_context.getErrorString(-1));
                }

                _context.pop();

            } else {
                _context.pop();
            }
        }

        ViewContext.pop();

        return root.lastChild();
    }

    public void post(final Runnable r) {

        final WeakReference<Application> app = new WeakReference<Application>(this);

        _context.post(new Runnable() {
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


}
