package cn.kkmofang.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.view.*;

import cn.kkmofang.view.DocumentView;
import cn.kkmofang.view.ViewContext;

/**
 * Created by hailong11 on 2018/3/21.
 */

public class ControllerFragment extends Fragment {

    private Controller _controller;

    public Controller getController() {
        return _controller;
    }

    public ViewController getViewController() {
        if(_controller instanceof ViewController) {
            return (ViewController) _controller;
        }
        return null;
    }

    public void setController(Controller controller) {
        _controller = controller;
    }

    public android.view.View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        final DocumentView view = new DocumentView(container.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));

        final Controller controller = _controller;

        if(controller != null) {

            controller.application().post(new Runnable() {
                @Override
                public void run() {
                    if(controller instanceof ViewController) {
                        ((ViewController) controller).run(view);
                    } else {
                        controller.run();
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(_controller != null) {
            _controller.willAppear();
            _controller.didAppear();
        }
    }

    @Override
    public void onStop() {

        if(_controller != null) {
            _controller.willDisappear();
            _controller.didDisappear();
        }

        super.onStop();
    }
}
