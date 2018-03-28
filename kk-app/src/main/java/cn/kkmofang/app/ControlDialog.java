package cn.kkmofang.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.*;
import android.view.View;

import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.view.DocumentView;

/**
 *
 * Created by nwangchao15 on 2018/3/28.
 */

public class ControlDialog extends Dialog {
    public ControlDialog(@NonNull android.content.Context context) {
        super(context);
    }

    public ControlDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    public static class Builder{
        private ControlDialog cDialog;
        private DocumentView contentView;
        private Context context;


        public Builder(Context context) {
            this.context = context;
            cDialog = new ControlDialog(context, R.style.Dialog);
            contentView = new DocumentView(context);

            cDialog.addContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        public ControlDialog create(){
            return cDialog;
        }

        public void runView(final Controller controller){

            controller.page().on(new String[]{"action", "close"}, new Listener<Application>() {
                @Override
                public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                    if (cDialog != null && cDialog.isShowing()){
                        cDialog.dismiss();

                    }
                }
            }, controller.application(), Observer.PRIORITY_NORMAL, false);

            controller.application().post(new Runnable() {
                @Override
                public void run() {
                    if(controller instanceof ViewController) {
                        ((ViewController) controller).run(contentView);
                    } else {
                        controller.run();
                    }
                    cDialog.show();
                    //设置dialog全屏显示
                    Window window = cDialog.getWindow();
                    if(window != null){
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.width = ((Activity)context).getWindowManager().getDefaultDisplay().getWidth();
                        params.height = ((Activity)context).getWindowManager().getDefaultDisplay().getHeight();
                        window.setAttributes(params);
                    }

                }
            });
        }
    }
}
