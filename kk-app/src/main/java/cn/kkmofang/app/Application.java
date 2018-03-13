package cn.kkmofang.app;

import android.os.Handler;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.IScriptFunction;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.Element;

/**
 * Created by hailong11 on 2018/3/12.
 */

public class Application {

    public final static double Kernel = 1.0;

    private static final ClassResource RES = new ClassResource(Application.class);

    private final IObserver _observer;
    private final Context _context;
    private final IResource _resource;
    private final Handler _handler;

    public Application(IResource resource) {
        _context = new Context();
        _observer = new Observer(_context);
        _resource = resource;
        _handler = new Handler();

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
            String code = RES.getString("js/require.js");
            execCode(code,null);
        }

        ScriptContext.popContext();
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

    public void execCode(String code,Map<String,Object> librarys) {
        ScriptContext.pushContext(context());

        if(librarys == null) {
            librarys = new TreeMap<String,Object>();
        }

        librarys.put("app",_observer);

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
    }

    public void exec(String name,Map<String,Object> librarys) {
        String code = _resource.getString(name);

        if(code != null) {
            execCode(code,librarys);
        }
    }

    public Element element(String path,IObserver data) {

        Element root = new Element();

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

        return root.lastChild();
    }

    public void post(final Runnable r) {

        final WeakReference<Application> app = new WeakReference<Application>(this);

        _handler.post(new Runnable() {
            @Override
            public void run() {
                Application v = app.get();
                if(v != null) {
                    ScriptContext.pushContext(v.context());
                    r.run();
                    ScriptContext.popContext();
                }
            }
        });
    }

}
