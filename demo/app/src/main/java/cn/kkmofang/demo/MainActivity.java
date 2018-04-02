package cn.kkmofang.demo;

import android.graphics.Point;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

import cn.kkmofang.app.Application;
import cn.kkmofang.app.AssetResource;
import cn.kkmofang.app.ControlDialog;
import cn.kkmofang.app.Controller;
import cn.kkmofang.app.ControllerFragment;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.value.Pixel;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application app = new Application(this,new AssetResource(getAssets(),"main/"),null
                ,new AssetViewContext(getApplicationContext(),getAssets(),"main/"));

        app.observer().on(new String[]{"action", "open"}, new Listener<Application>() {
            @Override
            public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                if(weakObject != null && value != null) {
                    Controller controller = weakObject.open(value);
                    if ("window".equals(controller.getType())){
                        new ControlDialog.Builder(weakObject.activity()).runView(controller);
                    }else {
                        ControllerActivity.openThis(weakObject.activity(), "main/", value);
                        weakObject.activity().finish();
                    }
                }
            }
        },app, Observer.PRIORITY_NORMAL,false);
        app.run();
    }

}
