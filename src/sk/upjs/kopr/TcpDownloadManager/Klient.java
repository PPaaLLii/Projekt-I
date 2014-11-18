
package sk.upjs.kopr.TcpDownloadManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Klient {
    private final String subor;
    private final String destinationPath;
    private final int pocetSoketov;
    
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
            
            for (int i = 0; i < pocetSoketov; i++) {
                TcpFileReciever tcpFileReciever = new TcpFileReciever(i);
                executorService.submit(tcpFileReciever);
            }
            
            //clientSocket.close();
        } catch (IOException ex) {
            System.err.println("klient nevie vytvorit spojenie");
        } 
    }
}
