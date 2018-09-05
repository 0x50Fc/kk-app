package cn.kkmofang.app;

import android.app.Activity;
import android.view.*;
import android.view.View;

import cn.kkmofang.view.DocumentView;
import cn.kkmofang.view.Element;
import cn.kkmofang.view.IViewApplication;
import cn.kkmofang.view.ViewElement;

/**
 * Created by zhanghailong on 2018/9/5.
 */

public class PageElement extends ViewElement {

    private boolean _pageShowing;
    private ViewController _viewController;

    public PageElement() {
        super();
        setAttrs("hidden","true");
    }

    @Override
    public Class<?> viewClass() {
        return DocumentView.class;
    }

    public DocumentView documentView() {
        return (DocumentView) view();
    }

    @Override
    public void recycle() {
        if(_viewController != null) {
            _viewController.recycle();
            _viewController = null;
        }
        super.recycle();
    }

    protected void showPage() {

        DocumentView view = documentView();

        if(view == null) {
            return ;
        }

        if(!_pageShowing && _viewController != null) {
            _pageShowing = true;
            _viewController.willAppear();
            android.view.View v = _viewController.view();
            if(v != null ) {
                v.setVisibility(android.view.View.VISIBLE);
            }
            _viewController.didAppear();
        }
    }

    protected void hidePage() {

        DocumentView view = documentView();

        if(view == null) {
            return ;
        }

        if(_pageShowing && _viewController != null) {
            _pageShowing = false;
            _viewController.willDisappear();
            android.view.View v = _viewController.view();
            if(v != null ) {
                v.setVisibility(View.GONE);
            }
            _viewController.didDisappear();
        }
    }

    public Application getApplication() {
        IViewApplication app = viewContext.getViewApplication();
        if(app != null && app instanceof Application) {
            return  (Application) app;
        }
        return null;
    }

    protected void open() {

        DocumentView view = documentView();

        if(view == null) {
            return ;
        }

        String path = get("path");

        if(_viewController != null) {

            String p = _viewController.path();

            if(p == path || (p != null && p.equals(path))|| (path != null && path.equals(p))) {
                if(isHidden()) {
                    hidePage();
                } else {
                    showPage();
                }
            }

            return;
        }

        if(_viewController != null) {
            if(_pageShowing) {
                _viewController.willDisappear();
                _viewController.didDisappear();
            }
            _viewController.recycle();
            _viewController = null;
        }

        if(path == null || path.equals("")) {
            return ;
        }

        if(isHidden()) {
            hidePage();
            return;
        }

        Application app = getApplication();

        if(app == null) {
            return;
        }

        _viewController = new ViewController();
        _viewController.setApplication(app);
        _viewController.setQuery(data());
        _viewController.setPath(path);
        _viewController.setViewPath(get("view"));
        _viewController.run(view);

        _viewController.willAppear();
        _viewController.didAppear();

        _pageShowing = true;
    }

    @Override
    protected void onSetProperty(View view, String key, String value) {
        super.onSetProperty(view,key,value);
        if("path".equals(key) || "hidden".equals(key)) {
            open();
        }
    }

    @Override
    protected void onLayout(View view) {
        super.onLayout(view);

        if(view instanceof  DocumentView) {
            ((DocumentView) view).setNeedsLayout(false);
        }
    }


    public void onPause(Activity activity) {

        if(_viewController != null) {
            ViewElement element = _viewController.element();
            if(element != null) {
                element.onPause(activity);
            }
        }

        super.onPause(activity);
    }

    public void onResume(Activity activity) {

        if(_viewController != null) {
            ViewElement element = _viewController.element();
            if(element != null) {
                element.onResume(activity);
            }
        }

        super.onResume(activity);
    }

    public void onStart(Activity activity) {
        if(_viewController != null) {
            ViewElement element = _viewController.element();
            if(element != null) {
                element.onStart(activity);
            }
        }

        super.onStart(activity);
    }

    public void onStop(Activity activity) {

        if(_viewController != null) {
            ViewElement element = _viewController.element();
            if(element != null) {
                element.onStop(activity);
            }
        }

        super.onStop(activity);
    }

    public void onDestroy(Activity activity){

        if(_viewController != null) {
            ViewElement element = _viewController.element();
            if(element != null) {
                element.onDestroy(activity);
            }
        }

        super.onDestroy(activity);
    }

}
