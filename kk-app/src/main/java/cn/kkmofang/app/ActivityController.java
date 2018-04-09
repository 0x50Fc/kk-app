package cn.kkmofang.app;

import android.app.Activity;
import android.os.Bundle;

import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.view.DocumentView;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public class ActivityController extends Activity {

    private Controller _controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kk_document);

        long id = savedInstanceState != null ? savedInstanceState.getLong("id",0) : getIntent().getLongExtra("id",0);

        if(_controller == null && id != 0) {

            _controller = Shell.popOpenController(id);

            if(_controller != null) {

                _controller.page().on(new String[]{"action", "close"}, new Listener<ActivityController>() {
                    @Override
                    public void onChanged(IObserver observer, String[] changedKeys, Object value, ActivityController weakObject) {

                        if(weakObject != null && value != null) {
                            weakObject.finish();
                        }
                    }
                },this, Observer.PRIORITY_NORMAL,false);
            }
        }

        final DocumentView documentView = (DocumentView) findViewById(R.id.kk_documentView);

        if(_controller != null) {
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(_controller != null) {
            long id = Shell.pushOpenController(_controller);
            outState.putLong("id",id);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(_controller != null) {
            _controller.application().post(new Runnable() {
                @Override
                public void run() {
                    _controller.willAppear();
                    _controller.didAppear();
                }
            });
        }
    }

    @Override
    protected void onStop() {


        if(_controller != null) {
            _controller.application().post(new Runnable() {
                @Override
                public void run() {
                    _controller.willDisappear();
                    _controller.didDisappear();
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
}
