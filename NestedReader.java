import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by Morgan on 1/28/15.
 * This class's origional code is from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
 */
public class NestedReader {
    StringBuilder buf;    // fill this as you process, character by character
    BufferedReader input; // where are we reading from?
    int c; // current character of lookahead; reset upon each getNestedString() call

    public NestedReader() {
        this.buf = new StringBuilder();
    }

    public void getNewInput(BufferedReader input) throws IOException {
        int state;
        this.input = input;
        c = input.read();
        while(c!=-1) {
            state = consume();
            if(state == -1) break;
        }
        System.out.println(getNestedString());
    }

    public String getNestedString() throws IOException {
        return buf.toString();
    }

    int consume() throws IOException {
        int flag = 0;
        buf.append((char)c);
        if(c == 47) flag = 1;
        c = input.read();
        if(flag == 1 && c == 47) {
            buf.deleteCharAt(buf.length()-1);
            return -1;
        }
        return 0;
    }
}
