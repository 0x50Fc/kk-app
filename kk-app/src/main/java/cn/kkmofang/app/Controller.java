package cn.kkmofang.app;

import android.renderscript.Script;

import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.ScriptContext;

/**
 * Created by zhanghailong on 2018/3/13.
 */

public class Controller extends RecycleContainer {

    private Application _application;
    private IObserver _page;
    private JSObserver _jsPage;
    private JSObserver _jsApp;
    private Map<String,Object> _query;
    private String _path;
    private JSHttp _http;
    private AsyncCaller _caller;
    private JSWebSocket _jsWebSocket;
    public Application application() {
        return _application;
    }

    public void setApplication(Application application) {
        _application = application;
    }

    public IObserver page() {
        if(_page == null && _application != null) {
            _page = new Observer(_application.jsContext());
        }
        return _page;
    }

    public JSObserver jsPage() {
        if(_jsPage == null) {
            _jsPage = new JSObserver(page());
        }
        return _jsPage;
    }

    public Map<String,Object> query() {
        if(_query == null) {
            _query = new TreeMap<>();
        }
        return _query;
    }

    public JSHttp http() {
        if(_http == null) {
            Application app = application();
            if(app != null) {
                _http = new JSHttp(app.http());
            }
        }
        return _http;
    }

    public JSObserver jsApp() {
        if(_jsApp == null) {
            Application app = application();
            if(app != null) {
                _jsApp = new JSObserver(app.observer());
            }
        }
        return _jsApp;
    }

    public AsyncCaller caller() {
        if(_caller == null) {
            _caller = new AsyncCaller();
        }
        return _caller;
    }

    public JSWebSocket jsWebSocket() {
        if(_jsWebSocket == null) {
            _jsWebSocket = new JSWebSocket();
        }
        return _jsWebSocket;
    }

    public void setQuery(Map<String,Object> query) {
        _query = query;
    }

    public String path() {
        return _path;
    }

    public void setPath(String path) {
        _path = path;
    }

    public void run() {

        if(_application != null && _path != null) {

            Map<String,Object> libs = new TreeMap<>();

            libs.put("app",jsApp());
            libs.put("page",jsPage());
            libs.put("query",query());
            libs.put("path",path());
            libs.put("http",http());
            libs.put("setTimeout",caller().SetTimeoutFunc);
            libs.put("clearTimeout",caller().ClearTimeoutFunc);
            libs.put("setInterval",caller().SetIntervalFunc);
            libs.put("clearInterval",caller().ClearIntervalFunc);
            libs.put("WebSocket",jsWebSocket());

            _application.exec(_path + ".js",libs);

        }
    }

    public void willAppear() {
        IObserver page = page();
        if(page != null) {
            page.change(new String[]{"page","willAppear"});
        }
    }

    public void didAppear() {
        IObserver page = page();
        if(page != null) {
            page.change(new String[]{"page","didAppear"});
        }
    }

    public void willDisappear() {
        IObserver page = page();
        if(page != null) {
            page.change(new String[]{"page","willDisappear"});
        }
    }

    public void didDisappear() {
        IObserver page = page();
        if(page != null) {
            page.change(new String[]{"page","didDisappear"});
        }
    }

    public void recycle() {
        if(_page != null) {
            _page.off(new String[]{},null,null);
            _page = null;
        }
        if(_http != null) {
            _http.cancel();
            _http = null;
        }
        if(_jsPage != null) {
            _jsPage.recycle();
            _jsPage = null;
        }
        if(_jsApp != null) {
            _jsApp.recycle();
            _jsApp = null;
        }
        if(_jsWebSocket != null) {
            _jsWebSocket.recycle();
            _jsWebSocket = null;
        }
        if(_caller != null) {
            _caller.recycle();
            _caller = null;
        }
        super.recycle();
    }

    public void setAction(Object action) {
        setPath(ScriptContext.stringValue(ScriptContext.get(action,"path"),null));
        {
            Object v = ScriptContext.get(action,"query");
            if(v instanceof Map) {
                setQuery((Map<String,Object>) v);
            }
        }
    }

}
