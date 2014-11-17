package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class TcpFileSender implements Callable<Boolean> {

    private int poradie;
    private CountDownLatch countDownLatch;

    @Override
    public Boolean call() throws Exception {
        ServerSocket serverSocket = new ServerSocket(1235);
        while (true) {
            Socket connectionSocket = serverSocket.accept();
            System.out.println("Just connected to " + connectionSocket.getRemoteSocketAddress());
            DataInputStream in = new DataInputStream(connectionSocket.getInputStream());

            System.out.println(in.readUTF());//hello from...

            DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());
            out.writeUTF("nice to meet you ☺");

            this.poradie = in.readInt();
            System.out.println("moje poradie je " + poradie);
        }
    }
    //return true;
}
