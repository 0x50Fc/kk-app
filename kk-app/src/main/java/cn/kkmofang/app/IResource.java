package cn.kkmofang.app;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhanghailong on 2018/3/13.
 */

public interface IResource {

    String getString(String name);

    InputStream open(String name) throws IOException;

}
