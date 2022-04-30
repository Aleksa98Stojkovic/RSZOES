package stickersserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectedStickersClient implements Runnable{
    
    private Socket socket;
    private String userName;
    private ArrayList<Integer> duplicates;
    private ArrayList<Integer> missing;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<ConnectedStickersClient> allClients;
    private static HashMap<String, ArrayList<Integer>> swapHash = new HashMap<>();
    
    public ConnectedStickersClient(Socket socket, ArrayList<ConnectedStickersClient> allClients) {
        this.socket = socket;
        this.allClients = allClients;
        this.duplicates = new ArrayList<>();
        this.missing = new ArrayList<>();
        try {
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            this.userName = "";
        } catch (IOException ex) {
            Logger.getLogger(ConnectedStickersClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String moreDetails(String user)
    {
        String message = "TA:";
        for(ConnectedStickersClient client : this.allClients)
        {
            if(client.getUserName().equals(user))
            {
                String str = "Korisnik " + client.getUserName() + " ima duplikate ";
                for(Integer el : client.duplicates)
                    if(this.missing.contains(el))
                        str += el + ", ";
                
                message += str;
                break;
            }
        }
        
        return message.substring(0, message.length() - 2);
    }
    
    public void findCompatible()
    {
        String[] message;
        message = new String[this.allClients.size()];
        ConnectedStickersClient pivot;
        for(int i = 0; i < this.allClients.size(); i++)
        {
            message[i] = "CB:";
            pivot = this.allClients.get(i);
            for(ConnectedStickersClient client : this.allClients)
            {
                if(!client.equals(pivot))
                {
                    int occurrence = 0;
                    int overlap = 0;
                    for(Integer myMissing : pivot.missing)
                    {
                        if(client.duplicates.contains(myMissing))
                            occurrence++;
                    }
                    for(Integer myDuplicate : pivot.duplicates)
                    {
                        if(client.missing.contains(myDuplicate))
                            overlap++;
                    }

                    if(Integer.min(occurrence, overlap) != 0)
                    {
                        // message[i] += client.getUserName() + "(" + Integer.min(overlap, occurrence) + "," + occurrence + "),";
                        message[i] += client.getUserName() + "(" + Integer.min(overlap, occurrence) + "),";
                    }
                }
            }
            
            if(!message[i].equals("CB:"))
                    message[i] = message[i].substring(0, message[i].length() - 1);
            
            // Slanje poruke
            pivot.pw.println(message[i]);
        }
    }
    
    // Upisuje određene informacije u strukturu podataka i obaveštava drugog korisnika
    // o mogućoj razmeni
    public void initSwap(String message)
    {
        
        // Poruka je u formi initswap:Korisnik:elementi_koje_zelimo
        String[] messageElements = message.split(":");
        ArrayList<Integer> swapCandidate = new ArrayList<>();
        for(String el : messageElements[2].split(","))
        {
            swapCandidate.add(Integer.parseInt(el));
        }
        swapHash.put(this.getUserName() + ":" + messageElements[1], swapCandidate);
        for(ConnectedStickersClient dst : this.allClients)
        {
            if(messageElements[1].equals(dst.getUserName()))
            {
                String msg = "TA1:Korisnik " + this.getUserName() + " želi da izvrši zamenu sa Vama. ";
                msg += "Od Vas je zatražio sličice ";
                msg += messageElements[2] + ". ";
                msg += "Ukoliko želite da prihvatite razmenu izaberite koje sličice želite da menjate.";
                dst.pw.println(msg);
                break;
            }
        }
    }
    
    // Brise elemente iz lista duplikata i nedostajucih kod korisnika koji vrse razmenu,
    // brise se odgovarajuci element iz mape i salju se poruke za brisanje CheckBox-ova kod oba klijenta
    void acceptSwap(String message)
    {
        // Poruka je oblika accept:Korisnik koji je inicirao razmenu:slicice koje zahteva onaj koji je primio zahtev za razmenu
        String msgInitiator = "remove:";
        String msgAcceptor = "remove:";
        String srcTAMessage = "TA1:Korisnik " + this.getUserName() + " je prihvatio razmenu";
        String[] messageElements = message.split(":");
        ArrayList<Integer> removeDuplicatesFromAcceptor = swapHash.get(messageElements[1] + ":" + this.getUserName());
        swapHash.remove(messageElements[1] + ":" + this.getUserName());
        ArrayList<Integer> removeDuplicatesFromInitiator = new ArrayList<>();
        for(String el : messageElements[2].split(","))
            removeDuplicatesFromInitiator.add(Integer.parseInt(el));
        for(ConnectedStickersClient src : this.allClients)
        {
            if(src.getUserName().equals(messageElements[1]))
            {
                src.pw.println(srcTAMessage);
                /* Ovo možda mora kao kritična sekcija */
                for(Integer el : removeDuplicatesFromInitiator)
                {
                    msgInitiator += el + ",";
                    src.removeDuplicates(el);
                }
                msgInitiator = msgInitiator.substring(0, msgInitiator.length() - 1);
                msgInitiator += ":";
                for(Integer el : removeDuplicatesFromAcceptor)
                {
                    msgInitiator += el + ",";
                    src.removeMissing(el);
                }
                msgInitiator = msgInitiator.substring(0, msgInitiator.length() - 1);
                src.pw.println(msgInitiator);
                /***************************************/
                break;
            }
        }
        
        /* Ovo možda mora kao kritična sekcija */
        for(Integer el : removeDuplicatesFromAcceptor)
        {
            msgAcceptor += el + ",";
            this.removeDuplicates(el);
        }
        msgAcceptor = msgAcceptor.substring(0, msgAcceptor.length() - 1);
        msgAcceptor += ":";
        for(Integer el : removeDuplicatesFromInitiator)
        {
            msgAcceptor += el + ",";
            this.removeMissing(el);
        }
        /***************************************/
        msgAcceptor = msgAcceptor.substring(0, msgAcceptor.length() - 1);
        this.pw.println(msgAcceptor);
    }
    
    void declineSwap(String message)
    {
        // Poruka je oblika decline:korisnik koji je inicirao razmenu
        String initUser = message.split(":")[1];
        swapHash.remove(initUser + ":" + this.getUserName());
        
        for(ConnectedStickersClient src : this.allClients)
        {
            if(src.getUserName().equals(initUser))
            {
                src.pw.println("TA1:Korisnik " + this.getUserName() + " je odbio razmenu");
                break;
            }
        }
    }
    
    // Uklanja dati skup duplikata specificiran stringom
    public void removeDuplicates(Integer duplicates)
    {
        this.duplicates.remove(duplicates);
    }
    
    // Uklanja dati skup nedostajucih slicica specificiran stringom
    public void removeMissing(Integer missing)
    {
        this.missing.remove(missing);
    }
    
    public void setDuplicates(String duplicates)
    {
        String[] duplicatesArray = duplicates.split(",");
        for(String el : duplicatesArray)
        {
            this.duplicates.add(Integer.parseInt(el));
        }
    }
    
    public void setMissing(String missing)
    {
        String[] missingArray = missing.split(",");
        for(String el : missingArray)
        {
            this.missing.add(Integer.parseInt(el));
        }
    }
    
    public String getUserName()
    {
        return this.userName;
    }
    
    @Override
    public void run()
    {
        while (true) 
        { 
            try
            {
                if(this.getUserName().equals(""))
                {
                    String command = this.br.readLine();
                    if(command != null)
                    {
                        if(command.startsWith("init:")) 
                        {
                            String[] subCommand = command.split(":");
                            System.out.println("Connected user: " + subCommand[1]);
                            this.userName = subCommand[1];
                            this.setDuplicates(subCommand[2]);
                            this.setMissing(subCommand[3]);
                            // Obavestava sve klijente o mogucim razmenama
                            this.findCompatible();
                        }
                    }
                    else 
                    {
                        break;
                    }
                }
                else
                {
                    System.out.println("Ceka se na poruku...");
                    String cmd = this.br.readLine();
                    if(cmd.startsWith("initswap:"))
                        this.initSwap(cmd);
                    else if(cmd.startsWith("get:"))
                    {
                        this.pw.println(this.moreDetails(cmd.split(":")[1]));
                    }
                    else if(cmd.startsWith("accept:"))
                    {
                        this.acceptSwap(cmd);
                        this.findCompatible();
                    }
                    else if(cmd.startsWith("decline:"))
                    {
                        this.declineSwap(cmd);
                    }
                    else
                        System.out.println("Nepoznata komanda");
                }
            }
            catch(IOException ex)
            {
                System.out.println("Disconnected user: " + this.userName);
                for (ConnectedStickersClient cl : this.allClients) {
                    if (cl.getUserName().equals(this.userName)) {
                        this.allClients.remove(cl);
                        return;
                    }
                }
            }
        }
    }
}