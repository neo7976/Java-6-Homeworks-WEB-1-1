package request;

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

//    public String setMethod(String method) {
//        if (method != null && method.isBlank())
//            return method;
//        return null;
//    }
}
