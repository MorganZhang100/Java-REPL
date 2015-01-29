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

    public NestedReader(BufferedReader input) {
        this.input = input;
    }
    public String getNestedString() throws IOException {
        return "";
    }

    void consume() throws IOException {
        buf.append((char)c);
        c = input.read();
    }
}
