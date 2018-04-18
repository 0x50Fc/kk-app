package cn.kkmofang.demo;

import android.content.Context;
import android.renderscript.Script;

import java.util.Map;

import cn.kkmofang.http.HttpOptions;
import cn.kkmofang.http.IHttpTask;
import cn.kkmofang.http.client.HttpClient;
import cn.kkmofang.script.ScriptContext;

/**
 * Created by zhanghailong on 2018/4/18.
 */

public class Http extends HttpClient {

    public static final String baseURL = "https://pay.sc.weibo.com";

    public Http(Context context) {
        super(context, 30, 30, 30);
    }

    public IHttpTask send(HttpOptions options, Object weakObject) {

        if(options.url != null && options.url.startsWith("/")) {

            options.url = baseURL + options.url;

            if(HttpOptions.TYPE_JSON.equals(options.type) && options.data instanceof Map) {
                ScriptContext.set(options.data,"v_p","56");
                ScriptContext.set(options.data,"lang","zh_CN");
                ScriptContext.set(options.data,"from","107C295010");
                ScriptContext.set(options.data,"wm","3333_2001");
            }

        }

        return super.send(options,weakObject);
    }

}
