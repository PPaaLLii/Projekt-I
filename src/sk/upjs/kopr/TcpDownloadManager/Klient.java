
package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

class Klient {
    private final String subor;
    private final String destinationPath;
    private final int pocetSoketov;
    private Future[] future;
    private static AtomicInteger uspesneSokety = new AtomicInteger(0);
    
    public Klient(String subor, String destinationPath, int pocetSoketov){
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

            out.writeUTF("Hello from "
                    + clientSocket.getLocalSocketAddress());
            out.writeUTF(subor);
            InputStream inFromServer = clientSocket.getInputStream();
            DataInputStream in
                    = new DataInputStream(inFromServer);//subor mam a posielam
            System.out.println(in.readUTF());
            
            System.out.println(in.readUTF());
            
            ExecutorService executorService = Executors.newFixedThreadPool(pocetSoketov);
            future = new Future[pocetSoketov];
            System.out.println("uspesne sokety na zaciatku " + uspesneSokety);
            for (int i = 0; i < pocetSoketov; i++) {
                TcpFileReciever tcpFileReciever = new TcpFileReciever(i, uspesneSokety);
                future[i] = executorService.submit(tcpFileReciever);
            }
            for (int i = 0; i < pocetSoketov; i++) {
                future[i].get();
            }
            
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
