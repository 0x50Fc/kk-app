package cn.kkmofang.app;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hailong11 on 2018/5/17.
 */

public class Protocol {

    public interface OpenApplication {
        void open(Application app);
    }

    private List<OpenApplication> _openApplications = new LinkedList<>();

    public void addOpenApplication(OpenApplication openApplication) {
        _openApplications.add(openApplication);
    }

    public void openApplication(Application app) {

        for(OpenApplication v : _openApplications) {
            v.open(app);
        }

    }

    public final static Protocol main = new Protocol();

}
