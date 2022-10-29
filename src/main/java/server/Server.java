package server;

import handler.Handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    public static final int PORT = 9999;
    private final ServerSocket serverSocket;
    private ConcurrentHashMap<String, Map<String, Handler>> handlers;
    public static List<String> validPaths = List.of("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css", "/app.js",
            "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final ExecutorService executorService = Executors.newFixedThreadPool(64);

    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
        handlers = new ConcurrentHashMap<>();
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.printf("Открой в браузере http://localhost:%d/\n", PORT);
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                executorService.execute(new MonoThreadClientHandler(socket, handlers));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                executorService.shutdown();
            }
        }
    }

    public void addHandler(String request, String msg, Handler handler) {
        //код
        if (handlers.containsKey(request))
            handlers.put(request, new HashMap<>());
        handlers.get(msg).put(msg, handler);
    }
}


