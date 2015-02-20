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
import java.util.concurrent.Callable;
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
    private ArrayBlockingQueue<Long> castiSuborovNaPoslanie;
    private long VelkostSuboru;
    protected static final Long POISON_PILL = -1l;
    protected static final int CHUNK_SIZE = 100000;
    protected static final CopyOnWriteArrayList<Long> prisli = new CopyOnWriteArrayList<>();
    protected static AtomicLong[] percenta = new AtomicLong[1];
    private final Exchanger exchanger;

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
            InputStream inFromServer = clientSocket.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            System.out.println(in.readUTF());//subor mam a posielam

            VelkostSuboru = in.readLong();
            //System.out.println("velkost suboru je: " + VelkostSuboru);
            
            int pocetChunkov = (int)(VelkostSuboru/CHUNK_SIZE)+1;
            
            castiSuborovNaPoslanie = new ArrayBlockingQueue(pocetChunkov);

            ExecutorService executorService = Executors.newFixedThreadPool(pocetSoketov);
            future = new Future[pocetSoketov];
            
            File cielovySubor = new File(destinationPath);
            cielovySubor.createNewFile();
            //System.out.println("cesta k suboru: " + destinationPath);
            RandomAccessFile raf = new RandomAccessFile(cielovySubor, "rw");
            raf.setLength(VelkostSuboru);
            raf.close();
            //System.err.println("klient zavrel raf");
            
            // vytvaranie  tcpFileReceiverov
            for (int i = 0; i < pocetSoketov; i++) {
                TcpFileReciever tcpFileReciever = 
                        new TcpFileReciever(i, uspesneSokety, castiSuborovNaPoslanie, cielovySubor, VelkostSuboru);
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
                //System.out.println("offerujem Poison_Pill " + j);
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
                    //exchanger.exchange(percenta);
                    //System.out.println("refresh");
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
            
            //System.out.println("pocet: "+prisli.size());
            
            //Long[] pole = new Long[prisli.size()];
                    
            /*for (int k=0; k < prisli.size(); k++) {
                pole[k] = prisli.get(k);
            }*/
            
            //Arrays.sort(pole);
            
            //System.out.println(Arrays.toString(pole));
            
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
        
        return true;
    }
}
