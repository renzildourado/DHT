import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Renzil Dourado on 3/12/2018.
 */
public class ServerListener implements Runnable{

    private ServerSocket servSockExLi;
    private Socket socketExLi;
    private ObjectInputStream in;

    public ServerListener(){
        try {
            servSockExLi = new ServerSocket(6000);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void run() {

        while(true){

            try {
                socketExLi = servSockExLi.accept();
                in = new ObjectInputStream(socketExLi.getInputStream());
                String id = in.readUTF();

                System.out.println("I have to remove :"+ id);
                Server.liveNodes.remove(id);
                Server.calculateFingerTable();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
