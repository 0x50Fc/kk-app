package cn.kkmofang.app;


import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.DocumentView;
import cn.kkmofang.view.Element;
import cn.kkmofang.view.ViewElement;
import cn.kkmofang.view.event.Event;
import cn.kkmofang.view.event.EventFunction;
import cn.kkmofang.view.value.V;

/**
 * Created by zhanghailong on 2018/3/13.
 */

public class ViewController extends Controller {

    private ViewElement _element;
    private String _viewPath;

    public ViewElement element() {
        return _element;
    }

    @Override
    public void recycle() {
        if(_element != null) {
            _element.recycleView();
            _element = null;
        }
        super.recycle();
    }

    public android.view.View view() {
        if(_element != null) {
            return _element.view();
        }
        return null;
    }

    public void setAction(Object action) {
        super.setAction(action);
        _viewPath = V.stringValue(V.get(action,"view"),null);
    }

    public String viewPath() {
        return _viewPath;
    }

    public void setViewPath(String view) {
        _viewPath = view;
    }

    public ViewElement run(DocumentView documentView) {

        Application app = application();
        String path = path();
        IObserver page = page();

        if(app != null && path != null) {

            String view = _viewPath;

            if(view != null) {
                view = view + "_view.js";
            } else {
                view = path + "_view.js";
            }

            Element e = app.element(view,page);

            if(e != null && e instanceof ViewElement) {
                _element = (ViewElement) e;
            } else {
                _element = null;
            }

        }

        run();

        documentView.setElement(_element);

        if( page != null){

            final Set<String> keySet = new TreeSet<>();

            Object keys = page.get(new String[]{"page","layoutKeys"});
            
            if(keys != null && keys instanceof List) {

                for(Object key : (List) keys) {
                    keySet.add(ScriptContext.stringValue(key, ""));
                }
            } else {
                keySet.add("data");
            }

            page.on(new String[]{}, new Listener<DocumentView>() {
                @Override
                public void onChanged(IObserver observer, String[] changedKeys, Object value, DocumentView documentView) {

                    if(value != null && documentView != null) {
                        if(changedKeys.length == 0 || keySet.contains(changedKeys[0])) {

                            String keys[] = new String[changedKeys.length +1];

                            for(int i=0;i<changedKeys.length;i++) {
                                keys[i] = changedKeys[i];
                            }

                            keys[changedKeys.length] = "animated";

                            boolean animated = V.booleanValue( observer.get(keys),false);

                            documentView.setNeedsLayout(animated);
                        }
                    }
                }
            },documentView, Observer.PRIORITY_LOW,true);
        }

        if(_element != null) {

            final WeakReference<DocumentView> view = new WeakReference<DocumentView>(documentView);

            _element.on("layout", new EventFunction() {
                @Override
                public void onEvent(Event event) {
                    final DocumentView v = view.get();
                    if(v != null) {
                        v.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                v.setNeedsLayout(true);
                            }
                        });
                    }
                }
            });

        }

        return _element;
    }
}
