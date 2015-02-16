package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingWorker;

public class TcpFileReciever implements Callable<Boolean> {

    private int poradie;
    private AtomicInteger uspesneSokety;
    private ArrayBlockingQueue<Long> castiSuborovNaOdoslanie;
    private String destinationPath;
    private RandomAccessFile raf;
    private File subor;
    private long velkostSuboru;
    private SwingWorker sw;

    public TcpFileReciever(int poradie, AtomicInteger uspesneSokety, 
            ArrayBlockingQueue<Long> castiSuborovNaOdoslanie, 
            File subor, long velkostSuboru, SwingWorker sw) {
        this.poradie = poradie;
        this.uspesneSokety = uspesneSokety;
        this.castiSuborovNaOdoslanie = castiSuborovNaOdoslanie;
        this.subor = subor;
        this.velkostSuboru = velkostSuboru;
        this.sw = sw;
    }

    @Override
    public Boolean call() throws Exception {
        Socket clientSocket = new Socket("localhost", 1235);
        //System.out.println(poradie + " Connecting to " + "localhost" + " on port " + 1235);
        //System.out.println(poradie + " Just connected to " + clientSocket.getRemoteSocketAddress());
        OutputStream outToServer = clientSocket.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);

        //out.writeUTF("Hello from " + clientSocket.getLocalSocketAddress());

        InputStream inFromServer = clientSocket.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);
        
        //in.readUTF();
        //System.out.println(in.readUTF());//nice to meet you
        //out.writeInt(poradie);
        //uspesneSokety.incrementAndGet();

        Long castNaOdoslanie = castiSuborovNaOdoslanie.poll(10000, TimeUnit.MILLISECONDS);
        System.err.println(poradie + ": spapal som: " + castNaOdoslanie);
        
        while (!castNaOdoslanie.equals(Klient.POISON_PILL)) {//chrustaj z radu a posielaj serveru, nech ti to posle + cakaj na chunk 
            //overenie, ci to nie je posledny chunk
            int poslednyChunkSize = (int)(castNaOdoslanie % Klient.CHUNK_SIZE);
            if(poslednyChunkSize != 0){
                System.err.println("posledny!!!");
                out.writeLong(velkostSuboru-poslednyChunkSize);
                out.writeInt(poslednyChunkSize);
            }else{
                out.writeLong(castNaOdoslanie);
                out.writeInt(Klient.CHUNK_SIZE);
            }
            //System.out.println(poradie + ": poslal som cast na odoslanie a velkost chunku");
            in.readUTF();
            //System.out.println(in.readUTF());//data tecu
            //castNaOdoslanie = castiSuborovNaOdoslanie.poll(1000, TimeUnit.MILLISECONDS);
            //System.out.println(poradie + ": casti na odoslanie je: " + castNaOdoslanie);
            byte[] data;            
            int velkostDatCoPrisli;
            //kontrola posledneho chunku
            
            if(poslednyChunkSize == 0){//nie je posledny
                    data = new byte[Klient.CHUNK_SIZE];
                    velkostDatCoPrisli = in.read(data, 0, Klient.CHUNK_SIZE);
            }else{//je posledny
                    data = new byte[poslednyChunkSize];
                    velkostDatCoPrisli = in.read(data, 0, poslednyChunkSize);
                    castNaOdoslanie = velkostSuboru-poslednyChunkSize;
            }
            System.err.println("tuu");
            
            RandomAccessFile raf = new RandomAccessFile(subor, "rw");
            System.err.println("tuuuuuu");
            raf.seek(castNaOdoslanie);
            raf.write(data);
            raf.close();
            
            Klient.uspesneSokety.incrementAndGet();
            castNaOdoslanie = castiSuborovNaOdoslanie.poll(10000, TimeUnit.MILLISECONDS);
            System.err.println(".");
            //System.err.println(poradie + ": spapal som: " + castNaOdoslanie);
            
        }
        out.writeLong(Klient.POISON_PILL);
        //System.out.println("spapal som POISON PILL");
        raf.close();
        clientSocket.close();
        return true;
    }
}
