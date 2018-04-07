import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Renzil Dourado on 3/11/2018.
 */
public class NodeListener implements Runnable {


    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectInputStream in;
    private int id;

    public NodeListener(int id){
         this.id = id;
        try {
            serverSocket = new ServerSocket(id+5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while(true){
            try {

                FingerTable table = null;
                try {
                    this.socket = serverSocket.accept();
                    in = new ObjectInputStream(this.socket.getInputStream());
                    Node.fingerTable = (FingerTable)in.readObject();
                    //findMissingData();
                    Thread.sleep(500);
                    synchronized (Node.waitObj){

                        Node.waitObj.notify();
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }


}
