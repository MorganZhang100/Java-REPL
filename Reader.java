import com.sun.source.util.JavacTask;

import javax.tools.*;
import java.io.*;
import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Morgan on 1/28/15.
 * This class's original code is from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
 */
public class Reader {
    private static StringBuilder buf = new StringBuilder();    // fill this as you process, character by character
    private static String testMultiDeclaration;
    private static BufferedReader input; // where are we reading from?
    private static int c; // current character of lookahead; reset upon each getNestedString() call
    private static int pre_c = -1; // previous character before the last character of buf ; -1 means no such character exists
    private static Stack<Integer> s_all = new Stack();
    private static boolean in_quotation = false;
    private static boolean in_doubleQuotation = false;
    private static int classIndex = 0;
    private static boolean toBeClean = true;
    private static boolean debug = false;

    public static int getClassIndex() {
        return classIndex;
    }

    public static void decreaseClassIndex() {
        classIndex--;
    }

    public static boolean isToBeClean() {
        return toBeClean;
    }

    public static boolean stillTestMultiDeclaration() {
        return testMultiDeclaration.length() != 0;
    }

    public static String getMultiDeclaration() throws IOException {
        int i = 0;
        for (i=0; i<=testMultiDeclaration.length(); i++) {
            String t = testMultiDeclaration.substring(0,i);
            if(isMultiDeclaration(t)) {
                if(i<testMultiDeclaration.length()) testMultiDeclaration = testMultiDeclaration.substring(i,testMultiDeclaration.length());
                else testMultiDeclaration = "";
                return t;
            }
        }

        return "";
    }

    public static int getNewInput(String newInput) throws IOException {
        if(toBeClean) {
            Reader.buf = new StringBuilder();
            Reader.s_all = new Stack();
            Reader.in_quotation = false;
            Reader.in_doubleQuotation = false;
        }

        if(Reader.buf.length() == 0 && newInput.equals("")) return -1;

        StringReader sr = new StringReader(newInput);
        BufferedReader br_newInput = new BufferedReader(sr);

        int state = 0;

        Reader.input = br_newInput;
        c = br_newInput.read();
        while(c!=-1) {
            state = consume();
            if(state == -1) break;
        }

        if(s_all.empty() || state == -1) {
            toBeClean = true;
            Reader.classIndex++;
        }
        else {
            toBeClean = false;
        }

        return 0;
    }

    //This function's original code comes from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
    public static boolean isDeclaration() throws IOException {
        String pattern = "(?<=print )[\\s\\S]*(?=;)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(buf.toString());
        if(m.find()) {
            String expr = m.group(0);
            buf = new StringBuilder("System.out.println( " + expr +" );");
        }

        addTestDeclaration();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList("tem/testDeclaration.java"));
        JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

        task.parse();
        if(debug) System.out.print("parase(): ");
        if(debug) System.out.println(diagnostics.getDiagnostics().size() == 0);

        if(diagnostics.getDiagnostics().size() == 0) testMultiDeclaration = buf.toString();
        else testMultiDeclaration = "";

        return diagnostics.getDiagnostics().size() == 0;
    }

    //This function's original code comes from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
    public static boolean isMultiDeclaration(String s) throws IOException {
        addTestMultiDeclaration(s);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList("tem/testMultiDeclaration.java"));
        JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

        task.parse();
        if(debug) System.out.print("parase(): ");
        if(debug) System.out.println(diagnostics.getDiagnostics().size() == 0);
        return diagnostics.getDiagnostics().size() == 0;
    }

    public static String getNestedString() throws IOException {
        return Reader.buf.toString();
    }

    //This function's original code comes from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
    public static int consume() throws IOException {
        int comment_flag = 0;
        if(buf.length()>=2) pre_c = buf.charAt(buf.length()-1);

        buf.append((char)c);
        if(c == (int)'/' && !in_quotation && !in_doubleQuotation) comment_flag = 1;

        switch (c) {
            case (int)'\'':
                if(pre_c != (int)'\\') {
                    in_quotation = !in_quotation;
                }
                break;
            case (int)'\"':
                if(pre_c != (int)'\\') {
                    in_doubleQuotation = !in_doubleQuotation;
                }
                break;
        }

        if(!in_quotation && !in_doubleQuotation) {
            switch (c) {
                case (int)'(':
                    s_all.push((int)')');
                    break;
                case (int)'[':
                    s_all.push((int)']');
                    break;
                case (int)'{':
                    s_all.push((int) '}');
                    break;

                case (int)')':
                    if(!s_all.empty() && s_all.peek() == (int)')') s_all.pop();
                    else {
                        return -1;
                    }
                    break;
                case (int)']':
                    if(!s_all.empty() && s_all.peek() == (int)']') s_all.pop();
                    else {
                        return -1;
                    }
                    break;
                case (int)'}':
                    if(!s_all.empty() && s_all.peek() == (int)'}') s_all.pop();
                    else {
                        return -1;
                    }
                    break;
            }
        }

        c = input.read();
        if(comment_flag == 1 && c == (int)'/' && !in_quotation && !in_doubleQuotation) {
            buf.deleteCharAt(buf.length()-1);
            return -1;
        }

        return 0;
    }

    public static void addTestDeclaration() throws IOException {
        String fileName = "tem/testDeclaration.java";
        String line;

        FileWriter writer = new FileWriter(fileName);
        writer.write("import java.io.*;\n");
        writer.write("import java.util.*;\n");
        line = "public class testDeclaration {";
        writer.write(line);

        line = "    public static " + Reader.buf.toString();
        writer.write(line);

        line = "}";
        writer.write(line);

        writer.close();
    }

    public static void addTestMultiDeclaration(String s) throws IOException {
        String fileName = "tem/testMultiDeclaration.java";
        String line;

        FileWriter writer = new FileWriter(fileName);
        writer.write("import java.io.*;\n");
        writer.write("import java.util.*;\n");
        line = "public class testMultiDeclaration {";
        writer.write(line);

        line = "    public static " + s;
        writer.write(line);

        line = "}";
        writer.write(line);

        writer.close();
    }
}