package cn.kkmofang.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.view.DocumentView;
import cn.kkmofang.unity.R;
import cn.kkmofang.view.ViewElement;
import cn.kkmofang.view.value.Pixel;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public class WindowController extends Dialog {

    private DocumentView _documentView;
    private Controller _controller;
    private IWindowContainer _container;
    private boolean _fullScreen = false;

    protected void onCreateViewElement(ViewElement element) {

        if(!_fullScreen) {
            float top = element.padding.top.floatValue(0, 0) / Pixel.UnitPX;
            Pixel p = new Pixel();
            p.type = Pixel.Type.PX;
            p.value = top - 20;
            element.padding.top.set(p);
        }

    }

    protected void onCreatePage(IObserver page) {


    }

    public WindowController(android.content.Context context, Controller controller) {
        super(context,R.style.KKWindow);

        if(context instanceof IWindowContainer) {
            _container = ((IWindowContainer) context);
            _container.obtainWindowContainer();
            if(_container.isFullScreenWindowContainer()) {
                _fullScreen = true;
                Window window = getWindow();
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                window.addFlags(flag);
            }
        }


        _controller = controller;
        setContentView(R.layout.kk_document);


        final WeakReference<WindowController> v = new WeakReference<>(this);

        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                WindowController vv = v.get();
                if(vv != null) {
                    vv.recycle();
                }
            }
        });

        if(_controller != null) {
            _controller.page().on(new String[]{"action", "close"}, new Listener<WindowController>() {
                @Override
                public void onChanged(IObserver observer, String[] changedKeys, Object value, WindowController weakObject) {
                    if(weakObject != null && value != null && weakObject.isShowing()) {
                        android.content.Context c = weakObject.getContext();
                        try {
                            if (c instanceof Activity){
                                if (!((Activity) c).isFinishing() && !((Activity) c).isDestroyed()){
                                    weakObject.dismiss();
                                }
                            }else {
                                weakObject.dismiss();
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            },this, Observer.PRIORITY_NORMAL,false);
        }

        _documentView = findViewById(R.id.kk_documentView);

        if(_controller != null) {

            onCreatePage(_controller.page());

            _controller.application().post(new Runnable() {
                @Override
                public void run() {
                    if(_controller instanceof ViewController) {
                        ViewElement element = ((ViewController) _controller).run(_documentView);
                        WindowController vv = v.get();
                        if(vv != null && element != null) {
                            vv.onCreateViewElement(element);
                        }
                    } else {
                        _controller.run();
                    }
                    if(isShowing()) {
                        _controller.willAppear();
                        _controller.didAppear();
                    }
                }
            });
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        if(window != null){
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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


        if(_controller != null) {
            _controller.willDisappear();
            _controller.didDisappear();
        }


        super.onStop();
    }

    public void recycle() {

        if(_controller != null) {
            _controller.recycle();
            _controller = null;
        }

        if(_documentView != null) {
            ViewElement element = _documentView.element();
            if(element != null) {
                element.recycleView();
            }
            _documentView.setElement(null);
            _documentView = null;
        }

        if(_container != null) {
            _container.recycleWindowContainer();
            _container = null;
        }

    }


}