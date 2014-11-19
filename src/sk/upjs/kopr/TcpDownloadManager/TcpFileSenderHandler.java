/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

public class TcpFileSenderHandler implements Callable<Boolean> {

    private Socket connectionSocket;
    private int poradie;
    
    public TcpFileSenderHandler(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    @Override
    public Boolean call() throws Exception {
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
            
            
            
            connectionSocket.close();
            return true;
    }
}
