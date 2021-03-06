package sk.upjs.kopr.TcpDownloadManager;

import java.util.Arrays;
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

    private final ConcurrentLinkedDeque<Integer> castiSuborovNaOdoslanie;
    private final File subor;
    private final long velkostSuboru;

    public TcpFileReciever(int poradie, ConcurrentLinkedDeque<Integer> castiSuborovNaOdoslanie, 
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

        int castNaOdoslanie = castiSuborovNaOdoslanie.pollFirst();
        byte[] data;
        data = new byte[Klient.CHUNK_SIZE];

        try {
            while (castNaOdoslanie != (Klient.POISON_PILL)) {

                out.writeInt(castNaOdoslanie);
                //out.writeInt(Klient.CHUNK_SIZE);
                out.flush();
                int read = in.read(data, 0, Klient.CHUNK_SIZE);
                if (read != Klient.CHUNK_SIZE) {
                    System.err.println(castNaOdoslanie + " " + read);
                    castiSuborovNaOdoslanie.offerFirst(castNaOdoslanie);
                    castNaOdoslanie = castiSuborovNaOdoslanie.pollFirst();
                    continue;
                    //}
                }

                RandomAccessFile raf = new RandomAccessFile(subor, "rw");
                raf.seek(castNaOdoslanie * Klient.CHUNK_SIZE);

                if ((int) (velkostSuboru / Klient.CHUNK_SIZE) == castNaOdoslanie) {
                    //zmensi posledny chunk
                    data = Arrays.copyOf(data, (int) velkostSuboru % Klient.CHUNK_SIZE);
                    System.out.println("posledny chunk!!!");
                }
                raf.write(data);
                raf.close();
                Klient.uspesneSokety.incrementAndGet();
                Klient.poslat[castNaOdoslanie] = false;
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("receiver interrupted");
                    clientSocket.close();
                    throw new InterruptedException();
                }
                castNaOdoslanie = castiSuborovNaOdoslanie.pollFirst();
            }
        }catch(InterruptedException e){
            System.out.println("receiver interrupted");
            clientSocket.close();
            throw new InterruptedException();
        }
        out.writeLong(Klient.POISON_PILL);
        out.flush();
        System.out.println("spapal som POISON PILL");
        clientSocket.close();
        return true;
    }
}
