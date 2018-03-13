package cn.kkmofang.app;

import android.provider.DocumentsContract;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.DocumentView;
import cn.kkmofang.view.Element;
import cn.kkmofang.view.ViewElement;

/**
 * Created by hailong11 on 2018/3/13.
 */

public class ViewController extends Controller {

    private ViewElement _element;
    private Set<String> _elementNeedsLayoutDataKeys;

    @Override
    public void recycle() {
        if(_element != null) {
            _element.recycleView();
        }
        super.recycle();
    }


    public void run(DocumentView documentView) {

        Application app = application();
        String path = path();
        IObserver page = page();

        if(app != null && path != null) {

            Element e = app.element(path,page);

            if(e != null && e instanceof ViewElement) {
                _element = (ViewElement) e;
            } else {
                _element = null;
            }

        }

        run();

        documentView.setElement(_element);

        if( page != null){

            Object keys = page.get(new String[]{"page","layoutKeys"});
            
            if(keys != null && keys instanceof List) {
                final Set<String> keySet = new TreeSet<>();
                for(Object key : (List) keys) {
                    keySet.add(ScriptContext.stringValue(key,""));
                }
                
                page.on(new String[]{}, new Listener<DocumentView>() {
                    @Override
                    public void onChanged(IObserver observer, String[] changedKeys, Object value, DocumentView documentView) {

                        if(value != null && documentView != null) {
                            if(changedKeys.length == 0 || keySet.contains(changedKeys[0])) {
                                documentView.requestLayout();
                            }
                        }
                    }
                },documentView, Observer.PRIORITY_LOW,true);
            }
        }
    }
}
