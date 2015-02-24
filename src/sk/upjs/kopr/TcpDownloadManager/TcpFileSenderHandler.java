
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
    private final int chunkSize;
    
    public TcpFileSenderHandler(Socket connectionSocket, File subor, int chunkSize) {
        this.connectionSocket = connectionSocket;
        this.subor = subor;
        this.chunkSize = chunkSize;
    }

    @Override
    public Boolean call() throws Exception {
            DataInputStream in = new DataInputStream(connectionSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());

            Integer zaciatok = in.readInt();//kde mam zacat posielat
            while (!zaciatok.equals(Klient.POISON_PILL)) {
                byte[] data = new byte[chunkSize];
                RandomAccessFile raf = new RandomAccessFile(subor, "r");
                raf.seek(zaciatok*Klient.CHUNK_SIZE);
                raf.read(data);
                raf.close();
                out.write(data);
                out.flush();
                zaciatok = in.readInt();
            }
            connectionSocket.close();
            return true;
    }
}
