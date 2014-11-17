package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

public class TcpFileReciever implements Callable<Boolean> {

    private int poradie;

    public TcpFileReciever(int poradie) {
        this.poradie = poradie;
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

        return true;
    }
}
