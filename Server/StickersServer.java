package stickersserver;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StickersServer {

private ServerSocket ssocket;
    private int port;
    private ArrayList<ConnectedStickersClient> clients;

    public ServerSocket getSsocket() {
        return ssocket;
    }

    public void setSsocket(ServerSocket ssocket) {
        this.ssocket = ssocket;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void acceptClients() {
        Socket client = null;
        Thread thr;
        while (true) {
            try {
                System.out.println("Waiting for new clients..");
                client = this.ssocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(StickersServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (client != null) {
                ConnectedStickersClient clnt = new ConnectedStickersClient(client, clients);
                clients.add(clnt);
                thr = new Thread(clnt);
                thr.start();
            } else {
            	break;
            }
        }
    }

    public StickersServer(int port) {
        this.clients = new ArrayList<>();
        try {
            this.port = port;
            this.ssocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(StickersServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public static void main(String[] args) {
        StickersServer server = new StickersServer(6001);
        
        System.out.println("Server pokrenut, slusam na portu 6001");
        server.acceptClients();
        
    }
    
}
