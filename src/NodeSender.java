import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Created by Renzil Dourado on 3/12/2018.
 */
public class NodeSender implements Runnable{



    @Override
    public void run() {

        Scanner sc = new Scanner(System.in);
        findMissingData();



        while(true){

            printMenu();
            String option = sc.next();

            switch(option){

                case "E":
                    try {
                        ObjectOutputStream out;
                        Socket socketExit;

                        System.out.println("Hey I am exitting");
                        socketExit = new Socket(Node.ip, 6000);
                        out = new ObjectOutputStream(socketExit.getOutputStream());
                        out.writeUTF(Node.id);
                        out.flush();
                        socketExit.close();
                        Node.transferFilesToSuccessor();
                        System.exit(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "F":
                    System.out.println("Finger Table for node: "+Node.id);
                    System.out.println("=================================");
                    System.out.println(Node.fingerTable);
                    System.out.println("==================================");
                    break;

                case "C":
                    System.out.println("Enter file name: ");
                    String file = sc.next();

                    Node.sendFile(new File(file));
                    break;

                case "D":
                    System.out.println("====================================");
                    System.out.println("Files at this node: ");
                    for(String fileName: Node.fileToContent.keySet()){
                        System.out.println(fileName);
                    }
                    System.out.println("=====================================");
                    break;

                case "R":
                    System.out.println("Enter file name");
                    String fileName = sc.next();
                    ServerSocket servSockRetrieve = null;
                    try {
                        servSockRetrieve = new ServerSocket(9000);
                        File findfile = new File(fileName);
                        findfile.ip = InetAddress.getLocalHost();
                        Node.findFile(findfile);
                        Socket socket;
                        ObjectInputStream in;
                        socket = servSockRetrieve.accept();
                        in = new ObjectInputStream(socket.getInputStream());
                        File retrievedFile = (File)in.readObject();
                        System.out.println("======================================================================");
                        System.out.println("File "+retrievedFile.name +" found at Node "+retrievedFile.foundNode);
                        System.out.println("======================================================================");
                        servSockRetrieve.close();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    break;

            }

        }
    }

    public static void printMenu(){
        System.out.println("MENU");
        System.out.println("===========================================");
        System.out.println("Type E to Exit");
        System.out.println("Type F to display Finger Table");
        System.out.println("Type C to Create file");
        System.out.println("Type D to Display files at this node");
        System.out.println("Type R to Retrieve a file");
        System.out.println("===========================================");

    }

    public static void findMissingData(){

        ArrayList<Integer> dataIshudHave = Node.fingerTable.nodesToBeHandled;
        HashSet<Integer> dataIhave = new HashSet<>();
        ArrayList<Integer> dataIshudRequestFor = new ArrayList<>();

        for(String filename: Node.fileToContent.keySet()){
            dataIhave.add((filename.hashCode())%16);
        }

        if(dataIhave.size()<dataIshudHave.size()){

            for(int i=0; i<dataIshudHave.size(); i++){
                if(!dataIhave.contains(dataIshudHave.get(i))){
                    if(Node.fingerTable.table[0][2]!=Integer.parseInt(Node.id)) {
                        dataIshudRequestFor.add(dataIshudHave.get(i));
                    }
                }
            }

        }
        //System.out.println("I shud req for "+dataIshudRequestFor +" from "+Node.fingerTable.table[0][2]);
        requestData(dataIshudRequestFor, Node.fingerTable.keyToIp.get(Node.fingerTable.table[0][2]));

    }

    public static void requestData(ArrayList<Integer> dataIshudRequestFor, InetAddress fromNode){

        ServerSocket servSockRequest;
        Socket socketReqData;
        ObjectInputStream in;
        ObjectOutputStream out;
        HashMap<String, File> fileToBeReceived = new HashMap<String, File>();


        try {
            socketReqData =  new Socket(fromNode, 10000+Node.fingerTable.table[0][2]);
            out = new ObjectOutputStream(socketReqData.getOutputStream());
            out.writeObject(dataIshudRequestFor);
            out.flush();
            socketReqData.close();

            servSockRequest = new ServerSocket(11000);
            socketReqData = servSockRequest.accept();
            in = new ObjectInputStream(socketReqData.getInputStream());
            try {
                fileToBeReceived = (HashMap<String, File>)in.readObject();
                socketReqData.close();
                servSockRequest.close();
                for(String filename: fileToBeReceived.keySet()) {

                    Node.fileToContent.put(filename, fileToBeReceived.get(filename));
                }

                } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            //e.printStackTrace();
        }



    }
}
