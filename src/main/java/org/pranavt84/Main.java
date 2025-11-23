package org.pranavt84;

import org.pranavt84.controller.RestServer;
import org.pranavt84.service.DataInputService;
import org.pranavt84.service.ServiceFactory;

import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException {
        int port = 8080;

        if (args != null && args.length == 1) {
            port = Integer.parseInt(args[0]);
            System.out.println("Starting server on port : " + port);
        } else if(args != null && args.length != 0) {
            System.out.println("Invalid input. You are allowed to pass only single command line argument, which is port.");
        } else {
            System.out.println("Starting server on default port : " + port);
        }

        DataInputService service = ServiceFactory.createService();
        RestServer server = new RestServer(port, service);
        server.start();
    }
}
