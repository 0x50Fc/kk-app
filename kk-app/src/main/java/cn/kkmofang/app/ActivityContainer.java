package cn.kkmofang.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import java.io.Serializable;

import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.view.DocumentView;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public class ActivityContainer extends Activity implements Container {


    private Controller _controller;
    private DocumentView _documentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.kk_document);

        _documentView = (DocumentView) findViewById(R.id.kk_documentView);

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
    protected void onStop() {

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

        super.onStop();
    }


    @Override
    protected void onDestroy() {

        if( _controller != null) {
            _controller.recycle();
            _controller = null;
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

    @Override
    public void open(Application app, Object action) {

        _application = app;
        _action = action;

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

            _controller.page().on(new String[]{"action", "close"}, new Listener<ActivityContainer>() {
                @Override
                public void onChanged(IObserver observer, String[] changedKeys, Object value, ActivityContainer weakObject) {

                    if(weakObject != null && value != null) {
                        weakObject.finish();
                    }
                }
            },this, Observer.PRIORITY_NORMAL,false);

            _controller.page().on(new String[]{"page", "orientation"}, new Listener<ActivityContainer>() {
                @Override
                public void onChanged(IObserver observer, String[] changedKeys, Object value, ActivityContainer weakObject) {

                    if(weakObject != null && value != null) {
                        if("landscape".equals(value)) {
                            weakObject.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        } else {
                            weakObject.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }

                    }
                }
            },this, Observer.PRIORITY_NORMAL,false);

            _controller.application().post(new Runnable() {
                @Override
                public void run() {
                    if(_controller instanceof ViewController) {
                        ((ViewController) _controller).run(documentView);
                    } else {
                        _controller.run();
                    }
                }
            });
        }
    }
}
