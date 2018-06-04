package cn.kkmofang.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.http.HttpOptions;
import cn.kkmofang.http.IHttp;
import cn.kkmofang.http.IHttpTask;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.IViewContext;
import cn.kkmofang.view.value.Pixel;
import cn.kkmofang.view.value.V;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public abstract class Shell {

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final Protocol _protocol;
    private final IHttp _http;
    private final android.content.Context _context;
    private final Map<String,AppLoading> _loadings;
    private WeakReference<Activity> _rootActivity;

    public Shell(android.content.Context context,IHttp http,Protocol protocol) {
        _context = context;
        _http = http;
        _protocol = protocol;
        _loadings = new TreeMap<>();
    }

    public Shell(android.content.Context context,IHttp http){
        this(context,http,Protocol.main);
    }

    public IHttp http() {
        return _http;
    }

    public android.content.Context context() {
        return _context;
    }

    private WeakReference<Application> _rootApplication = null;

    private List<WeakReference<Activity>> _activitys = new LinkedList<>();

    public Activity topActivity() {
        int i= _activitys.size() - 1;
        while(i >= 0) {
            Activity v = _activitys.get(i).get();
            if(v == null || v.isFinishing()) {
                _activitys.remove(i);
                i --;
                continue;
            }
            return v;
        }
        return null;
    }
    public Application rootApplication() {
        if(_rootApplication != null) {
            return _rootApplication.get();
        }
        return null;
    }

    public Activity rootActivity() {
        if(_rootActivity == null || _rootActivity.get() == null) {

            int i= 0;

            while(i < _activitys.size()) {
                Activity v = _activitys.get(i).get();
                if(v == null || v.isFinishing()) {
                    _activitys.remove(i);
                    continue;
                }
                _rootActivity = new WeakReference<>(v);
                return _rootActivity.get();
            }

            return null;
        }

        return _rootActivity != null ? _rootActivity.get() : null;
    }

    public void setRootActivity(Activity rootActivity) {
        _activitys.clear();
        if(rootActivity == null) {
            _rootActivity = null;
        } else {
            _rootActivity = new WeakReference<>(rootActivity);
            _activitys.add(_rootActivity);
        }
        if(rootActivity != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            rootActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Pixel.UnitRPX = Math.min(metrics.widthPixels,metrics.heightPixels) / 750.0f;
            Pixel.UnitPX = metrics.density;
            Pixel.UnitVH = metrics.heightPixels * 0.01f;
            Pixel.UnitVW = metrics.widthPixels * 0.01f;
        }
    }

    public void removeActivity(Activity activity) {
        int i=0;
        while(i < _activitys.size()) {
            Activity v = _activitys.get(i).get();
            if(v == null || v == activity) {
                _activitys.remove(i);
                continue;
            }
            i ++;
        }
    }

    public void addActivity(Activity activity) {
        _activitys.add(new WeakReference<>(activity));
    }

    public void popActivity(int idx,int n) {

        int i = _activitys.size() - 1 - idx;

        while(n > 0 && i > 0) {
            Activity v = _activitys.get(i).get();
            if(v == null || v.isFinishing()) {
                _activitys.remove(i);
                i --;
            } else {
                v.finish();
                _activitys.remove(i);
                i --;
                n --;
            }
        }
    }

    public void open(String url,Object query) {
        open(url,query,false);
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

    protected void load(final String url,final File path,final OnLoading fn,final List<Object> items,final int index,final Object appinfo,final Map<String,String> vers,final String key) {

        final Map<String,AppLoading> loadings = _loadings;

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
                    load(url,path,fn,items,index + 1,appinfo,vers,key);
                    return;
                }
            } else {
                topath = ScriptContext.stringValue(ScriptContext.get(item,"path"),null);
                tofile = new File(todir,topath);
                String ver = ScriptContext.stringValue(ScriptContext.get(item,"ver"),null);
                if(vers == null || !vers.containsKey(topath) || vers.get(topath).equals(ver)) {
                    if(tofile.exists()) {
                        load(url,path,fn,items,index + 1,appinfo,vers,key);
                        return;
                    }
                }
            }

            URI u = URI.create(url);


            final String itemURL = u.resolve(topath).toString();

            HttpOptions options = new HttpOptions();
            options.url = itemURL;
            options.method = HttpOptions.METHOD_GET;
            options.type = HttpOptions.TYPE_URI;

            willHttpOptions(options);

            options.onfail = new HttpOptions.OnFail() {
                @Override
                public void on(Exception error, Object weakObject) {
                    loadings.remove(key);
                    if(fn != null) {
                        fn.onError(url,error);
                    }
                }
            };

            options.onload = new HttpOptions.OnLoad() {

                public void on(Object data, Exception error, Object weakObject) {

                    if(error != null) {
                        loadings.remove(key);
                        if (fn != null) {
                            fn.onError(url, error);
                        }
                    } else if(data == null || !( data instanceof  String)) {
                        loadings.remove(key);
                        if (fn != null) {
                            fn.onError(url, new Exception("下载出错了: " + itemURL));
                        }
                    } else {
                        File file = new File((String) data);
                        File dir = tofile.getParentFile();
                        if(!dir.exists()) {
                            dir.mkdirs();
                        }
                        file.renameTo(tofile);
                        if(weakObject != null) {
                            ((Shell) weakObject).load(url,path,fn,items,index + 1,appinfo,vers,key);
                        }
                    }
                }
            };

            _http.send(options,this);


        } else {

            AppLoading loading = loadings.remove(key);

            if(!path.exists()) {
                path.mkdirs();
            }

            setObject(new File(path,"app.json"),appinfo);

            if(fn != null) {
                fn.onLoad(url,new File(path,version),loading);
            }
        }
    }

    protected void load(final String url,final OnLoading fn) {

        final String key = HttpOptions.cacheKey(url);
        final Map<String,AppLoading> loadings = _loadings;

        if(loadings.containsKey(key)) {
            AppLoading loading = loadings.get(key);
            loading.canceled = false;
            return;
        }

        setLoading(key,url);

        final File path = new File(_context.getDir("kk",android.content.Context.MODE_PRIVATE),key);

        HttpOptions options = new HttpOptions();

        options.url = url;
        options.method = HttpOptions.METHOD_GET;
        options.type = HttpOptions.TYPE_JSON;
        options.timeout = 10;

        Map<String,Object> data = new TreeMap<>();

        data.put("platform","android");
        data.put("kernel",Application.Kernel + "");

        willHttpOptions(options);

        options.onload = new HttpOptions.OnLoad() {
            @Override
            public void on(Object data, Exception error, Object weakObject) {

                if(error != null) {
                    loadings.remove(key);
                    if(fn != null) {
                        fn.onError(url, error);
                    }
                } else {

                    String version = ScriptContext.stringValue(ScriptContext.get(data,"version"),null);

                    if(version != null) {

                        Object items = ScriptContext.get(data,"res");

                        if(items == null) {
                            items = ScriptContext.get(data,"items");
                        }

                        if(items instanceof List) {

                            Map<String,String> vers = null;

                            File info = new File(path,version + "/app.json");

                            if(info.exists()) {
                                vers = new TreeMap<>();
                                String text = FileResource.getString(info);
                                Object appinfo = _http.decodeJSON(text);
                                Object its = ScriptContext.get(appinfo,"res");
                                if(its == null) {
                                    its = ScriptContext.get(appinfo,"items");
                                }
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
                                ((Shell) weakObject).load(url, path, fn, (List<Object>) items, 0, data, vers,key);
                            }

                        } else {
                            loadings.remove(key);
                            if(fn != null) {
                                fn.onError(url,new Exception("未找到应用包资源"));
                            }
                        }

                    } else {
                        loadings.remove(key);
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

                loadings.remove(key);

                if(fn != null) {
                    fn.onError(url,error);
                }

            }
        };

        Log.d("kk",options.url);

        _http.send(options,this);

    }

    public void open(String url,final Object query, boolean checkUpdate)  {

        if(url == null) {
            return;
        }

        if(url.startsWith("asset://")) {
            open(url,query,new AssetResource(_context.getAssets(),""),url.substring(8),null);
        } else if(url.startsWith("http://") || url.startsWith("https://")) {

            final String key = HttpOptions.cacheKey(url);
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
                        open(url,query,new FileResource(null),path.getAbsolutePath(),key);
                        load(url,null);
                        return;
                    }
                }

            }

            willLoading(url);

            final WeakReference<Shell> v = new WeakReference<>(this);

            load(url, new OnLoading() {

                @Override
                public void onLoad(String url, File path,AppLoading loading) {

                    Shell vv = v.get();

                    if(vv != null) {
                        if(loading == null || !loading.canceled) {
                            vv.open(url, query, new FileResource(null), path.getAbsolutePath(), key);
                        }
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
            open(url,query,new FileResource(null),url,null);
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

    public AppLoading isLoading(String url) {
        if(url.startsWith("http://") || url.startsWith("https://")) {
            String key = HttpOptions.cacheKey(url);
            if(_loadings.containsKey(key)) {
                return _loadings.get(key);
            }
        }
        return null;
    }

    protected AppLoading setLoading(String key,String url ){
        if(_loadings.containsKey(key)) {
            return _loadings.get(key);
        }
        AppLoading loading = new AppLoading();
        loading.url = url;
        _loadings.put(key,loading);
        return loading;
    }

    protected AppLoading cancelLoading(String key) {
        if(_loadings.containsKey(key)) {
            return _loadings.remove(key);
        }
        return null;
    }

    public boolean has(String url) {
        if(url.startsWith("http://") || url.startsWith("https://")) {
            String key = HttpOptions.cacheKey(url);
            File path = new File(_context.getDir("kk",android.content.Context.MODE_PRIVATE),key);
            File info = new File(path,"app.json");
            return info.exists();
        }
        return true;
    }

    protected void openWindow(Application app, Object action) {
        Activity topActivity = topActivity();
        if(topActivity != null) {
            WindowController v = new WindowController(topActivity,app.open(action));
            v.show();
        }
    }

    protected Class<?> openActivityClass() {
        return ActivityContainer.class;
    }


    protected void openActivity(Application app, Object action) {
        openActivity(app,action,openActivityClass());
    }

    protected void openActivity(Application app, Object action,Class<?> activityClass) {

        Activity root = rootActivity();

        if(root != null && action instanceof Serializable) {
            String target = ScriptContext.stringValue(ScriptContext.get(action,"target"),null);
            if(root instanceof Container
                    && (! ((Container) root).isOpened()|| "root".equals(target) )) {
                ((Container) root).open(app,action);
            } else {
                Intent i = new Intent(_context, activityClass);
                i.putExtra("appid", app.id());
                i.putExtra("action", (Serializable) action);
                root.startActivity(i);
            }
        }
    }

    protected void openURL(Application app, Object action,String url) {

    }

    protected void openScheme(Application app, Object action,String scheme) {

    }

    protected void openAction(Application app, Object action) {

        int count = _activitys.size();

        String type = ScriptContext.stringValue(ScriptContext.get(action,"type"),null);
        String url = ScriptContext.stringValue(ScriptContext.get(action,"url"),null);
        String scheme = ScriptContext.stringValue(ScriptContext.get(action,"scheme"),null);
        String path = ScriptContext.stringValue(ScriptContext.get(action,"path"),null);

        if("window".equals(type)) {
            openWindow(app, action);
        } else if("app".equals(type)) {
            if(url != null) {
                open(url,ScriptContext.get(action,"query"));
            }
        } else if(path != null){
            openActivity(app, action);
        } else if(scheme != null) {
            if(scheme.startsWith("http://") || scheme.startsWith("https://")) {
                openURL(app,action,scheme);
            } else {
                openScheme(app, action, scheme);
            }
        } else if(url != null) {
            openURL(app,action,url);
        }

        int idx = _activitys.size() - count;

        String back = ScriptContext.stringValue(ScriptContext.get(action,"back"),null);

        if(back != null) {
            int n = 0;
            String[] vs = back.split("/");
            for(String v : vs) {
                if(v.equals("..")) {
                    n ++;
                }
            }
            popActivity(idx,n);
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

        app.observer().on(new String[]{"action", "update"}, new Listener<Application>() {
            @Override
            public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {

                Shell shell = v.get();

                if(weakObject != null && value != null && shell != null && value instanceof Map) {

                    String url = ScriptContext.stringValue(ScriptContext.get(value,"url"),null);

                    if(url != null) {

                        boolean checkUpdate = ScriptContext.booleanValue(ScriptContext.get(value,"checkUpdate"),false);

                        if(checkUpdate || !shell.has(url)) {
                            shell.update(url);
                        }
                    }

                }
            }
        },app, Observer.PRIORITY_NORMAL,false);

        app.observer().on(new String[]{"alert"}, new Listener<Application>() {
            @Override
            public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                if(weakObject != null && value != null && v.get() != null && value instanceof String) {
                    new AlertDialog.Builder(v.get().topActivity())
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

        app.setShell(this);

        if(_rootApplication == null || _rootApplication.get() == null) {
            _rootApplication = new WeakReference<>(app);
            app.observer().on(new String[]{"app","cancel"}, new Listener<Application>() {
                @Override
                public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                    if (weakObject != null && value != null && value instanceof Map) {
                        Shell vv = v.get();
                        if(vv != null) {
                            String url = V.stringValue(V.get(value,"url"),null);
                            if(url != null) {
                                AppLoading loading = vv.isLoading(url);
                                if(loading != null) {
                                    loading.canceled = true;
                                }
                            }
                        }

                    }
                }
            },app, Observer.PRIORITY_NORMAL,false);
        }

        _protocol.openApplication(app);

        app.run();
    }

    abstract protected IViewContext openViewContext(IResource resource, String path);

    protected IHttpTask send(Application app,HttpOptions options, Object weakObject) {
        _protocol.httpOptions(app,options,weakObject);
        return _http.send(options,weakObject);
    }

    protected void cancel(Application app,Object weakObject) {
        _http.cancel(weakObject);
    }

    protected String encodeJSON(Application app,Object object) {
        return _http.encodeJSON(object);
    }

    protected Object decodeJSON(Application app,String text) {
        return _http.decodeJSON(text);
    }

    protected void open(String url, Object query,IResource resource, String path,String key){

        Application app = new Application(_context,new BasePathResource(resource,path),openViewContext(resource,path));

        {
            final WeakReference<Shell> v = new WeakReference<>(this);
            final WeakReference<Application> a = new WeakReference<>(app);
            app.setHttp(new IHttp() {
                @Override
                public IHttpTask send(HttpOptions options, Object weakObject) {
                    Shell vv = v.get();
                    Application aa = a.get();
                    if(vv != null && aa != null) {
                        return vv.send(aa,options,weakObject);
                    }
                    return null;
                }

                @Override
                public void cancel(Object weakObject) {
                    Shell vv = v.get();
                    Application aa = a.get();
                    if(vv != null && aa != null) {
                        vv.cancel(aa,weakObject);
                    }
                }

                @Override
                public String encodeJSON(Object object) {
                    Shell vv = v.get();
                    Application aa = a.get();
                    if(vv != null && aa != null) {
                        return vv.encodeJSON(aa,object);
                    }
                    return null;
                }

                @Override
                public Object decodeJSON(String text) {
                    Shell vv = v.get();
                    Application aa = a.get();
                    if(vv != null && aa != null) {
                        return vv.decodeJSON(aa,text);
                    }
                    return null;
                }
            });
        }

        app.observer().set(new String[]{"path"},path);
        app.observer().set(new String[]{"url"},url);
        app.observer().set(new String[]{"key"},key);
        app.observer().set(new String[]{"query"},query);

        openApplication(app);

    }


    private static Shell _main;

    public static void setMain(Shell main) {
        _main = main;
    }

    public static Shell main() {
        return _main;
    }

    protected static interface OnLoading {

        void onLoad(String url,File path,AppLoading loading);

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

    public static class AppLoading {
        public String url;
        public boolean canceled;
    }

}
