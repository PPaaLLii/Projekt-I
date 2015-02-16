
package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.Callable;

public class TcpFileSenderHandler implements Callable<Boolean> {

    private final File subor;
    
    private final Socket connectionSocket;
    private int poradie;
    
    public TcpFileSenderHandler(Socket connectionSocket, File subor) {
        this.connectionSocket = connectionSocket;
        this.subor = subor;
    }

    @Override
    public Boolean call() throws Exception {
            DataInputStream in = new DataInputStream(connectionSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());

            Long zaciatok = in.readLong();//kde mam zacat posielat
            while (!zaciatok.equals(Klient.POISON_PILL)) {
                int chunksize = in.readInt();//chunksize
                byte[] data = new byte[chunksize];
                RandomAccessFile raf = new RandomAccessFile(subor, "r");
                raf.seek(zaciatok);
                raf.read(data);
                raf.close();
                //System.err.println("close raf");
                out.write(data);
                //out.flush();
                zaciatok = in.readLong();
            }
            connectionSocket.close();
            return true;
    }
}
