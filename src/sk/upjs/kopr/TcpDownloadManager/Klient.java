package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Klient {

    private final String subor;
    private final String destinationPath;
    private final int pocetSoketov;
    private Future[] future;
    private AtomicInteger uspesneSokety = new AtomicInteger(0);
    private ArrayBlockingQueue<Long> castiSuborovNaPoslanie;
    private long VelkostSuboru;
    private long poslednySize;
    protected static final Long POISON_PILL = -1l;
    protected static final int CHUNK_SIZE = 1000;

    public Klient(String subor, String destinationPath, int pocetSoketov) {
        this.subor = subor;
        this.destinationPath = destinationPath;
        this.pocetSoketov = pocetSoketov;
        spusti();
    }

    private void spusti() {
        System.out.println("klient: \t START");

        Socket clientSocket = null;
        try { //http://www.tutorialspoint.com/java/java_networking.htm
            clientSocket = new Socket("localhost", 1234);
            System.out.println("Connecting to " + "localhost"
                    + " on port " + 1234);
            System.out.println("Just connected to "
                    + clientSocket.getRemoteSocketAddress());
            OutputStream outToServer = clientSocket.getOutputStream();
            DataOutputStream out
                    = new DataOutputStream(outToServer);

            out.writeUTF("Hello from " + clientSocket.getLocalSocketAddress());
            out.writeUTF(subor);
            InputStream inFromServer = clientSocket.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            System.out.println(in.readUTF());//subor mam a posielam

            VelkostSuboru = in.readLong();
            System.out.println("velkost suboru je: " + VelkostSuboru);

            castiSuborovNaPoslanie = new ArrayBlockingQueue(pocetSoketov);

            ExecutorService executorService = Executors.newFixedThreadPool(pocetSoketov);
            future = new Future[pocetSoketov];
            System.out.println("uspesne sokety na zaciatku " + uspesneSokety);
            File cielovySubor = new File(destinationPath);
            cielovySubor.createNewFile();
            System.out.println("cesta k suboru: " + destinationPath);
            RandomAccessFile raf = new RandomAccessFile(cielovySubor, "rw");
            raf.setLength(VelkostSuboru);

            
            // vytvaranie  tcpFileReceiverov
            for (int i = 0; i < pocetSoketov; i++) {
                TcpFileReciever tcpFileReciever = 
                        new TcpFileReciever(i, uspesneSokety, castiSuborovNaPoslanie, destinationPath, raf, VelkostSuboru);
                future[i] = executorService.submit(tcpFileReciever);
            }

            //delenie suboru
            long i;
            for (i = 0l; i < VelkostSuboru-CHUNK_SIZE; i = i + CHUNK_SIZE) {
                castiSuborovNaPoslanie.offer(i,120,TimeUnit.SECONDS);
                //System.out.println("offerujem cast suboru " + i);
            }
            //pridanie posledneho chunku
            castiSuborovNaPoslanie.offer(i+(VelkostSuboru % CHUNK_SIZE), 120, TimeUnit.SECONDS);

            for (long j = 0; j < pocetSoketov; j++) {
                castiSuborovNaPoslanie.offer(POISON_PILL,120,TimeUnit.SECONDS);
                System.out.println("offerujem Poison_Pill " + j);
            }
            
            for (int j = 0; j < pocetSoketov; j++) {
                future[j].get();
            }
            System.err.println("vsetci klienti skoncili");

            //clientSocket.close();
        } catch (IOException ex) {
            System.err.println("klient nevie vytvorit spojenie");
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (ExecutionException ex) {
            ex.printStackTrace();
            ex.getCause();
        }
    }
}
