package com.nativelibs4java.opencl.library;

import com.bridj.*;
import com.bridj.ann.*;
import static com.bridj.Pointer.*;

/**
 * JNA Wrapper for library <b>OpenGL</b><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>, <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
@Library("OpenGL")
@com.bridj.ann.Runtime(CRuntime.class)
public class OpenGLContextUtils {
    static {
        // TODO BridJ.setLibraryName("OpenGL", JNI.isWindows() ? "OpenGL32" : JNI.isLinux() ? "GL" : "OpenGL");
        BridJ.register();
    }
	//public static final java.lang.String JNA_LIBRARY_NAME = com.ochafik.lang.jnaerator.runtime.LibraryExtractor.getLibraryPath("OpenGL", true, OpenGLApple.class);
	//public static final com.sun.jna.NativeLibrary JNA_NATIVE_LIB = com.sun.jna.NativeLibrary.getInstance(OpenGLApple.JNA_LIBRARY_NAME, com.ochafik.lang.jnaerator.runtime.MangledFunctionMapper.DEFAULT_OPTIONS);
	public static final OpenGLContextUtils INSTANCE = new OpenGLContextUtils();
	
	/// Original signature : <code>CGLShareGroupObj CGLGetShareGroup(CGLContextObj)</code>
	public static native Pointer<?> CGLGetShareGroup(Pointer<?> ctx);
	/**
	 * * Current context functions<br>
	 * Original signature : <code>CGLError CGLSetCurrentContext(CGLContextObj)</code>
	 */
	public static native int CGLSetCurrentContext(Pointer<?> ctx);
	/// Original signature : <code>CGLContextObj CGLGetCurrentContext()</code>
	public static native Pointer<?> CGLGetCurrentContext();
	
	/**
	 * * Version numbers<br>
	 * Original signature : <code>void CGLGetVersion(GLint*, GLint*)</code>
	 */
	public static native void CGLGetVersion(Pointer<Integer> majorvers, Pointer<Integer> minorvers);
	/**
	 * * Convert an error code to a string<br>
	 * Original signature : <code>char* CGLErrorString(CGLError)</code><br>
	 * @param error @see CGLError
	 */
	public static native Pointer<Byte> CGLErrorString(int error);

    public static native Pointer<?> wglGetCurrentDC();
    public static native Pointer<?> wglGetCurrentContext();
    public static native Pointer<?> glXGetCurrentDisplay();
    public static native Pointer<?> glXGetCurrentContext();
}
