package request;

import java.util.List;

public class Request {
    private String method;
    private String path;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getQueryParam(String name) {
        //todo дописать реализацию
        return null;
    }

    public List<String> getQueryParams() {
        //todo дописать реализацию
        return null;
    }


}
