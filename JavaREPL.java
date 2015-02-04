import com.sun.source.util.JavacTask;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class JavaREPL {
    static boolean debug = false;

	public static void main(String[] args) throws IOException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        File temDir = new File("tem/");
        deleteAll(temDir);

        if(!temDir.exists() && !temDir.isDirectory()) temDir.mkdir();

        URL[] urls = new URL[] { temDir.toURI().toURL() };
        URLClassLoader ul = new URLClassLoader(urls);

        int inputResult;

        while(true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            if(Reader.isToBeClean()) System.out.print(">");

            String text = br.readLine();

            if(text == null) {
                deleteAll(temDir);
                return;
            }

            inputResult = Reader.getNewInput(text);
            if(inputResult == -1) continue;

            if(Reader.isToBeClean()) {
                if(Reader.isDeclaration()) {
                    addDeclarationPartOne();
                    while(Reader.stillTestMultiDeclaration()) {
                        addMultiDeclaration(Reader.getMultiDeclaration());
                    }
                    addMultiDeclarationPartTwo();
                }
                else {
                    addStatement(Reader.getNestedString());
                }

                if(compile(Reader.getClassIndex())) {
                    run(ul, Reader.getClassIndex());
                } else{
                    Reader.decreaseClassIndex();
                }
            }
        }
    }

    public static void addDeclaration(String s) throws IOException {
        String fileName = "tem/REPL_t_" + Reader.getClassIndex() + ".java";

        FileWriter writer = new FileWriter(fileName);

        writer.write("import java.io.*;\n");
        writer.write("import java.util.*;\n");

        String line;

        if(Reader.getClassIndex() != 1) {
            String classFrom;
            classFrom = "REPL_t_" + (Reader.getClassIndex() - 1);

            line = "public class REPL_t_" + Reader.getClassIndex() + " extends " + classFrom + " {";
            writer.write(line);
        }
        else {
            line = "public class REPL_t_1 {";
            writer.write(line);
        }

        line = "\n    public static " + s;
        writer.write(line);

        line = "\n    public static void exec() {";
        writer.write(line);

        line = "\n    }";
        writer.write(line);

        line = "\n}";
        writer.write(line);

        writer.close();
    }

    public static void addDeclarationPartOne() throws IOException {
        String fileName = "tem/REPL_t_" + Reader.getClassIndex() + ".java";
        FileWriter writer = new FileWriter(fileName);

        writer.write("import java.io.*;\n");
        writer.write("import java.util.*;\n");

        String line;

        if(Reader.getClassIndex() != 1) {
            String classFrom;
            classFrom = "REPL_t_" + (Reader.getClassIndex() - 1);

            line = "public class REPL_t_" + Reader.getClassIndex() + " extends " + classFrom + " {";
            writer.write(line);
        }
        else {
            line = "public class REPL_t_1 {";
            writer.write(line);
        }

        writer.close();
    }

    public static void addMultiDeclarationPartTwo() throws IOException {
        String fileName = "tem/REPL_t_" + Reader.getClassIndex() + ".java";
        FileWriter writer = new FileWriter(fileName,true);
        String line;

        line = "\n    public static void exec() {";
        writer.write(line);

        line = "\n    }";
        writer.write(line);

        line = "\n}";
        writer.write(line);

        writer.close();
    }

    public static void addMultiDeclaration(String s) throws IOException {
        String fileName = "tem/REPL_t_" + Reader.getClassIndex() + ".java";
        FileWriter writer = new FileWriter(fileName,true);

        String line;

        line = "\n    public static " + s;
        writer.write(line);

        writer.close();
    }

    public static void addStatement(String s) throws IOException {
        String fileName = "tem/REPL_t_" + Reader.getClassIndex() + ".java";

        FileWriter writer = new FileWriter(fileName);

        writer.write("import java.io.*;\n");
        writer.write("import java.util.*;\n");

        String line;

        if(Reader.getClassIndex() != 1) {
            String classFrom;
            classFrom = "REPL_t_" + (Reader.getClassIndex() - 1);

            line = "public class REPL_t_" + Reader.getClassIndex() + " extends " + classFrom + " {";
            writer.write(line);
        }
        else {
            line = "public class REPL_t_1 {";
            writer.write(line);
        }

        line = "\n    public static void exec() {";
        writer.write(line);

        line = "\n      " + s;
        writer.write(line);

        line = "\n    }";
        writer.write(line);

        line = "\n}";
        writer.write(line);

        writer.close();
    }

    //This function's original code comes from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
    public static boolean compile(int classIndex) throws IOException {
        List c = new ArrayList();
        for (int i=1; i<=classIndex; i++) {
            c.add("tem/REPL_t_" + i + ".java");
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(c);
        JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

        boolean ok = task.call();

        if(debug) {
            System.out.print("call(): ");
            System.out.print(ok);
            System.out.println();
        }

        //This loop original code comes from http://docs.oracle.com/javase/7/docs/api/javax/tools/JavaCompiler.html
        for (Diagnostic diagnostic : diagnostics.getDiagnostics())
            System.err.format("line %d:  %s%n", diagnostic.getLineNumber(), diagnostic.getMessage(null));

        fileManager.close();

        return ok;
    }

    //This original code comes from http://www.onjava.com/pub/a/onjava/2003/11/12/classloader.html
    public static void run(URLClassLoader ul, int classIndex) throws IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException, MalformedURLException, NoSuchMethodException {
        if(debug) System.out.println("Going to load REPL_t_" + classIndex);

        String className = "REPL_t_" + classIndex;
        Class<?> clazz = ul.loadClass(className);

        if(debug) System.out.println("Class has been successfully loaded");

        Method method = clazz.getDeclaredMethod("exec");

        Object object = clazz.newInstance();
        if(debug) System.out.print("Before exec():\n");
        try {
            method.invoke(object);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if(debug) System.out.print("\nAfter exec()\n\n");
    }

    //This function's origional code comes from http://blog.csdn.net/love_ubuntu/article/details/6673722
    public static void deleteAll(File file){
        if(file.exists() || file.isDirectory()) {
            if(file.isFile() || file.list().length ==0)
            {
                file.delete();
            }else{
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteAll(files[i]);
                    files[i].delete();
                }

                if(file.exists()) file.delete();
            }
        }

    }
}