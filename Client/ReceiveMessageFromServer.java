import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveMessageFromServer implements Runnable{
    JFrameClientGUI parent;
    BufferedReader br;
    
    public ReceiveMessageFromServer(JFrameClientGUI parent) 
    {
        this.parent = parent;
        this.br = parent.getBr();
    }
    
    @Override
    public void run()
    {
        while (true) {
            String line;
            try {
                
                line = this.br.readLine();

                if (line.startsWith("CB:")) 
                {
                    // Poruka je oblika CB:Ivan(5),Dusan(3),... 
                    if(!line.equals("CB:"))
                    {
                        this.parent.getTA().setText("");
                        String[] options = line.split(":")[1].split(",");
                        parent.getComboBox().removeAllItems();
                        for (String el : options) {
                            parent.getComboBox().addItem(el);
                        }
                    }
                    else
                        parent.getTA().setText("Nema opcija za menjažu");

                } 
                else if(line.startsWith("TA:"))
                {
                    // Poruka je formata TA:...
                    parent.getTA().setText(line.split(":")[1]);
                }
                else if(line.startsWith("remove:"))
                {
                    // Poruka je formata remove:duplikati:nedostajuci
                    String[] messageElements = line.split(":");
                    this.parent.removeMissing(messageElements[2]);
                    this.parent.removeDuplicates(messageElements[1]);
                }
                else if(line.startsWith("TA1:"))
                {
                    // Poruka je formata TA1:...
                    parent.getTA1().setText(line.split(":")[1]);
                }
                else
                    System.out.println("Nepoznata komanda");
            } catch (IOException ex) {
                Logger.getLogger(ReceiveMessageFromServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
