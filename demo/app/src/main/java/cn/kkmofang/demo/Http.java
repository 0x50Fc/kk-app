package cn.kkmofang.demo;

import android.text.TextUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.kkmofang.http.HttpOptions;
import cn.kkmofang.http.IHttp;
import cn.kkmofang.http.IHttpTask;
import okhttp3.Call;
import okhttp3.Callback;
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
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String content = "";
        if (HttpOptions.METHOD_POST.equals(method)){
            // TODO: 2018/3/30 获取json content
        }


        Headers.Builder headerBuilders = new Headers.Builder();
        if (options.headers != null){
            for (Map.Entry<String, Object> entry : options.headers.entrySet()) {
                headerBuilders.add(entry.getKey(), (String) entry.getValue());
            }
        }

        final Request request = new Request.Builder()
                .url(url)
                .method(method, RequestBody.create(mediaType, content))
                .headers(headerBuilders.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                options.onfail.on(e, weakObject);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                System.out.println("onResponse:" + string);
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
