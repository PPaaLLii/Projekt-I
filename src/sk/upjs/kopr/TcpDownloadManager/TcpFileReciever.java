package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class TcpFileReciever implements Callable<Boolean> {

    private int poradie;
    private AtomicInteger uspesneSokety;

    public TcpFileReciever(int poradie, AtomicInteger uspesneSokety) {
        this.poradie = poradie;
        this.uspesneSokety = uspesneSokety;
    }

    @Override
    public Boolean call() throws Exception {
        Socket clientSocket = new Socket("localhost", 1235);
        System.out.println(poradie + " Connecting to " + "localhost" + " on port " + 1235);
        System.out.println(poradie +" Just connected to " + clientSocket.getRemoteSocketAddress());
        OutputStream outToServer = clientSocket.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
        
        out.writeUTF("Hello from " + clientSocket.getLocalSocketAddress());
        
        InputStream inFromServer = clientSocket.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);
        
        System.out.println(in.readUTF());//nice to meet you
        out.writeInt(poradie);
        uspesneSokety.incrementAndGet();

        return true;
    }
}
