import com.sun.source.util.JavacTask;

import javax.tools.*;
import java.io.*;
import java.util.Arrays;
import java.util.Stack;

/**
 * Created by Morgan on 1/28/15.
 * This class's original code is from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
 */
public class Reader {
    private static StringBuilder buf = new StringBuilder();    // fill this as you process, character by character
    private static BufferedReader input; // where are we reading from?
    private static int c; // current character of lookahead; reset upon each getNestedString() call
    private static Stack<Integer> s = new Stack();
    private static int classIndex = 0;
    private static boolean toBeClean = true;

    public static int getClassIndex() {
        return classIndex;
    }

    public static void decreaseClassIndex() {
        classIndex--;
    }

    public static boolean isToBeClean() {
        return toBeClean;
    }

    public static void getNewInput(BufferedReader input) throws IOException {
        int state;
        if(toBeClean) {
            Reader.buf = new StringBuilder();
            Reader.s = new Stack();
        }

        Reader.input = input;
        c = input.read();
        while(c!=-1) {
            state = consume();
            if(state == -1) break;
        }

        if(s.empty()) {
            toBeClean = true;
            Reader.classIndex++;
        }
        else {
            toBeClean = false;
        }
    }

    //This function's original code comes from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
    public static boolean isDeclaration() throws IOException {
        //copyFile("tem/REPL_t_0.java","tem2/t.java");
        addTestDeclaration();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList("tem2/t.java"));
        JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

        task.parse();
        System.out.print("parase(): ");
        System.out.println(diagnostics.getDiagnostics().size() == 0);
        return diagnostics.getDiagnostics().size() == 0;
    }

    public static String getNestedString() throws IOException {
        return Reader.buf.toString();
    }

    //This function's original code comes from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
    public static int consume() throws IOException {
        int flag = 0;
        buf.append((char)c);
        if(c == (int)'/') flag = 1;

        switch (c) {
            case (int)'(':
                s.push((int)')');
                break;
            case (int)'[':
                s.push((int)']');
                break;
            case (int)'{':
                s.push((int) '}');
                break;

            case (int)')':
                if(!s.empty() && s.peek() == (int)')') s.pop();
                else {
                    return -1;
                }
                break;
            case (int)']':
                if(!s.empty() && s.peek() == (int)']') s.pop();
                else {
                    return -1;
                }
                break;
            case (int)'}':
                if(!s.empty() && s.peek() == (int)'}') s.pop();
                else {
                    return -1;
                }
                break;
        }

        c = input.read();
        if(flag == 1 && c == (int)'/') {
            buf.deleteCharAt(buf.length()-1);
            return -1;
        }

        return 0;
    }

    //This function's code original comes from http://zhidao.baidu.com/link?url=3K-eO6Gy-tz_hMIB1eCvx6Th7UnX0Aqh47qdGtbas63BgNgCGE9MYjnujxNPRxyDN9DVBcHtjM1z_inttzjKaa
    public static void copyFile(String oldPath, String newPath) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);  //Read the original file
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("Copy File Error!");
            e.printStackTrace();
        }
    }

    public static void addTestDeclaration() throws IOException {
        String fileName = "tem2/t.java";
        String line;

        FileWriter writer = new FileWriter(fileName);
        writer.write("import java.io.*;\n");
        writer.write("import java.util.*;\n");
        line = "public class t {";
        writer.write(line);

        line = "    public static " + Reader.buf.toString();
        writer.write(line);

        line = "}";
        writer.write(line);

        writer.close();
    }

    public static void addLineToFile(String fileName, String line) throws IOException {
        FileWriter writer = new FileWriter(fileName,true);
        writer.write(line + "\n");
        writer.close();
    }
}