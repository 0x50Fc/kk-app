package cn.kkmofang.app;

import java.util.LinkedList;
import java.util.List;

import cn.kkmofang.http.HttpOptions;

/**
 * Created by hailong11 on 2018/5/17.
 */

public class Protocol {

    public interface OpenApplication {
        void open(Application app);
    }

    public interface Http {
        void httpOptions(Application app,HttpOptions options,Object weakObject);
    }

    private List<Http> _https = new LinkedList<>();
    private List<OpenApplication> _openApplications = new LinkedList<>();

    public void addOpenApplication(OpenApplication openApplication) {
        _openApplications.add(openApplication);
    }

    public void addHttp(Http http) {
        _https.add(http);
    }

    public void openApplication(Application app) {

        for(OpenApplication v : _openApplications) {
            v.open(app);
        }

    }

    public void httpOptions(Application app,HttpOptions options,Object weakObject) {

        for(Http v : _https) {
            v.httpOptions(app,options,weakObject);
        }
    }

    public final static Protocol main = new Protocol();

}
