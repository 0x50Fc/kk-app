package cn.kkmofang.demo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;

import java.util.Stack;

import cn.kkmofang.app.ActionContext;
import cn.kkmofang.app.Application;
import cn.kkmofang.app.AssetResource;
import cn.kkmofang.app.ControlDialog;
import cn.kkmofang.app.Controller;
import cn.kkmofang.app.ControllerFragment;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.IScriptContext;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.value.Pixel;

/**
 * Created by nwangchao15 on 2018/4/2.
 */

public class ControllerActivity extends FragmentActivity {
    private String mBasePath;
    private Object mPage;

    public static final String BASEPATH = "basePath";
    public static final float DEFAULT_SIZE = 750.0f;

    public static void openThis(Context context, String basePath, Object value){
        Intent intent = new Intent(context, ControllerActivity.class);
        intent.putExtra(BASEPATH, basePath);
        ActionContext.getInstance().pushContext(value);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_control);
        mBasePath = getIntent().getStringExtra(BASEPATH);
        mPage = ActionContext.getInstance().popContext();

        initUnit();
        runPage();
    }

    public void initUnit(){
        if (Pixel.UnitRPX != 1.0f || Pixel.UnitPX != 1.0f)return;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        Pixel.UnitRPX = Math.min(metrics.widthPixels,metrics.heightPixels) / DEFAULT_SIZE;
        Pixel.UnitPX = metrics.density;
    }

    public void runPage(){
        Application app = new Application(this,new AssetResource(getAssets(),mBasePath),null
                ,new AssetViewContext(getApplicationContext(),getAssets(),mBasePath));

        app.observer().on(new String[]{"action", "open"}, new Listener<Application>() {
            @Override
            public void onChanged(IObserver observer, String[] changedKeys, Object value, Application weakObject) {
                if(weakObject != null && value != null) {
                    Controller controller = weakObject.open(value);
                    if ("window".equals(controller.getType())){
                        new ControlDialog.Builder(weakObject.activity()).runView(controller);
                    }else {
                        openThis(weakObject.activity(), mBasePath, value);
                    }
                }
            }
        },app, Observer.PRIORITY_NORMAL,false);

        Controller controller = app.open(mPage);
        ControllerFragment fragment = new ControllerFragment();
        fragment.setController(controller);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.llContainer, fragment);
        ft.commit();
    }
}
