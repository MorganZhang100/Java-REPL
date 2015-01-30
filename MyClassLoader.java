/**
 * Created by Morgan on 1/29/15.
 * This original code comes from http://www.onjava.com/pub/a/onjava/2003/11/12/classloader.html
 */
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MyClassLoader
        extends ClassLoader {

    private String mDirectory;

    public MyClassLoader(String pDirectory) {
        super();
        mDirectory = pDirectory;
    }

    protected Class findClass(String pClassName)
            throws ClassNotFoundException {
        try {
            System.out.println( "Current dir: " + new File( mDirectory ).getAbsolutePath() );
            File lClassFile = new File( mDirectory, pClassName + ".class" );
            InputStream lInput = new BufferedInputStream( new FileInputStream( lClassFile ) );
            ByteArrayOutputStream lOutput = new ByteArrayOutputStream();
            int i = 0;
            while( ( i = lInput.read() ) >= 0 ) {
                lOutput.write( i );
            }
            byte[] lBytes = lOutput.toByteArray();
            return defineClass( pClassName, lBytes, 0, lBytes.length );
        } catch( Exception e ) {
            throw new ClassNotFoundException( "Class: " + pClassName + " could not be found" );
        }
    }

    public Class loadClass( String pClassName, boolean pResolve ) throws ClassNotFoundException {
        System.out.println( "loadClass(), resolve: " + pResolve );
        Class lClass = findLoadedClass( pClassName );
        if( lClass == null ) {
            try {
                ClassLoader lParent = getParent();
                if( lParent == null) {
                    lClass = getSystemClassLoader().loadClass( pClassName );
                } else {
                    lClass = lParent.loadClass(pClassName );
                }
            } catch( ClassNotFoundException cnfe ) {
                lClass = findClass( pClassName );
            }
        }
        try {
            if( pResolve ) {
                System.out.println( "resolve class: " + lClass );
                resolveClass( lClass );
            }
        } catch (Error e) {
            e.printStackTrace();
            throw e;
        }
        return lClass;
    }
}
