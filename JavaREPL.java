import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

public class JavaREPL {
	public static void main(String[] args) throws IOException {
        NestedReader nr = new NestedReader();

        while(true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(">");
            String text = br.readLine();
            StringReader sr = new StringReader(text);
            BufferedReader br_line = new BufferedReader(sr);
            nr.getNewInput(br_line);
        }
    }
}