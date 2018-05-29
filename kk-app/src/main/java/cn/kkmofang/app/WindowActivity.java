package cn.kkmofang.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.view.DocumentView;
import cn.kkmofang.view.ViewElement;
import cn.kkmofang.unity.R;

public class WindowActivity extends Activity {
    private DocumentView _documentView;
    private Controller _controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kk_document);
        _documentView = (DocumentView) findViewById(cn.kkmofang.unity.R.id.kk_documentView);

        Application app = null;
        Object action = null;
        if (getIntent() != null){
            action = getIntent().getSerializableExtra("action");
            app = Application.get(getIntent().getLongExtra("appid", 0));
            if (app != null && action != null){
                _controller = app.open(action);
            }
        }

        if(_controller != null) {
            _controller.page().on(new String[]{"action", "close"}, new Listener<WindowActivity>() {
                @Override
                public void onChanged(IObserver observer, String[] changedKeys, Object value, WindowActivity weakObject) {
                    if(weakObject != null && value != null) {
                        weakObject.finish();
                    }
                }
            },this, Observer.PRIORITY_NORMAL,false);
        }

        if(_controller != null) {
            _controller.application().post(new Runnable() {
                @Override
                public void run() {
                    if(_controller instanceof ViewController) {
                        ((ViewController) _controller).run(_documentView);
                    } else {
                        _controller.run();
                    }
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(_controller != null) {
            _controller.willAppear();
            _controller.didAppear();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(_controller != null) {
            _controller.willDisappear();
            _controller.didDisappear();
        }
    }

    @Override
    protected void onDestroy() {
        if (_controller != null){
            _controller.recycle();
        }

        if(_documentView != null) {
            ViewElement element = _documentView.element();
            if(element != null) {
                element.recycleView();
            }
            _documentView.setElement(null);
        }
        super.onDestroy();
    }
}
