package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TcpFileReciever implements Callable<Boolean> {

    private final ConcurrentLinkedDeque<Integer> castiSuborovNaOdoslanie;
    private final File subor;
    private final long velkostSuboru;

    public TcpFileReciever(int poradie, ConcurrentLinkedDeque<Integer> castiSuborovNaOdoslanie, 
            File subor, long velkostSuboru) {
        this.castiSuborovNaOdoslanie = castiSuborovNaOdoslanie;
        this.subor = subor;
        this.velkostSuboru = velkostSuboru;
    }

    @Override
    public Boolean call() throws Exception {
        Socket clientSocket = new Socket("localhost", 1235);
        OutputStream outToServer = clientSocket.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
        InputStream inFromServer = clientSocket.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);

        int castNaOdoslanie = castiSuborovNaOdoslanie.pollFirst();
        byte[] data;
        data = new byte[Klient.CHUNK_SIZE];
        
        while (castNaOdoslanie != (Klient.POISON_PILL)) {
            
            
            /*if(castNaOdoslanie == Klient.POSLEDNY){
                System.err.println("posledny!!!");
                castNaOdoslanie = ((int)velkostSuboru-poslednyChunkSize)/Klient.CHUNK_SIZE;
                out.writeInt(castNaOdoslanie);
                //out.writeInt(poslednyChunkSize);
                out.flush();
                data = new byte[poslednyChunkSize];
                in.read(data, 0, poslednyChunkSize);
             */
            //}else{
                out.writeInt(castNaOdoslanie);
                //out.writeInt(Klient.CHUNK_SIZE);
                out.flush();
                int read = in.read(data, 0, Klient.CHUNK_SIZE);
                if(read != Klient.CHUNK_SIZE && read != (velkostSuboru % Klient.CHUNK_SIZE)){
                    System.err.println(castNaOdoslanie + " " + read);
                    castiSuborovNaOdoslanie.offerFirst(castNaOdoslanie);
                    castNaOdoslanie = castiSuborovNaOdoslanie.pollFirst();
                    continue;
                //}
            }
            
            RandomAccessFile raf = new RandomAccessFile(subor, "rw");
            raf.seek(castNaOdoslanie*Klient.CHUNK_SIZE);
            raf.write(data);
            raf.close();
            Klient.uspesneSokety.incrementAndGet();
            castNaOdoslanie = castiSuborovNaOdoslanie.pollFirst();
        }
        out.writeLong(Klient.POISON_PILL);
        out.flush();
        System.out.println("spapal som POISON PILL");
        clientSocket.close();
        return true;
    }
}
