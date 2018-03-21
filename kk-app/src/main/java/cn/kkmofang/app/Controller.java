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

public class Controller {

    private Application _application;
    private IObserver _page;
    private Map<String,Object> _query;
    private String _path;

    public Application application() {
        return _application;
    }

    public void setApplication(Application application) {
        _application = application;
    }

    public IObserver page() {
        if(_page == null && _application != null) {
            _page = new Observer(_application.context());
        }
        return _page;
    }

    public Map<String,Object> query() {
        if(_query == null) {
            _query = new TreeMap<>();
        }
        return _query;
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

            libs.put("page",page());
            libs.put("query",query());
            libs.put("path",path());

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
        }
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
