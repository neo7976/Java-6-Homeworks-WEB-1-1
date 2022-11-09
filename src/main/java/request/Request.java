package request;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Request {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private String method;
    private String path;
    private List<String> headers;
    private List<NameValuePair> queryParams;
    private List<NameValuePair> postParams;


    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public Request(String method, String path, List<String> headers, List<NameValuePair> queryParams, List<NameValuePair> postParams) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.queryParams = queryParams;
        this.postParams = postParams;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return getQueryParams().stream()
                .filter(x -> x.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
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

        //заполняем headers
        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        List<NameValuePair> queryParams = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);

        List<NameValuePair> postParams = new ArrayList<>();

        final var contentTypeHeader = extractHeader(headers, "Content-Type");
        if (!method.equals(GET)) {
            if (contentTypeHeader.get().equals("application/x-www-form-urlencoded")) {
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader(headers, "Content-Length");

                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);
                    final var body = new String(bodyBytes);
                    //todo выводим проверку для дальнейшего написания
                    System.out.println("ПРОВЕРЯЕМ ТЕЛО:" + body);
                }
                //todo нужно распарсить body

            }
        }


        return new Request(method, path, headers, queryParams, postParams);
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

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public List<String> getHeaders() {
        return headers;
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
