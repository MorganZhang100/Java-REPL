import com.sun.source.util.JavacTask;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;

public class JavaREPL {
	public static void main(String[] args) throws IOException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        newSoftWare();

        while(true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(">");
            String text = br.readLine();
            StringReader sr = new StringReader(text);
            BufferedReader br_line = new BufferedReader(sr);
            Reader.getNewInput(br_line);
            System.out.println(Reader.isDeclaration());
            if(Reader.isDeclaration()) {
                addDeclaration(Reader.getNestedString());
            }
            compile();
            run();
        }
    }

    public static void newSoftWare() throws IOException {
        String fileName="tem/REPL_t.java";

        FileWriter writer = new FileWriter(fileName);
        writer.write("import java.io.*;\n");
        writer.write("import java.util.*;\n");
        writer.write("public class REPL_t {\n");
        writer.write("}\n");
        writer.close();
    }

    public static void addDeclaration(String s) throws IOException {
        String fileName = "tem/REPL_t_" + (Reader.getClassAmount() + 1) + ".java";

        FileWriter writer = new FileWriter(fileName);

        writer.write("import java.io.*;\n");
        writer.write("import java.util.*;\n");

        String line;
        String classFrom;
        if(Reader.getClassAmount() == 0) classFrom = "REPL_t";
        else classFrom = "REPL_t_" + Reader.getClassAmount();

        line = "\npublic class REPL_t_" + (Reader.getClassAmount() + 1) + " extends " + classFrom + " {";
        writer.write(line);

        line = "\n    public static " + s;
        writer.write(line);

        line = "\n    public static void exec() {System.out.println(\"KKKKKKKK\");";
        writer.write(line);

        line = "\n    }";
        writer.write(line);

        line = "\n}";
        writer.write(line);

        writer.close();
    }

    public static void addLineToFile(String fileName, String line) throws IOException {
        FileWriter writer = new FileWriter(fileName,true);
        writer.write(line + "\n");
        writer.close();
    }

    //This function's original code comes from https://github.com/parrt/cs652/blob/master/projects/Java-REPL.md
    public static boolean compile() throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList("tem/REPL_t.java","tem/REPL_t_1.java"));
        JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

        boolean ok = task.call();
        System.out.println(ok);
        fileManager.close();

        return true;
    }

    //This original code comes from http://www.onjava.com/pub/a/onjava/2003/11/12/classloader.html
    public static void run() throws IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException, MalformedURLException, NoSuchMethodException {
        MyClassLoader ClassLoader_A = new MyClassLoader( "tem/" );
        Class clazz = ClassLoader_A.findClass("REPL_t_1");
        System.out.println("Class has been successfully loaded");

        Method method = clazz.getDeclaredMethod("exec", null);

        Object object = clazz.newInstance();
        method.invoke(object, null);
    }
}