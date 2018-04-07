import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Renzil Dourado on 3/13/2018.
 */
public class NodeFileRedistribute implements Runnable {


    ServerSocket servSock;
    Socket socket;
    ObjectInputStream in;

    public NodeFileRedistribute(){
        try {
            servSock = new ServerSocket(10000+Integer.parseInt(Node.id));

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
                    ArrayList<Integer> requestedFileList = (ArrayList<Integer>) in.readObject();

                    HashMap<String, File> fileToBeSent = new HashMap<String, File>();

                    for(int i=0; i<requestedFileList.size(); i++){
                        for(String filename: Node.fileToContent.keySet()) {
                            if (Math.abs(filename.hashCode() % 16) == requestedFileList.get(i)) {

                                fileToBeSent.put(filename, Node.fileToContent.get(filename));
                                Node.fileToContent.remove(filename);
                            }
                        }
                    }

                    System.out.println("Sending data to "+socket.getInetAddress());
                    Socket sendSock = new Socket(socket.getInetAddress(), 11000);
                    ObjectOutputStream out = new ObjectOutputStream(sendSock.getOutputStream());
                    out.writeObject(fileToBeSent);
                    out.flush();
                    sendSock.close();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }



            } catch (IOException e) {
                //e.printStackTrace();
            }

        }



    }
}
