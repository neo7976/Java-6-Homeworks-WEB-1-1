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
            try {
//                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
//                String requestLine = in.readLine();
//                String[] parts = requestLine.split(" ");

//                if (parts.length != 3) {
//                    // just close socket
//                    socket.close();
////                    continue;
//                }

                //todo Переписать под новый запрос
//                Request request = new Request(parts[0], parts[1]);
                Request request = Request.requestBuild(in);
                if (request.getMethod() == null || !handlers.containsKey(request.getMethod())) {
                    responseLack(out, "404", "Request Not Found");
//                    continue;
                }

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
}
