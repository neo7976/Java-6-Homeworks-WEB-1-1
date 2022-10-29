import handler.Handler;
import request.Request;
import server.Server;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();


        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
            }
        });
    }
}


