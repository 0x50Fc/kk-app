package cn.kkmofang.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.app.ActivityContainer;
import cn.kkmofang.app.Application;
import cn.kkmofang.app.BasePathResource;
import cn.kkmofang.app.IResource;
import cn.kkmofang.app.Shell;
import cn.kkmofang.http.client.HttpClient;
import cn.kkmofang.view.IViewContext;


public class MainActivity extends ActivityContainer {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getApplicationContext();

        Shell.setMain(new Shell(context,new HttpClient(context,30,30,30)) {
            @Override
            protected IViewContext openViewContext(IResource resource, String path) {
                return new ViewContext(context,new BasePathResource(resource,path));
            }

            @Override
            public void openApplication(Application app) {

                Map<String,Object> data = new TreeMap<>();

                data.put("gsid","_2A253RBkcDeTxGeBO6FEX8ibFzziIHXVSaQFvrDV6PUJbi9ANLWWlkWpNSgDOgg_U3NpcBOza2xOtjtB7UZbZIeTp");
                data.put("uid","6033628944");

                app.observer().set(new String[]{"auth"},data);

                super.openApplication(app);
            }
        });

        Shell.main().setRootActivity(this);
        Shell.main().open("asset://main/");
    }



}
