package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

public class TcpFileSender implements Callable<Boolean> {

    private int poradie;

    @Override
    public Boolean call() throws Exception {
        ServerSocket serverSocket = new ServerSocket(1235);
        //Socket connectionSocket = serverSocket.accept();
        while (true) {
            Socket connectionSocket = serverSocket.accept();
            System.out.println("Just connected to " + connectionSocket.getRemoteSocketAddress());
            DataInputStream in = new DataInputStream(connectionSocket.getInputStream());

            System.out.println(in.readUTF());//hello from...

            DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());
            out.writeUTF("nice to meet you ☺");

            this.poradie = in.readInt();
            System.out.println("moje poradie je " + poradie);
            
            long zaciatok = in.readLong();//kde mam zacat posielat
            int chunksize = in.readInt();//chunksize
            System.out.println("zaciatok " + zaciatok);
            System.out.println("chunksize: " + chunksize);
            out.write("data tecu".getBytes());
        }
    }
    //return true;
}
