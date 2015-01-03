package sk.upjs.kopr.TcpDownloadManager;


import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpFileSender implements Callable<Boolean> {
    
    private final File subor;

//    private int poradie;
    
    

    public TcpFileSender(File subor) {
        this.subor = subor;
    }

    @Override
    public Boolean call() throws Exception {
        ServerSocket serverSocket = new ServerSocket(1235);
        //Socket connectionSocket = serverSocket.accept();
        while (true) {
            Socket connectionSocket = serverSocket.accept();
            TcpFileSenderHandler tfsh = new TcpFileSenderHandler(connectionSocket, subor);
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(tfsh);
        }
        /*
        System.out.println("Just connected to " + connectionSocket.getRemoteSocketAddress());
        DataInputStream in = new DataInputStream(connectionSocket.getInputStream());
        
        System.out.println(in.readUTF());//hello from...
        
        DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());
        out.writeUTF("nice to meet you ☺");
        
        this.poradie = in.readInt();
        System.out.println("moje poradie je " + poradie);
        
        long zaciatok = in.readLong();//kde mam zacat posielat
        int chunksize = in.readInt();//chunksize
        System.out.println("zaciatok " + zaciatok);
        System.out.println("chunksize: " + chunksize);
        out.write("data tecu".getBytes());
        */
    }
    //return true;
}
