package cn.kkmofang.app;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import cn.kkmofang.http.IHttp;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.IViewContext;
import cn.kkmofang.view.value.Pixel;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public abstract class Shell {

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

    public void open(String url)  {

        if(url == null) {
            return;
        }

        if(url.startsWith("asset://")) {
            open(url,new AssetResource(_context.getAssets(),""),url.substring(8));
        } else if(url.startsWith("http://") || url.startsWith("https://")) {

        } else {
            open(url,new FileResource(null),url);
        }
    }

    public void update(String url) {

        if(url == null) {
            return;
        }

        if(url.startsWith("http://") || url.startsWith("https://")) {

        }
    }

    protected void openWindow(Application app, Controller controller,Object action) {
        WindowController v = new WindowController(_context,controller);
        v.show();
    }

    protected void openActivity(Application app, Controller controller,Object action) {

        Activity root = rootActivity();

        if(root != null && action instanceof Serializable) {
            String target = ScriptContext.stringValue(ScriptContext.get(action,"target"),null);
            if(root instanceof Container
                    && (! ((Container) root).isOpened()|| "root".equals(target) )) {
                ((Container) root).open(app,action);
            } else {
                Intent i = new Intent(_context, ActivityContainer.class);
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

}
