package cn.kkmofang.demo;

import android.graphics.Point;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Window;

import cn.kkmofang.app.Application;
import cn.kkmofang.app.AssetResource;
import cn.kkmofang.app.Controller;
import cn.kkmofang.app.ControllerFragment;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.view.value.Pixel;

public class MainActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        Point size = new Point();

        getWindowManager().getDefaultDisplay().getSize(size);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Pixel.UnitRPX = Math.min(size.x,size.y) / 750.0f;
        Pixel.UnitPX = metrics.density;

        System.out.println("UnitPX:" + Pixel.UnitPX + ":" + Pixel.UnitRPX + ":" + size.x + ":" + size.y + ":" + metrics.densityDpi);

        final FragmentManager fmg = getSupportFragmentManager();

        Application app = new Application(this,new AssetResource(getAssets(),"main/"),null
                ,new AssetViewContext(getApplicationContext(),getAssets(),"main/"));

        app.observer().on(new String[]{"action", "open"}, new Listener<Application>() {
            @Override
            public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                if(weakObject != null && value != null) {
                    Controller controller = weakObject.open(value);
                    ControllerFragment fragment = new ControllerFragment();
                    fragment.setController(controller);
                    fmg.beginTransaction().add(R.id.contentView,fragment).commit();
                }
            }
        },app, Observer.PRIORITY_NORMAL,false);

        app.run();
    }
}
