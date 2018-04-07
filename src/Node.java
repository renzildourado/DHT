import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Renzil Dourado on 3/11/2018.
 */
public class Node {

    private Socket socket;
    private ServerSocket serverSock;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    public static String id;
    public static String ip;
    public static FingerTable fingerTable;
    public static ConcurrentHashMap<String, File> fileToContent = new ConcurrentHashMap<>();
    public static Object waitObj = new Object();

    public Node(){
        System.out.println("Enter GUID:");
        Scanner sc = new Scanner(System.in);
        this.id = sc.next();
        System.out.println("Enter Server IP address:");
        this.ip = sc.next();
        //this.ip = "localhost";

        try {
            this.socket = new Socket(ip, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Node nodeObj = new Node();
        nodeObj.register();

        synchronized (Node.waitObj){
            try {
                System.out.println("Waiting here at Node");
                Node.waitObj.wait();
                new Thread(new NodeSender()).start();
                new Thread(new NodeFileRedistribute()).start();
                new Thread((new NodeFileSendingHandler())).start();
                new Thread((new NodeFileRetrievingHandler())).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void register(){

        try {

            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeUTF(id);
            out.flush();
            new Thread(new NodeListener(Integer.parseInt(id))).start();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void sendFile(File file){

        int hash = Math.abs(file.name.hashCode());
        hash = hash%16;
        //System.out.println("I have to send the file "+file.name+" to node " +hash);

        if(id.equals(hash+"")||id.equals(file.tempNode+"")){
            //System.out.println("Oh wait..thats me");
            fileToContent.put(file.name, file);

        }
        else{
            int target = findNearestTarget(hash, file);
            InetAddress sendIP = fingerTable.keyToIp.get(target);

            try {
                Socket sendFile = new Socket(sendIP, 7000+target);
                ObjectOutputStream fileOut = new ObjectOutputStream(sendFile.getOutputStream());
                //System.out.println("Sending file "+file.name +" to "+target);
                fileOut.writeObject(file);
                fileOut.flush();
                sendFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public static void reDistributeFiles(){
        for(String fileName: fileToContent.keySet()){
            File fileToRedistribute = fileToContent.get(fileName);

            if(fileToRedistribute.tempNode !=-1){
                fileToRedistribute.tempNode = -1;
                sendFile(fileToRedistribute);
                fileToContent.remove(fileName);

            }
        }
    }

    public static int findNearestTarget(int hash, File file){

        int[][] table = fingerTable.table;
        int bestDifference = Integer.MAX_VALUE;
        int target = -1;

        for(int i=0; i<4; i++){

            if(table[i][1] == hash){
                file.tempNode = table[i][2];
                return table[i][2];
            }
            else{

                int difference = hash - table[i][1];

                if(difference<0){
                    difference += 16;
                }

                if(difference<bestDifference){
                    bestDifference = difference;
                    if(table[i][1] <hash && hash < table[i][2])
                        file.tempNode = table[i][2];
                    target = table[i][2];
                }
                //if(Integer.parseInt(Node.id) )
            }
        }



        return target;
    }

    public static void transferFilesToSuccessor(){

        int target = fingerTable.table[0][2];

        for(String filename: fileToContent.keySet()){
            File fileToSend = fileToContent.get(filename);
            fileToSend.tempNode = target;
            InetAddress sendIP = fingerTable.keyToIp.get(target);

            try {
                Socket sendFile = new Socket(sendIP, 7000+target);
                ObjectOutputStream fileOut = new ObjectOutputStream(sendFile.getOutputStream());
                //System.out.println("Sending file "+fileToSend.name +" to "+target);
                fileOut.writeObject(fileToSend);
                fileOut.flush();
                sendFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    public static void findFile(File file){

        int hash = Math.abs(file.name.hashCode());
        hash = hash%16;

        if(id.equals(hash+"")||id.equals(file.tempNode+"")||fileToContent.containsKey(file.name)){
            //System.out.println("Oh wait...its here..How to send it back?");

            //System.out.println(fileToContent.get(file.name).name);


            try {
                System.out.println("File found, sending to"+file.ip);
                Socket sendFoundFile = new Socket(file.ip, 9000);
                ObjectOutputStream fileOut = new ObjectOutputStream(sendFoundFile.getOutputStream());
                fileToContent.get(file.name).foundNode = Node.id;
                fileOut.writeObject(fileToContent.get(file.name));
                fileOut.flush();
                sendFoundFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        else{
            int target = findNearestTarget(hash, file);
            InetAddress sendIP = fingerTable.keyToIp.get(target);

            try {
                Socket sendRequest = new Socket(sendIP, 8000+target);
                ObjectOutputStream fileOut = new ObjectOutputStream(sendRequest.getOutputStream());
                //System.out.println("Search directed to file "+target);
                fileOut.writeObject(file);
                fileOut.flush();
                sendRequest.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
}
