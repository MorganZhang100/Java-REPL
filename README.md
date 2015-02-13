# Morgan Zhang's Java-REPL Project
A Java REPL project for CS652 class. For more information please see [here](https://github.com/parrt/cs652/blob/master/README.md "More Info").

Source file are JavaREPL.java and Reader.java.  

##Java-REPL
This is a read-eval-print loop (REPL) interface for Java code similar to Python's interactive shell.  
And it looks like this (running in the terminal):
```
>int a = 10;
>int b = 20;
>print a + b;
30
>class T {
	int y;
	void f() {
		System.out.println("T:f");
	}
}
>T t = new T();
>t.f();
T:f
```

##About compile
Before compile with javac, tools.jar need to be put in CLASSPATH
