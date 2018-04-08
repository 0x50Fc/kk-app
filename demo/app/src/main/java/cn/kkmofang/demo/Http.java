package cn.kkmofang.demo;

import android.text.TextUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.kkmofang.app.ControlDialog;
import cn.kkmofang.http.HttpOptions;
import cn.kkmofang.http.IHttp;
import cn.kkmofang.http.IHttpTask;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by hailong11 on 2018/3/21.
 */

public class Http implements IHttp {
    public static final long DEFAUT_TIMEOUT = 30_000;

    @Override
    public IHttpTask send(final HttpOptions options, final Object weakObject) {
        if (options == null)return null;
        long timeout = options.timeout <= 0?DEFAUT_TIMEOUT:options.timeout;
        String method = TextUtils.isEmpty(options.method)?HttpOptions.METHOD_POST:options.method;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)//毫秒
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();

        String url = options.absoluteUrl();
//        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
//        String content = "";
        FormBody.Builder builder = new FormBody.Builder();

        if (HttpOptions.METHOD_POST.equals(method)){
            if (options.data != null && options.data instanceof Map){
                Map<String, Object> map = (Map<String, Object>) options.data;
                for(Map.Entry entry:map.entrySet()){
                    builder.add((String) entry.getKey(), entry.getValue().toString());
                }
//                JSONObject jb = new JSONObject(map);
//                content = jb.toString();
            }
        }
//        System.out.println("Http post content:"+ content);


        Headers.Builder headerBuilders = new Headers.Builder();
        if (options.headers != null){
            for (Map.Entry<String, Object> entry : options.headers.entrySet()) {
                headerBuilders.add(entry.getKey(), (String) entry.getValue());
            }
        }
        //此处先用一个默认的调试
        headerBuilders.add("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 11_2_6 like Mac OS X) AppleWebKit/604.5.6 (KHTML, like Gecko) Mobile/15D100");


        final Request request = new Request.Builder()
                .url(url)
                .method(method, builder.build())
                .headers(headerBuilders.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                options.onfail.on(e, weakObject);
                System.out.println("Http onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                Map<>
//                String string = response.body().string();
//                System.out.println("Http onResponse:" + string);
//                Headers headers = response.networkResponse().request().headers();
//                int code = response.code();
                //options.onresponse.on(code, "",headers, weakObject);
            }
        });
        return new HttpTask();
    }

    @Override
    public void cancel(Object weakObject) {

    }

    private static class HttpTask implements IHttpTask {

        @Override
        public void cancel() {

        }
    }
}
