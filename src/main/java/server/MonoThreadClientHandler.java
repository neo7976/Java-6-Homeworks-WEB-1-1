package server;

import handler.Handler;
import request.Request;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonoThreadClientHandler implements Runnable {
    private Socket socket;
    private ConcurrentHashMap<String, Map<String, Handler>> handlers;

    public MonoThreadClientHandler(Socket socket, ConcurrentHashMap<String, Map<String, Handler>> handlers) {
        this.socket = socket;
        this.handlers = handlers;
    }

    @Override
    public void run() {
            try (
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())){

                Request request = Request.requestBuild(in);
                if (request == null || !handlers.containsKey(request.getMethod())) {
                    responseLack(out, "404", "Request Not Found");
//                    continue;
                    return;
                }
                else
                    printRequestDebug(request);

                Map<String, Handler> handlerMap = handlers.get(request.getMethod());
                String requestPath = request.getPath();
                if (handlerMap.containsKey(requestPath)) {
                    Handler handler = handlerMap.get(requestPath);
                    handler.handle(request, out);
                } else {
                    if (!Server.validPaths.contains(requestPath)) {
                        responseLack(out, "404", "Not Found");
//                        continue;
                    } else {
                        Path filePath = Path.of(".", "public", requestPath);
                        String mimeType = Files.probeContentType(filePath);

                        // special case for classic
                        if (requestPath.equals("/classic.html")) {
                            String template = Files.readString(filePath);
                            byte[] content = template.replace(
                                    "{time}",
                                    LocalDateTime.now().toString()
                            ).getBytes();
                            out.write((
                                    "HTTP/1.1 200 OK\r\n" +
                                            "Content-Type: " + mimeType + "\r\n" +
                                            "Content-Length: " + content.length + "\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                            ).getBytes());
                            out.write(content);
                            out.flush();
//                            continue;
                        }

                        long length = Files.size(filePath);
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        Files.copy(filePath, out);
                        out.flush();
                    }
                }

            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }


    private void responseLack(BufferedOutputStream out, String responseCode, String responseMsg) throws IOException {
        out.write((
                "HTTP/1.1 " + responseCode + " " + responseMsg + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void printRequestDebug(Request request) {
        System.out.println("Request debug information: ");
        System.out.println("METHOD: " + request.getMethod());
        System.out.println("PATH: " + request.getPath());
        System.out.println("---HEADERS:---Начало---");
        for (String header : request.getHeaders()) {
            System.out.println(header);
        }
        System.out.println("---HEADERS:---Конец---");
        System.out.println("BODY: " + request.getQueryParams());
        System.out.println("BODY Test login: " + request.getQueryParam("login"));
        System.out.println("BODY Test password: " + request.getQueryParam("password"));

        System.out.println("Test POST: " + request.getPostParams());
        System.out.println("Test POST \"title\": " + request.getPostParam("title"));
        System.out.println("Test POST \"value\": " + request.getPostParam("value"));
        System.out.println("Test POST \"image\": " + request.getPostParam("image"));

    }
}
