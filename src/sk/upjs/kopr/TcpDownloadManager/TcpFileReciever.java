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

    private final ConcurrentLinkedDeque<Long> castiSuborovNaOdoslanie;
    private final File subor;
    private final long velkostSuboru;

    public TcpFileReciever(int poradie, ConcurrentLinkedDeque<Long> castiSuborovNaOdoslanie, 
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

        Long castNaOdoslanie = castiSuborovNaOdoslanie.pollFirst();
        
        while (!castNaOdoslanie.equals(Klient.POISON_PILL)) {
            //overenie, ci to nie je posledny chunk
            int poslednyChunkSize = (int)(castNaOdoslanie % Klient.CHUNK_SIZE);
            
            byte[] data;
            
            if(poslednyChunkSize != 0){
                System.err.println("posledny!!!");
                castNaOdoslanie = velkostSuboru-poslednyChunkSize;
                out.writeLong(castNaOdoslanie);
                out.writeInt(poslednyChunkSize);
                out.flush();
                data = new byte[poslednyChunkSize];
                in.read(data, 0, poslednyChunkSize);
                
            }else{
                out.writeLong(castNaOdoslanie);
                out.writeInt(Klient.CHUNK_SIZE);
                out.flush();
                data = new byte[Klient.CHUNK_SIZE];
                int read = in.read(data, 0, Klient.CHUNK_SIZE);
                if(read != Klient.CHUNK_SIZE){
                    System.err.println(castNaOdoslanie + " " +read);
                    castiSuborovNaOdoslanie.offerFirst(castNaOdoslanie);
                    castNaOdoslanie = castiSuborovNaOdoslanie.pollFirst();
                    continue;
                }
            }
            
            RandomAccessFile raf = new RandomAccessFile(subor, "rw");
            raf.seek(castNaOdoslanie);
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
