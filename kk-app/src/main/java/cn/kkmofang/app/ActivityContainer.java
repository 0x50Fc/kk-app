package cn.kkmofang.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import android.support.annotation.NonNull;

import java.util.Map;
import java.util.TreeMap;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.view.DocumentView;
import cn.kkmofang.view.ViewElement;
import cn.kkmofang.view.value.V;
import cn.kkmofang.unity.R;

import static cn.kkmofang.app.PermissionsProtocol.STATUS_CANCEL;
import static cn.kkmofang.app.PermissionsProtocol.STATUS_OK;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public class ActivityContainer extends Activity implements Container , IWindowContainer {


    protected Controller _controller;

    protected DocumentView _documentView;

    protected void onCreateDocumentView() {
        setContentView(cn.kkmofang.unity.R.layout.kk_document);
        _documentView = (DocumentView) findViewById(cn.kkmofang.unity.R.id.kk_documentView);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        {
            String v = getIntent().getStringExtra("orientation");

            if ("landscape".equals(v)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }


        {
            Boolean v = getIntent().getBooleanExtra("fullScreen",getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            if(v) {
                Window window = getWindow();
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                window.addFlags(flag);
                _fullScreenWindowContainer = true;
                hideNavr(window);
                final WeakReference<Window> wr = new WeakReference<>(window);
                window.getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        Window w = wr.get();
                        if (w != null){
                            hideNavr(w);
                        }
                    }
                });
            }

        }

        onCreateDocumentView();

        Application app = null;
        Object action = null;

        if(savedInstanceState != null) {
            app = Application.get(savedInstanceState.getLong("appid",0));
            action = savedInstanceState.getSerializable("action");
        } else {
            app = Application.get(getIntent().getLongExtra("appid",0));
            action = getIntent().getSerializableExtra("action");
        }

        if(app != null && action != null && _documentView != null) {
            open(app,action);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(_documentView != null) {
            ViewElement element = _documentView.element();
            if(element != null) {
                element.onStart(this);
            }
        }
    }

    @Override
    protected void onStop() {

        if(_documentView != null) {
            ViewElement element = _documentView.element();
            if(element != null) {
                element.onStop(this);
            }
        }

        super.onStop();
    }


    @Override
    protected void onDestroy() {

        if( _controller != null) {
            _controller.recycle();
            _controller = null;
        }

        if(_documentView != null) {
            ViewElement element = _documentView.element();
            if(element != null) {
                element.onDestroy(this);
                element.recycle();
            }
        }

        if(_application != null ){
            Shell shell = _application.shell();
            if(shell != null) {
                shell.removeActivity(this);
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(_application != null) {
            outState.putLong("appid",_application.id());
        }

        if(_action != null && _action instanceof Serializable) {
            outState.putSerializable("action",(Serializable) _action);
        }

    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(_documentView != null) {
            _documentView.setNeedsLayout(false);
        }
    }

    @Override
    public boolean isOpened() {
        return _controller != null;
    }

    private Application _application;
    private Object _action;

    protected void onCreateViewElement(ViewElement element) {

    }

    protected void onCreatePage(IObserver page) {



    }

    protected void onRun(Controller controller) {


    }

    @Override
    protected void onResume() {
        super.onResume();

        if(_documentView != null) {
            ViewElement element = _documentView.element();
            if(element != null) {
                element.onResume(this);
            }
        }

        if(_controller != null) {
            final Controller v = _controller;
            v.application().post(new Runnable() {
                @Override
                public void run() {
                    v.willAppear();
                    v.didAppear();
                }
            });
        }

    }

    @Override
    protected void onPause() {

        if(_controller != null) {
            final Controller v = _controller;
            v.application().post(new Runnable() {
                @Override
                public void run() {
                    v.willDisappear();
                    v.didDisappear();
                }
            });
        }

        if(_documentView != null) {
            ViewElement element = _documentView.element();
            if(element != null) {
                element.onPause(this);
            }
        }

        super.onPause();
    }


    @Override
    public void open(Application app, Object action) {

        if(_application != null ){
            Shell shell = _application.shell();
            if(shell != null) {
                shell.removeActivity(this);
            }
        }

        _application = app;
        _action = action;

        if(_application != null ){
            Shell shell = _application.shell();
            if(shell != null) {
                shell.addActivity(this);
            }
        }

        if(_documentView == null) {
            return;
        }

        if( _controller != null) {
            _controller.recycle();
            _controller = null;
        }

        _controller = app.open(action);

        if(_controller != null) {

            final DocumentView documentView = _documentView;
            final WeakReference<ActivityContainer> v = new WeakReference<>(this);

            _controller.page().on(new String[]{"action", "close"}, new Listener<ActivityContainer>() {
                @Override
                public void onChanged(IObserver observer, String[] changedKeys, Object value, ActivityContainer weakObject) {

                    if(weakObject != null && value != null) {
                        weakObject.setRecycleWindowContainer();
                    }
                }
            },this, Observer.PRIORITY_NORMAL,false);

            final WeakReference<IObserver> w = new WeakReference<>(_controller.page());
            _documentView.setOnPageSizeChangeListener(new DocumentView.PageSizeChangeListener() {
                @Override
                public void onChange(int l, int t, int r, int b) {
                    IObserver o = w.get();
                    if (o != null){
                        Map<String, Object> data = new TreeMap<>();
                        data.put("width", r - l);
                        data.put("height", b - t);
                        o.set(new String[]{"page", "resize"}, data);
                    }
                }
            });

            onCreatePage(_controller.page());

            _controller.application().post(new Runnable() {
                @Override
                public void run() {
                    if(_controller == null) {
                        return;
                    }
                    if(_controller instanceof ViewController) {
                        ViewElement element = ((ViewController) _controller).run(documentView);
                        ActivityContainer vv = v.get();
                        if(vv != null && element != null) {
                            vv.onCreateViewElement(element);
                        }
                    } else {
                        _controller.run();
                    }
                    onRun(_controller);
                }
            });
        }
    }


    private boolean _recycleWindowContainer = false;
    private int _obtainWindowCount = 0;

    @Override
    public void obtainWindowContainer() {
        _obtainWindowCount ++;
    }

    @Override
    public void recycleWindowContainer() {
        --_obtainWindowCount;
        if(_recycleWindowContainer && _obtainWindowCount == 0) {
            finish();
        }
    }

    @Override
    public void setRecycleWindowContainer() {
        _recycleWindowContainer = true;
        if(_obtainWindowCount == 0) {
            finish();
        }
    }

    private boolean _fullScreenWindowContainer = false;

    @Override
    public boolean isFullScreenWindowContainer() {
        return _fullScreenWindowContainer;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsProtocol.PermissionCode p = PermissionsProtocol.PermissionCode.vOf(requestCode);
        if (p != PermissionsProtocol.PermissionCode.NONE){
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED){
                    granted = false;
                }
            }
            if(_application != null) {
                Map<String,Object> data = new TreeMap<>();
                data.put(p.getPname(), granted?STATUS_OK:STATUS_CANCEL);
                _application.observer().set(new String[]{"permissions","result"},data);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        windowAnimation();
    }

    public void windowAnimation(){
        if (_action != null){
            boolean animated = V.booleanValue(V.get(_action, "animated"), true);
            if (!animated) {
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return;
            }
            overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
        }
    }


    protected void hideNavr(Window w){
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 19){
            uiOptions |= 0x00001000;
        } else {
            uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        w.getDecorView().setSystemUiVisibility(uiOptions);
    }


}
