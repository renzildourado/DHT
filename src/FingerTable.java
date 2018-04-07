import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Renzil Dourado on 3/11/2018.
 */
public class FingerTable implements Serializable {

    public int id;
    public int[][] table = new int[4][3];
    public HashMap<Integer, InetAddress> keyToIp = new HashMap<>();
    public ArrayList<Integer> nodesToBeHandled = new ArrayList<>();

    public FingerTable(int id){
            this.id = id;
    }

    public String toString(){

        String tab = "";

        //System.out.println("Node "+id+" should handle "+nodesToBeHandled);

        System.out.println("i k+pow(2,i) Successor");

        for(int i=0; i<4; i++){
            for(int j=0; j<3 ; j++){
                tab = tab + table[i][j] +"\t\t";
            }
            tab = tab +"\n";
        }

        return tab;

    }


}
