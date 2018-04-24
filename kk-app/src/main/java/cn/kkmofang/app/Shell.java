package cn.kkmofang.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.http.HttpOptions;
import cn.kkmofang.http.IHttp;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.IViewContext;
import cn.kkmofang.view.Tag;
import cn.kkmofang.view.value.Pixel;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public abstract class Shell {

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final IHttp _http;
    private final android.content.Context _context;

    private WeakReference<Activity> _rootActivity;

    public Shell(android.content.Context context,IHttp http) {
        _context = context;
        _http = http;
    }

    public android.content.Context context() {
        return _context;
    }

    public Activity rootActivity() {
        return _rootActivity != null ? _rootActivity.get() : null;
    }

    public void setRootActivity(Activity rootActivity) {
        if(rootActivity == null) {
            _rootActivity = null;
        } else {
            _rootActivity = new WeakReference<Activity>(rootActivity);
        }
        if(rootActivity != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            rootActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Pixel.UnitRPX = Math.min(metrics.widthPixels,metrics.heightPixels) / 750.0f;
            Pixel.UnitPX = metrics.density;
        }
    }

    public void open(String url) {
        open(url,false);
    }

    protected void setContent(File file,String content) {

        try {
            FileOutputStream out = new FileOutputStream(file);
            try {
                out.write(content.getBytes(UTF8));
            }
            finally {
                out.close();
            }
        } catch (java.io.IOException e) {
            Log.d(Context.TAG,Log.getStackTraceString(e));
        }

    }

    protected void setObject(File file,Object content) {
        setContent(file,_http.encodeJSON(content));
    }

    protected void load(final String url,final File path,final OnLoading fn,final List<Object> items,final int index,final Object appinfo,final Map<String,String> vers) {

        if(fn != null) {
            fn.onProgress(url,path,index,items.size());
        }

        String version = ScriptContext.stringValue(ScriptContext.get(appinfo,"version"),"_");

        if(index < items.size()) {

            Object item = items.get(index);
            String topath;
            File todir = new File(path,version);
            final File tofile;

            if(item instanceof String) {
                topath = (String) item;
                tofile = new File(todir,topath);
                if(tofile.exists()) {
                    load(url,path,fn,items,index + 1,appinfo,vers);
                    return;
                }
            } else {
                topath = ScriptContext.stringValue(ScriptContext.get(item,"path"),null);
                tofile = new File(todir,topath);
                String ver = ScriptContext.stringValue(ScriptContext.get(item,"ver"),null);
                if(vers == null || !vers.containsKey(topath) || vers.get(topath).equals(ver)) {
                    if(tofile.exists()) {
                        load(url,path,fn,items,index + 1,appinfo,vers);
                        return;
                    }
                }
            }

            URI u = URI.create(url);


            HttpOptions options = new HttpOptions();
            options.url = u.resolve(topath).toString();
            options.method = HttpOptions.METHOD_GET;
            options.type = HttpOptions.TYPE_URI;

            willHttpOptions(options);

            options.onfail = new HttpOptions.OnFail() {
                @Override
                public void on(Exception error, Object weakObject) {
                    if(fn != null) {
                        fn.onError(url,error);
                    }
                }
            };

            options.onload = new HttpOptions.OnLoad() {
                @Override
                public void on(Object data, Exception error, Object weakObject) {

                    if(error != null) {
                        if(fn != null) {
                            fn.onError(url,error);
                        }
                    } else {
                        File file = new File((String) data);
                        File dir = tofile.getParentFile();
                        if(!dir.exists()) {
                            dir.mkdirs();
                        }
                        file.renameTo(tofile);
                        if(weakObject != null) {
                            ((Shell) weakObject).load(url,path,fn,items,index + 1,appinfo,vers);
                        }
                    }
                }
            };

            _http.send(options,this);


        } else {
            if(!path.exists()) {
                path.mkdirs();
            }
            setObject(new File(path,"app.json"),appinfo);

            if(fn != null) {
                fn.onLoad(url,new File(path,version));
            }
        }
    }

    protected void load(final String url,final OnLoading fn) {

        String key = HttpOptions.cacheKey(url);
        final File path = new File(_context.getDir("kk",android.content.Context.MODE_PRIVATE),key);

        HttpOptions options = new HttpOptions();

        options.url = url;
        options.method = HttpOptions.METHOD_GET;
        options.type = HttpOptions.TYPE_JSON;

        Map<String,Object> data = new TreeMap<>();

        data.put("platform","android");
        data.put("kernel",Application.Kernel + "");

        willHttpOptions(options);

        options.onload = new HttpOptions.OnLoad() {
            @Override
            public void on(Object data, Exception error, Object weakObject) {

                if(error != null) {
                    if(fn != null) {
                        fn.onError(url,error);
                    }
                } else {

                    String version = ScriptContext.stringValue(ScriptContext.get(data,"version"),null);

                    if(version != null) {

                        Object items = ScriptContext.get(data,"items");

                        if(items instanceof List) {

                            Map<String,String> vers = null;

                            File info = new File(path,version + "/app.json");

                            if(info.exists()) {
                                vers = new TreeMap<>();
                                String text = FileResource.getString(info);
                                Object appinfo = _http.decodeJSON(text);
                                Object its = ScriptContext.get(appinfo,"items");
                                if(its != null && its instanceof List) {
                                    for(Object i : (List<Object>) its) {
                                        String p = ScriptContext.stringValue(ScriptContext.get(i,"path"),null);
                                        String ver = ScriptContext.stringValue(ScriptContext.get(i,"ver"),null);
                                        if(p != null && ver != null) {
                                            vers.put(p,ver);
                                        }
                                    }
                                }
                            }

                            if(weakObject != null) {
                                ((Shell) weakObject).load(url, path, fn, (List<Object>) items, 0, data, vers);
                            }

                        } else {
                            if(fn != null) {
                                fn.onError(url,new Exception("未找到应用包资源"));
                            }
                        }

                    } else {
                        if(fn != null) {
                            fn.onError(url,new Exception("未找到应用版本号"));
                        }
                    }
                }
            }
        };

        options.onfail = new HttpOptions.OnFail() {
            @Override
            public void on(Exception error, Object weakObject) {

                if(fn != null) {
                    fn.onError(url,error);
                }
            }
        };

        _http.send(options,this);

    }

    public void open(String url, boolean checkUpdate)  {

        if(url == null) {
            return;
        }

        if(url.startsWith("asset://")) {
            open(url,new AssetResource(_context.getAssets(),""),url.substring(8));
        } else if(url.startsWith("http://") || url.startsWith("https://")) {

            String key = HttpOptions.cacheKey(url);
            File path = new File(_context.getDir("kk",android.content.Context.MODE_PRIVATE),key);

            if(!checkUpdate) {

                File info = new File(path,"app.json");

                Object appinfo = null;

                if(info.exists()) {
                    String text = FileResource.getString(info);
                    appinfo = _http.decodeJSON(text);
                }

                String version = ScriptContext.stringValue(ScriptContext.get(appinfo,"version"),null);

                if(version != null) {

                    path = new File(path,version);
                    info = new File(path,"app.json");

                    if(info.exists()) {
                        open(url,new FileResource(null),path.getAbsolutePath());
                        load(url,null);
                        return;
                    }
                }

            }

            willLoading(url);

            final WeakReference<Shell> v = new WeakReference<>(this);

            load(url, new OnLoading() {

                @Override
                public void onLoad(String url, File path) {

                    Shell vv = v.get();

                    if(vv != null) {
                        vv.open(url,new FileResource(null),path.getAbsolutePath());
                        vv.didLoading(url,path);
                    }
                }

                @Override
                public void onProgress(String url, File path, int count, int totalCount) {

                    Shell vv = v.get();

                    if(vv != null) {
                        vv.didProgress(url,path,count,totalCount);
                    }
                }

                @Override
                public void onError(String url, Exception error) {

                    Shell vv = v.get();

                    if(vv != null) {
                        vv.didError(url,error);
                    }

                }
            });

        } else {
            open(url,new FileResource(null),url);
        }
    }

    public void update(String url) {

        if(url == null) {
            return;
        }

        if(url.startsWith("http://") || url.startsWith("https://")) {
            load(url,null);
        }
    }

    protected void openWindow(Application app, Controller controller,Object action) {
        WindowController v = new WindowController(rootActivity(),controller);
        v.show();
    }

    protected Class<?> openActivityClass() {
        return ActivityContainer.class;
    }

    protected void openActivity(Application app, Controller controller,Object action) {

        Activity root = rootActivity();

        if(root != null && action instanceof Serializable) {
            String target = ScriptContext.stringValue(ScriptContext.get(action,"target"),null);
            if(root instanceof Container
                    && (! ((Container) root).isOpened()|| "root".equals(target) )) {
                ((Container) root).open(app,action);
            } else {
                Intent i = new Intent(_context, openActivityClass());
                i.putExtra("appid", app.id());
                i.putExtra("action", (Serializable) action);
                root.startActivity(i);
            }
        }
    }

    protected void openAction(Application app, Object action) {

        String type = ScriptContext.stringValue(ScriptContext.get(action,"type"),null);

        if("window".equals(type)) {
            openWindow(app, app.open(action), action);
        } else if("app".equals(type)) {

            String url = ScriptContext.stringValue(ScriptContext.get(action,"url"),null);

            if(url != null) {
                open(url);
            }

        } else  {
            openActivity(app,app.open(action),action);
        }
    }

    public void openApplication(Application app) {

        final WeakReference<Shell> v = new WeakReference<>(this);

        app.observer().on(new String[]{"action", "open"}, new Listener<Application>() {
            @Override
            public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                if(weakObject != null && value != null && v.get() != null) {
                    v.get().openAction(weakObject,value);
                }
            }
        },app, Observer.PRIORITY_NORMAL,false);

        app.observer().on(new String[]{"alert"}, new Listener<Application>() {
            @Override
            public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                if(weakObject != null && value != null && v.get() != null && value instanceof String) {
                    new AlertDialog.Builder(v.get().rootActivity())
                            .setMessage((String) value)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create().show();
                }
            }
        },app, Observer.PRIORITY_NORMAL,false);

        app.run();
    }

    abstract protected IViewContext openViewContext(IResource resource, String path);

    protected void open(String url, IResource resource, String path){

        Application app = new Application(_context,new BasePathResource(resource,path),_http,openViewContext(resource,path));

        app.observer().set(new String[]{"path"},path);
        app.observer().set(new String[]{"url"},url);

        openApplication(app);

    }


    private static Shell _main;

    public final static void setMain(Shell main) {
        _main = main;
    }

    public final static Shell main() {
        return _main;
    }

    protected static interface OnLoading {

        void onLoad(String url,File path);

        void onProgress(String url,File path,int count, int totalCount);

        void onError(String url ,Exception error);

    }

    protected void willLoading(String url) {

    }

    protected void didLoading(String url,File path) {

    }

    protected void didProgress(String url,File path,int count,int totalCount) {

    }

    protected void didError(String url, Exception error){

    }

    protected void willHttpOptions(HttpOptions options) {

    }

}
