/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server {

    public static void main(String[] args) {
        System.out.println("Server: \t START");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(1234);
            while (true) { // http://www.tutorialspoint.com/java/java_networking.htm
                Socket connectionSocket = serverSocket.accept();
                System.out.println("Just connected to " + connectionSocket.getRemoteSocketAddress());
                DataInputStream in = new DataInputStream(connectionSocket.getInputStream());
                System.out.println(in.readUTF());
                File subor = new File(in.readUTF());
                DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());
                out.writeUTF("subor mam a posielam");

                ExecutorService executorService = Executors.newSingleThreadExecutor();

                TcpFileSender tcpFileSender = new TcpFileSender();
                Future future = executorService.submit(tcpFileSender);

                out.writeUTF("napoj sa na serversoket na port 1235");

                //////////////////////////////////////////////////////////////////////
                DataOutputStream out1
                        = new DataOutputStream(connectionSocket.getOutputStream());
                out1.writeUTF("Thank you for connecting to "
                        + connectionSocket.getLocalSocketAddress() + "\nGoodbye!");
            }
        } catch (IOException ex) {
            System.err.println("server soket sa nepodarilo otvorit");
            ex.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    System.err.println("server soket sa nepodarilo zavriet");
                }
            }
        }
    }
}
