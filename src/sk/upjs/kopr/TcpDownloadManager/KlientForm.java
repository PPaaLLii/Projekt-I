package sk.upjs.kopr.TcpDownloadManager;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import static sk.upjs.kopr.TcpDownloadManager.Klient.poslat;

public class KlientForm extends javax.swing.JFrame {

    private final MyJFileChooser chooser1 = new MyJFileChooser();
    private final MyJFileChooser chooser2 = new MyJFileChooser();
    private String destinationPath = null;
    private String fullSourcePath = null;
    private String sourcePath = null;
    private Klient klient = null;
    private int pocetSoketov;
    private final Exchanger exchanger = new Exchanger();
    private boolean obnovit = false;
    private SwingWorker sw;
    private SwingWorker swTime;
    private Map<String, String> mapa;
    
    public KlientForm() {
        initComponents();
        refreshniObnovit();
        
        if(obnovit){
            btnPauseContinue.setText("Continue");
            btnDownload.setEnabled(false);
            btnSelectDestinationFolder.setEnabled(false);
            btnFileToDownload.setEnabled(false);
            txtSocketCount.setEditable(false);
            txtSocketCount.setText(String.valueOf(pocetSoketov));
        }else{
            btnSelectDestinationFolder.addActionListener(chooser1);
            btnFileToDownload.addActionListener(chooser2);
            btnPauseContinue.setVisible(false);
            btnStop.setVisible(false);
        }
        progressBar.setVisible(false);
        btnExit.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnSelectDestinationFolder = new javax.swing.JButton();
        lblSelectedDestination = new javax.swing.JLabel();
        btnDownload = new javax.swing.JButton();
        lblSocketCount = new javax.swing.JLabel();
        txtSocketCount = new javax.swing.JTextField();
        progressBar = new javax.swing.JProgressBar();
        btnPauseContinue = new javax.swing.JButton();
        lblProgress = new javax.swing.JLabel();
        btnFileToDownload = new javax.swing.JButton();
        lblTimeElapsed = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        lblSelectedFile = new javax.swing.JLabel();
        btnStop = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnSelectDestinationFolder.setText("Select Destination Folder");
        btnSelectDestinationFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectDestinationFolderActionPerformed(evt);
            }
        });

        lblSelectedDestination.setText("There is no file selected");

        btnDownload.setText("Download");
        btnDownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownloadActionPerformed(evt);
            }
        });

        lblSocketCount.setText("Socket count");

        txtSocketCount.setText("8");

        btnPauseContinue.setText("Pause");
        btnPauseContinue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPauseContinueActionPerformed(evt);
            }
        });

        btnFileToDownload.setText("Select file to download");
        btnFileToDownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFileToDownloadActionPerformed(evt);
            }
        });

        lblTimeElapsed.setText("Time elapsed in seconds");

        lblTime.setText("0");

        lblSelectedFile.setText("There is no file selected");

        btnStop.setText("Stop");
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTimeElapsed)
                        .addGap(62, 62, 62)
                        .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnPauseContinue)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnStop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnExit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDownload))
                    .addComponent(lblProgress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSelectDestinationFolder)
                            .addComponent(lblSocketCount)
                            .addComponent(btnFileToDownload, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSelectedDestination, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                            .addComponent(lblSelectedFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtSocketCount, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFileToDownload)
                    .addComponent(lblSelectedFile))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSelectedDestination)
                    .addComponent(btnSelectDestinationFolder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSocketCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSocketCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTimeElapsed)
                    .addComponent(lblTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDownload)
                    .addComponent(btnPauseContinue)
                    .addComponent(btnStop)
                    .addComponent(btnExit))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectDestinationFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectDestinationFolderActionPerformed
        destinationPath = chooser1.chooser.getSelectedFile().getAbsolutePath();
        lblSelectedDestination.setText(destinationPath);
    }//GEN-LAST:event_btnSelectDestinationFolderActionPerformed

    private void btnDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownloadActionPerformed
        SpustiStahovanie();
    }//GEN-LAST:event_btnDownloadActionPerformed

    private void SpustiStahovanie() throws NumberFormatException, HeadlessException {
        if(destinationPath == null){
            JOptionPane.showMessageDialog(this, "Musite vybrat priecinok, kde sa ma stiahnut zadany subor");
        }
        if(fullSourcePath == null || fullSourcePath.equals("Choose file")){
            JOptionPane.showMessageDialog(this, "Musite vybrat subor, ktory chcete stiahnut");
        }
        if(fullSourcePath != null && destinationPath != null){
            btnPauseContinue.setVisible(true);
            btnStop.setVisible(true);
            btnDownload.setVisible(false);
            progressBar.setVisible(true);
            progressBar.setMinimum(0);
            progressBar.setMaximum(100);
            progressBar.setValue(0);
            //lblProgress.setText("inicializuje sa spojenie so serverom");
            pocetSoketov = Integer.parseInt(txtSocketCount.getText());
            sw = new SwingWorker<Void, Integer>() {
                
                @Override
                protected Void doInBackground() throws Exception {
                    klient = new Klient(fullSourcePath, destinationPath + "\\" + sourcePath, pocetSoketov, exchanger, obnovit);
                    ExecutorService es = Executors.newSingleThreadExecutor();
                    Future future = es.submit(klient);
                    int i = 0;
                    while(i != 100){
                        try{
                            if(isCancelled()){
                                System.out.println("swDoInBackground cancelled");
                                es.shutdownNow();
                                break;
                                //System.out.println("do in background" + Thread.currentThread().isInterrupted());
                            }
                            i = (int)exchanger.exchange(null,5000,TimeUnit.MILLISECONDS);
                        }catch(InterruptedException e){
                            System.err.println("exchanger InterruptedException");
                        }
                        publish(i);
                        Thread.sleep(500);
                    }
                    //System.err.println("tuuuuuuuuuuuuuuu");
                    mapa = (Map<String, String>)future.get();
                    System.out.println("mam mapu");
                    return null;
                }
                
                @Override
                protected void process(List<Integer> chunks) {
                    progressBar.setValue(chunks.get(chunks.size()-1));
                }
                
                @Override
                protected void done() {
                    lblProgress.setText("Stahovanie dokoncene");
                    btnStop.setVisible(false);
                    btnPauseContinue.setVisible(false);
                    btnExit.setVisible(true);
                }
                
            };
            sw.execute();
            
            swTime = new SwingWorker<Void, String>(){
                
                @Override
                protected Void doInBackground() throws Exception {
                    long i = 0;
                    while(true){
                        publish(String.valueOf(i));
                        i++;
                        Thread.sleep(1000);
                    }
                    //return null;
                }
                
                @Override
                protected void process(List<String> chunks) {
                    lblTime.setText(chunks.get(chunks.size()-1));
                }

                @Override
                protected void done() {
                    try{
                        get();
                    }catch(CancellationException e){
                        System.out.println("cancel");
                    }catch(InterruptedException e){
                        System.out.println("swTimeInterruptedException");
                    }catch(ExecutionException e){
                        System.out.println("swTimeExecutionException");
                    }
                }
                
            };
            swTime.execute();
        }
    }

    private void btnFileToDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFileToDownloadActionPerformed
        fullSourcePath = chooser2.chooser.getSelectedFile().getAbsolutePath();
        sourcePath = chooser2.chooser.getSelectedFile().getName();
        lblSelectedFile.setText(fullSourcePath);
    }//GEN-LAST:event_btnFileToDownloadActionPerformed

    private void btnPauseContinueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPauseContinueActionPerformed
        if(obnovit){
            nacitajStav();
            SpustiStahovanie();
        }else{
            zastavStahovanie();
            ulozStav();
        }
    }//GEN-LAST:event_btnPauseContinueActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
        zastavStahovanie();
        zmazStopy();
    }//GEN-LAST:event_btnStopActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(KlientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(KlientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(KlientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(KlientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new KlientForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDownload;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnFileToDownload;
    private javax.swing.JButton btnPauseContinue;
    private javax.swing.JButton btnSelectDestinationFolder;
    private javax.swing.JButton btnStop;
    private javax.swing.JLabel lblProgress;
    private javax.swing.JLabel lblSelectedDestination;
    private javax.swing.JLabel lblSelectedFile;
    private javax.swing.JLabel lblSocketCount;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTimeElapsed;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextField txtSocketCount;
    // End of variables declaration//GEN-END:variables

    private void refreshniObnovit() {
        Scanner citac = null;
        try{
            citac = new Scanner(new File("posli.txt"));        
            obnovit = citac.nextBoolean();
            System.out.println("treba obnovit: " + obnovit);
            if(obnovit){
                pocetSoketov = citac.nextInt();
                System.out.println("pocet soketov aktualizovany");
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }finally{
            if(citac != null)
                citac.close();
        }
    }

    private void zastavStahovanie() {
        swTime.cancel(true);
        sw.cancel(false);
    }

    private void zmazStopy() {
        
    }

    private void nacitajStav() {
        
    }

    private void ulozStav() {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter("posli.txt");
            pw.println(true);
            pw.println(pocetSoketov);
            pw.println(fullSourcePath);
            pw.println(destinationPath);
            System.err.println("TUUUUUUUUUUUUUU");
            pw.println(mapa.get("uspesneSokety"));
            pw.println(mapa.get("posli"));
            System.out.println("stav ulozeny");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(pw != null) pw.close();
        }
    }
}
