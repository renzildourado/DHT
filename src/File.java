import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by Renzil Dourado on 3/12/2018.
 */
public class File implements Serializable{

    String name;
    String content;
    int tempNode;
    InetAddress ip;
    String foundNode;

    public File(String filename){
        this.name = filename;
        this.tempNode = -1;
    }
}
