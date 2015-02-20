package sk.upjs.kopr.TcpDownloadManager;


import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpFileSender implements Callable<Boolean> {
    
    private final File subor;

    public TcpFileSender(File subor) {
        this.subor = subor;
    }

    @Override
    public Boolean call() throws Exception {
        ServerSocket serverSocket = new ServerSocket(1235);
        while (true) {
            Socket connectionSocket = serverSocket.accept();
            TcpFileSenderHandler tfsh = new TcpFileSenderHandler(connectionSocket, subor);
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(tfsh);
        }
    }
}
