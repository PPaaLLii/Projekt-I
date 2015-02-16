
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
            //System.out.println("Just connected to " + connectionSocket.getRemoteSocketAddress());
            DataInputStream in = new DataInputStream(connectionSocket.getInputStream());

            //System.out.println(in.readUTF());//hello from...

            DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());
            //out.writeUTF("nice to meet you ☺");

            //this.poradie = in.readInt();
            //System.out.println("moje poradie je " + poradie);
            
            
            Long zaciatok = in.readLong();//kde mam zacat posielat
            while (!zaciatok.equals(Klient.POISON_PILL)) {
                int chunksize = in.readInt();//chunksize
                //System.out.println("zaciatok " + zaciatok);
                //System.out.println("chunksize: " + chunksize);
                out.writeUTF("data tecu");
                
                byte[] data = new byte[chunksize];
                
                RandomAccessFile raf = new RandomAccessFile(subor, "r");
                raf.seek(zaciatok);
                raf.read(data);
                raf.close();
                System.err.println("close raf");
                out.write(data);
                out.flush();
                //System.out.println("posielam data");
                
                zaciatok = in.readLong();
            }
            //System.out.println(poradie + "spapal som poison pill a koncim");
            connectionSocket.close();
            return true;
    }
}
