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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
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

public class Klient implements Callable<Map<String, String>> {

    private long VelkostSuboru;
    private int pocetChunkov;
    private final Exchanger exchanger;
    private final String subor;
    private final String destinationPath;
    private final int pocetSoketov;
    private ExecutorService executorService;
    private Future[] future;
    private ConcurrentLinkedDeque<Integer> castiSuborovNaPoslanie;
    private File cielovySubor;
    private boolean obnovit = false;
    private Map<String, String> mapa;
    
    protected static final Integer POISON_PILL = -1;
    protected static final Integer POSLEDNY = -2;
    protected static final int CHUNK_SIZE = 10000;
    protected static final CopyOnWriteArrayList<Long> prisli = new CopyOnWriteArrayList<>();
    protected static AtomicLong[] percenta = new AtomicLong[1];
    protected static AtomicInteger uspesneSokety = new AtomicInteger(0);
    protected static boolean[] poslat;

    public Klient(String subor, String destinationPath, int pocetSoketov, Exchanger exchanger, Map<String, String> mapa) {
        this.subor = subor;
        this.destinationPath = destinationPath;
        this.pocetSoketov = pocetSoketov;
        percenta[0] = new AtomicLong(0);
        this.exchanger = exchanger;
        this.mapa = mapa;
    }

    @Override
    public Map<String, String> call() throws InterruptedException {
        System.out.println("klient: \t START");
        
        inicializujSpojenieSoServerom();
        
        inicializujPremenne();
        
        nacitajStav();
        
        vytvorSubor();
        
        rozdelSuborNaCasti();
        
        vytvorRecieverov();
        
        try{
            updatujProgressBar();
        }catch(InterruptedException e){
            Map<String, String> mapa = prataj(true);
            return mapa;
        }
        
        Map<String, String> mapa = prataj(false);
        return mapa;
    }

    /**
     * http://www.tutorialspoint.com/java/java_networking.htm
     */
    private void inicializujSpojenieSoServerom() {
        Socket clientSocket = null;
        try {
            clientSocket = new Socket("localhost", 1234);
            System.out.println("Connecting to " + "localhost" + " on port " + 1234);
            System.out.println("Just connected to " + clientSocket.getRemoteSocketAddress());
            OutputStream outToServer = clientSocket.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF("Hello from " + clientSocket.getLocalSocketAddress());
            out.writeUTF(subor);
            out.writeInt(Klient.CHUNK_SIZE);
            InputStream inFromServer = clientSocket.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            System.out.println(in.readUTF());//subor mam a posielam
            VelkostSuboru = in.readLong();
            System.out.println("velkost suboru je: " + VelkostSuboru);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void inicializujPremenne(){
        System.out.println("inicializujem premenne");
        pocetChunkov = (int)(VelkostSuboru/CHUNK_SIZE)+1;
            //System.err.println("pocet chunkov: " + pocetChunkov);
            
            castiSuborovNaPoslanie = new ConcurrentLinkedDeque();
            
            poslat = new boolean[pocetChunkov];
            for (int i = 0; i < poslat.length; i++) {
                poslat[i] = true;
            }

            executorService = Executors.newFixedThreadPool(pocetSoketov);
            future = new Future[pocetSoketov];
    }
    
    private void nacitajStav() {
        System.out.println("nacitavam stav");
        if (Boolean.valueOf(mapa.get("treba"))) {
            obnovit = true;
            uspesneSokety = new AtomicInteger(Integer.parseInt(mapa.get("uspesneSokety")));
            String posli = mapa.get("posli");
            Scanner citac = new Scanner(posli);
            while (citac.hasNextInt()) {
                int castNaOdoslanie = citac.nextInt();
                castiSuborovNaPoslanie.offerFirst(castNaOdoslanie);
                //System.out.println("offerujem" + castNaOdoslanie);
            }
        }
    }
    
    private void vytvorSubor() {
        System.out.println("vytvaram subor");
        try {
            cielovySubor = new File(destinationPath);
            cielovySubor.createNewFile();
            RandomAccessFile raf = new RandomAccessFile(cielovySubor, "rw");
            raf.setLength(VelkostSuboru);
            raf.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void rozdelSuborNaCasti() {
        System.out.println("rozdelujem subor na casti");
        if (!obnovit) {
            for (int i = 0; i < pocetChunkov; i = i + 1) {
                castiSuborovNaPoslanie.offerLast((int) i);
                //System.err.println("offerujem cast suboru " + i);
            }
        }
        System.out.println("offerujem poison pill");
        for (long j = 0; j < pocetSoketov; j++) {
            castiSuborovNaPoslanie.offerLast(POISON_PILL);
            //System.out.println("offerujem Poison_Pill " + j);
        }
    }
    
    private void vytvorRecieverov() {
        // vytvaranie  tcpFileReceiverov
        System.out.println("vytvaram recieverov");
        for (int k = 0; k < pocetSoketov; k++) {
            TcpFileReciever tcpFileReciever =
                    new TcpFileReciever(k, castiSuborovNaPoslanie, cielovySubor, VelkostSuboru);
            future[k] = executorService.submit(tcpFileReciever);
        }
    }
    
    private void updatujProgressBar() throws InterruptedException {
        System.out.println("updatujem progressbar");
        int percenta = 0;
        
        //System.out.println("pocetchunkov je: " + pocetChunkov);
        while(percenta != 100){
            if(Thread.currentThread().isInterrupted()){
                System.out.println("klient interrupted");
                executorService.shutdownNow();
            }
            double percentage = uspesneSokety.doubleValue()/pocetChunkov;
            percenta = (int)(percentage*100);
            try{
                exchanger.exchange(percenta,5000,TimeUnit.MILLISECONDS);
            }catch(InterruptedException e){
                System.err.println("exchanger skoncil");
                throw new InterruptedException();
            }catch(TimeoutException e){
                System.err.println("timeout!!!");
            }
        }
    }
    
    private void skonciAUloz() {
        try {
            for (int j = 0; j < pocetSoketov; j++) {
                future[j].get();

            }
            System.err.println("vsetci klienti skoncili");
            ZapisTrebaObnovit(false);
        }catch(InterruptedException e){
            e.printStackTrace();
        }catch(ExecutionException e){
            e.printStackTrace();
        }
    }

    private void ZapisTrebaObnovit(boolean treba){
        PrintWriter pw = null;
        try {
            pw = new PrintWriter("posli.txt");
            pw.println(treba);
            System.out.println("zapisane");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally{
            pw.close();
        }
    }
    
    private Map<String, String> prataj(boolean prerusene) {
        System.out.println("klient ide pratat");
        Map<String, String> mapa = new HashMap<>();
        if(prerusene){
            mapa.put("prerusene", "true");
            mapa.put("uspesneSokety", String.valueOf(uspesneSokety.get()));
            StringBuilder posli = new StringBuilder();
            
            for(int i = 0; i < poslat.length; i++){
                if(poslat[i]){
                    posli.append(String.valueOf(i));
                    posli.append(" ");
                }
            }
            
            mapa.put("posli", posli.toString());
            
        }else{
            mapa.put("prerusene", "false");
        }
        System.out.println("klient before return");
        return mapa;
    }
}
