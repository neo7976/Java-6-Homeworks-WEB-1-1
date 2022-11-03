package request;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Request {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private String method;
    private String path;
    public List<String> headers;
    //todo переименовать <...> и название переменной списка
    public List<NameValuePair> body;


    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public Request(String method, String path, List<String> headers, List<NameValuePair> body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
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

    public static Request requestBuild(BufferedInputStream in) throws IOException, URISyntaxException {
        final var allowedMethods = List.of(GET, POST);

            final var limit = 4096;
            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);

            //ищем запрос
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                return null;
            }

            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3)
                return null;

            final var method = requestLine[0];
            if (!allowedMethods.contains(method))
                return null;

            //todo если что, проверить это место
            final var path = requestLine[1];
            if (!path.startsWith("/"))
                return null;

            // ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                return null;
            }

            // отматываем на начало буфера
            in.reset();
            // пропускаем requestLine
            in.skip(headersStart);

            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));

            // todo Прочитать <!-- https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient -->
            List<NameValuePair> body = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);

            return new Request(method, path, headers, body);
    }


    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

//    private static void badRequest(BufferedOutputStream out) throws IOException {
//        out.write((
//                "HTTP/1.1 400 Bad Request\r\n" +
//                        "Content-Length: 0\r\n" +
//                        "Connection: close\r\n" +
//                        "\r\n"
//        ).getBytes());
//        out.flush();
//    }


}
