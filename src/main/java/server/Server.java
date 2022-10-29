package server;

import handler.Handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    public static final int PORT = 9999;
    private final ServerSocket serverSocket;
    public static List<String> validPaths = List.of("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css", "/app.js",
            "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final ExecutorService executorService = Executors.newFixedThreadPool(64);

    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.printf("Открой в браузере http://localhost:%d/\n", PORT);
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                executorService.execute(new MonoThreadClientHandler(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addHandler(String request, String msg, Handler handler) {
        //код
    }
}


