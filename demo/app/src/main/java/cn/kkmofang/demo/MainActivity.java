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

import static cn.kkmofang.demo.ControllerActivity.DEFAULT_SIZE;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);
        initUnit();

        Application app = new Application(this,new AssetResource(getAssets(),"main/"),new Http()
                ,new AssetViewContext(getApplicationContext(),getAssets(),"main/"));

        app.observer().on(new String[]{"action", "open"}, new Listener<Application>() {
            @Override
            public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                if(weakObject != null && value != null) {
                    Controller controller = weakObject.open(value);
                    if ("window".equals(controller.getType())){
                        new ControlDialog.Builder(weakObject.activity()).runView(controller);
                    }else {
//                        ControllerActivity.openThis(weakObject.activity(), "main/", value);
//                        weakObject.activity().finish();
                        ControllerFragment fragment = new ControllerFragment();
                        fragment.setController(controller);
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.add(R.id.contentView, fragment);
                        ft.commit();
                    }
                }
            }
        },app, Observer.PRIORITY_NORMAL,false);
        app.run();
    }

    public void initUnit(){
        if (Pixel.UnitRPX != 1.0f || Pixel.UnitPX != 1.0f)return;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        Pixel.UnitRPX = Math.min(metrics.widthPixels,metrics.heightPixels) / DEFAULT_SIZE;
        Pixel.UnitPX = metrics.density;
    }

}
