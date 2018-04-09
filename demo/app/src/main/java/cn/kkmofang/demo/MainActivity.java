package cn.kkmofang.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import cn.kkmofang.app.BasePathResource;
import cn.kkmofang.app.IResource;
import cn.kkmofang.app.Shell;
import cn.kkmofang.http.client.HttpClient;
import cn.kkmofang.view.IViewContext;


public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getApplicationContext();

        Shell.setMain(new Shell(context,new HttpClient(context,30,30,30)) {
            @Override
            protected IViewContext openViewContext(IResource resource, String path) {
                return new ViewContext(context,new BasePathResource(resource,path));
            }
        });

        Shell.main().setRootActivity(this);
        Shell.main().open("asset://main/");
    }



}
