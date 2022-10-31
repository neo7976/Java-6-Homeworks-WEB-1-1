package server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class MonoThreadClientHandler implements Runnable {
    private final Socket socket;

    public MonoThreadClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                String requestLine = in.readLine();
                String[] parts = requestLine.split(" ");

                if (parts.length != 3) {
                    // just close socket
//                    continue;
                }

                String path = parts[1];
                if (!Server.validPaths.contains(path)) {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
//                    continue;
                }
                Path filePath = Path.of(".", "public", path);
                String mimeType = Files.probeContentType(filePath);

                // special case for classic
                if (path.equals("/classic.html")) {
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
//                    continue;
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

