import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Renzil Dourado on 3/11/2018.
 */
public class Server {

    private Socket socket;
    private ServerSocket serverSock;
    private ObjectInputStream in;
    private static ObjectOutputStream out;
    public static ConcurrentHashMap<String, Socket> liveNodes = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, ArrayList<Integer>> idToDataNodes = new ConcurrentHashMap<>();


    public Server(){
        try {
            System.out.println("Server Started");
            this.serverSock = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        Server serverObj = new Server();
        new Thread(new ServerListener()).start();
        serverObj.start();

    }

    public void start(){

        try {

            while(true){

                this.socket = serverSock.accept();
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                String id = in.readUTF();
                System.out.println(id +" has joined");
                liveNodes.put(id, socket);
//                System.out.println("LiveNodes are " +liveNodes);
                calculateFingerTable();


            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void fillNodeResponsibility(String id){

        int idInt = Integer.parseInt(id);

        ArrayList<Integer> nodes = new ArrayList<>();
        nodes.add(idInt);

        int findIds = idInt-1;

        if(findIds<0)
            findIds += 16;

        while(findIds!=idInt && (!liveNodes.containsKey(findIds+""))){

            if(findIds<0)
                findIds+=16;
            nodes.add(findIds);
            findIds--;

        }

        idToDataNodes.put(id, nodes);


    }

    public static void calculateFingerTable(){


        for(int h=0; h<16; h++){

            String key = h+"";
            if(liveNodes.containsKey(key)) {

                fillNodeResponsibility(key);

                int id = Integer.parseInt(key);
                FingerTable fingerTable = new FingerTable(id);

                fingerTable.nodesToBeHandled = idToDataNodes.get(key);
                int table[][] = new int[4][3];

                for (int i = 0; i < 4; i++) {

                    table[i][0] = i;
                    table[i][1] = (id + (int) Math.pow(2, i)) % 16;
                    int currentNode = table[i][1];
                    int current;


                    for (int k = 0; k < 16; k++) {
                        current = (currentNode + k) % 16;

                        if (liveNodes.containsKey("" + current)) {
                            table[i][2] = current;
                            break;
                        }

                    }

                    Socket tempSock = liveNodes.get(table[i][2] + "");
                    InetAddress ip = tempSock.getInetAddress();
                    fingerTable.keyToIp.put(table[i][2], ip);
                }

                fingerTable.table = table;
                System.out.println();

                Socket sock = liveNodes.get(key);
                InetAddress ip = sock.getInetAddress();
                int port = 5000 + Integer.parseInt(key);

                try {
                    Socket sendSock = new Socket(ip, port);
                    out = new ObjectOutputStream(sendSock.getOutputStream());
                    out.writeObject(fingerTable);
                    out.flush();
                    System.out.println("Finger table sent to" + key);

                } catch (IOException e) {
                    System.out.println("Retry");
                    h--;
                }
            }
            //System.out.println(fingerTable);

        }

    }
}
