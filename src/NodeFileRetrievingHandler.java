import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Renzil Dourado on 3/13/2018.
 */
public class NodeFileRetrievingHandler implements Runnable{

    ServerSocket servSock;
    Socket socket;
    ObjectInputStream in;

    public NodeFileRetrievingHandler(){
        try {
            servSock = new ServerSocket(8000+Integer.parseInt(Node.id));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        while(true){

            try {
                socket = servSock.accept();
                in = new ObjectInputStream(socket.getInputStream());
                try {
                    File file = (File)in.readObject();
                    Node.findFile(file);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
}
