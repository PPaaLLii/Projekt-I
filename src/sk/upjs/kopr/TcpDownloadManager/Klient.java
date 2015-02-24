package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Klient implements Callable<Boolean> {

    private final String subor;
    private final String destinationPath;
    private final int pocetSoketov;
    private Future[] future;
    protected static AtomicInteger uspesneSokety = new AtomicInteger(0);
    private ConcurrentLinkedDeque<Integer> castiSuborovNaPoslanie;
    private long VelkostSuboru;
    protected static final Integer POISON_PILL = -1;
    protected static final Integer POSLEDNY = -2;
    protected static final int CHUNK_SIZE = 10000;
    protected static final CopyOnWriteArrayList<Long> prisli = new CopyOnWriteArrayList<>();
    protected static AtomicLong[] percenta = new AtomicLong[1];
    private final Exchanger exchanger;
    protected static boolean[] poslat;

    public Klient(String subor, String destinationPath, int pocetSoketov, Exchanger exchanger) {
        this.subor = subor;
        this.destinationPath = destinationPath;
        this.pocetSoketov = pocetSoketov;
        percenta[0] = new AtomicLong(0);
        this.exchanger = exchanger;
    }

    public Boolean call() {
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
            out.writeInt(Klient.CHUNK_SIZE);
            InputStream inFromServer = clientSocket.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            System.out.println(in.readUTF());//subor mam a posielam

            VelkostSuboru = in.readLong();
            System.out.println("velkost suboru je: " + VelkostSuboru);
            
            int pocetChunkov = (int)(VelkostSuboru/CHUNK_SIZE)+1;
            System.err.println("pocet chunkov: " + pocetChunkov);
            
            castiSuborovNaPoslanie = new ConcurrentLinkedDeque();
            
            poslat = new boolean[pocetChunkov];
            for (int i = 0; i < poslat.length; i++) {
                poslat[i] = true;
            }

            ExecutorService executorService = Executors.newFixedThreadPool(pocetSoketov);
            future = new Future[pocetSoketov];
            
            File cielovySubor = new File(destinationPath);
            cielovySubor.createNewFile();
            //System.out.println("cesta k suboru: " + destinationPath);
            RandomAccessFile raf = new RandomAccessFile(cielovySubor, "rw");
            raf.setLength(VelkostSuboru);
            raf.close();
            //System.err.println("klient zavrel raf");
            
            //delenie suboru
            int i;
            for (i = 0; i < pocetChunkov; i = i+1) {
                castiSuborovNaPoslanie.offerLast((int)i);
                //System.err.println("offerujem cast suboru " + i);
            }
            //pridanie posledneho chunku
            //castiSuborovNaPoslanie.offerLast(POSLEDNY);
            //System.out.println("offerujem posledny");

            for (long j = 0; j < pocetSoketov; j++) {
                castiSuborovNaPoslanie.offerLast(POISON_PILL);
                //System.out.println("offerujem Poison_Pill " + j);
            }
            
            // vytvaranie  tcpFileReceiverov
            for (int k = 0; k < pocetSoketov; k++) {
                TcpFileReciever tcpFileReciever = 
                        new TcpFileReciever(k, castiSuborovNaPoslanie, cielovySubor, VelkostSuboru);
                future[k] = executorService.submit(tcpFileReciever);
            }
            
            
            
            int percenta = 0;
            
            //System.out.println("pocetchunkov je: " + pocetChunkov);
            while(percenta != 100){
                //System.err.println("pocetchunkov je: " + pocetChunkov + "; uspesnesokety: " + uspesneSokety.longValue());
                double percentage = uspesneSokety.doubleValue()/pocetChunkov;
                //System.out.println(percentage);
                percenta = (int)(percentage*100);
                try{
                    exchanger.exchange(percenta,5000,TimeUnit.MILLISECONDS);
                }catch(InterruptedException e){
                    System.err.println("exchanger skoncil");
                }catch(TimeoutException e){
                    System.err.println("timeout!!!");
                }
            }
            
            for (int j = 0; j < pocetSoketov; j++) {
                future[j].get();
            }
            System.err.println("vsetci klienti skoncili");
            
            ZapisTrebaObnovit(false);
            
        } catch (IOException ex) {
            System.err.println("klient nevie vytvorit spojenie");
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (ExecutionException ex) {
            ex.printStackTrace();
            ex.getCause();
        } 
        
        return true;
    }

    private void ZapisTrebaObnovit(boolean treba){
        PrintWriter pw = null;
        try {
            File posli = new File("posli.txt");
            pw = new PrintWriter(posli);
            pw.print(treba);// netreba nacitavat
            System.out.println("zapisane false");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally{
            pw.close();
        }
    }
}
