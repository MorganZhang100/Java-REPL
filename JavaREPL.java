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
	public static void main(String[] args) throws IOException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        File toRenew = new File("tem/");
        JavaREPL.deleteAll(toRenew);

        if(!toRenew.exists() && !toRenew.isDirectory()) toRenew.mkdir();

        File file = new File( "tem/" );
        URL[] urls = new URL[] { file.toURI().toURL() };
        URLClassLoader ul = new URLClassLoader(urls);

        while(true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(">");
            String text = br.readLine();

            Reader.getNewInput(text);

            if(Reader.isToBeClean()) {
                if(Reader.isDeclaration()) {
                    addDeclaration(Reader.getNestedString());
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

    public static void addLineToFile(String fileName, String line) throws IOException {
        FileWriter writer = new FileWriter(fileName,true);
        writer.write(line + "\n");
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
        //Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList("tem/REPL_t_" + Reader.getClassIndex() + ".java"));
        JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

        boolean ok = task.call();

        System.out.print("call(): ");
        System.out.print(ok);
        System.out.println();

        //This loop original code comes from http://docs.oracle.com/javase/7/docs/api/javax/tools/JavaCompiler.html
        for (Diagnostic diagnostic : diagnostics.getDiagnostics())
            System.err.format("line %d:  %s%n", diagnostic.getLineNumber(), diagnostic.getMessage(null));

        fileManager.close();

        return ok;
    }

    //This original code comes from http://www.onjava.com/pub/a/onjava/2003/11/12/classloader.html
    public static void run(URLClassLoader ul, int classIndex) throws IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException, MalformedURLException, NoSuchMethodException {
        System.out.println("Going to load REPL_t_" + classIndex);

        String className = "REPL_t_" + classIndex;
        Class<?> clazz = ul.loadClass(className);

        System.out.println("Class has been successfully loaded");

        Method method = clazz.getDeclaredMethod("exec", null);

        Object object = clazz.newInstance();
        System.out.print("Before exec():\n");
        method.invoke(object, null);
        System.out.print("\nAfter exec()\n\n");
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

                if(file.exists())         //如果文件本身就是目录 ，就要删除目录
                    file.delete();
            }
        }

    }
}