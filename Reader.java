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
    static StringBuilder buf = new StringBuilder();    // fill this as you process, character by character
    static BufferedReader input; // where are we reading from?
    static int c; // current character of lookahead; reset upon each getNestedString() call
    static Stack<Integer> s = new Stack();
    private static int classAmount = 0;

    public static int getClassAmount() {
        return classAmount;
    }

    public static void getNewInput(BufferedReader input) throws IOException {
        int state;
        Reader.input = input;
        c = input.read();
        while(c!=-1) {
            state = consume();
            if(state == -1) break;
        }
        System.out.println(getNestedString());
    }


    //This function's original code comes from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
    public static boolean isDeclaration() throws IOException {
        copyFile("REPL_t.java","t.java");
        addTestDeclaration();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList("t.java"));
        //JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
        JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

        task.parse();
        return diagnostics.getDiagnostics().size() == 0;
    }

    public static String getNestedString() throws IOException {
        return buf.toString();
    }

    //This function's original code comes from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
    public static int consume() throws IOException {
        int flag = 0;
        buf.append((char)c);
        if(c == (int)'\\') flag = 1;

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
                if(s.peek() == (int)')') s.pop();
                else {
                    return -1;
                }
                break;
            case (int)']':
                if(s.peek() == (int)']') s.pop();
                else {
                    return -1;
                }
                break;
            case (int)'}':
                if(s.peek() == (int)'}') s.pop();
                else {
                    return -1;
                }
                break;
        }

        c = input.read();
        if(flag == 1 && c == (int)'\\') {
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
        String fileName = "tem/t.java";
        String line;
        String classFrom;
        if(classAmount == 0) classFrom = "REPL_t";
        else classFrom = "REPL_t_" + classAmount;
        line = "public class REPL_t_" + (classAmount + 1) + " extends " + classFrom + " {";
        addLineToFile(fileName,line);

        line = "    public static " + buf.toString();
        addLineToFile(fileName,line);

        line = "}";
        addLineToFile(fileName,line);
    }

    public static void addLineToFile(String fileName, String line) throws IOException {
        FileWriter writer = new FileWriter(fileName,true);
        writer.write(line + "\n");
        writer.close();
    }
}