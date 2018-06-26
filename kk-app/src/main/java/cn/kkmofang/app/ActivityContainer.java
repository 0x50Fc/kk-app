package cn.kkmofang.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.view.DocumentView;
import cn.kkmofang.view.ViewElement;
import cn.kkmofang.unity.R;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public class ActivityContainer extends Activity implements Container , IWindowContainer {


    private Controller _controller;

    protected DocumentView _documentView;

    protected void onCreateDocumentView() {
        setContentView(R.layout.kk_document);
        _documentView = (DocumentView) findViewById(R.id.kk_documentView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String v = getIntent().getStringExtra("orientation");

        if("landscape".equals(v)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
                element.recycleView();
                element.onDestroy(this);
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
                        weakObject.finish();
                    }
                }
            },this, Observer.PRIORITY_NORMAL,false);


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
    }
}
