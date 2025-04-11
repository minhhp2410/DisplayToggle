import android.os.Build;

import android.os.IBinder;

import java.lang.reflect.Method;

import java.lang.reflect.InvocationTargetException;



public class DisplayToggle {



	private static final Class<?> SURFACE_CONTROL_CLASS;

	private static final Class<?> DISPLAY_CONTROL_CLASS;



	static {

		try {

			SURFACE_CONTROL_CLASS = Class.forName("android.view.SurfaceControl");

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

				Class<?> classLoaderFactoryClass = Class.forName("com.android.internal.os.ClassLoaderFactory");

				Method createClassLoaderMethod = classLoaderFactoryClass.getDeclaredMethod("createClassLoader",

						String.class, String.class, String.class, ClassLoader.class, int.class, boolean.class, String.class);

				ClassLoader classLoader = (ClassLoader) createClassLoaderMethod.invoke(null,

						"/system/framework/services.jar", null, null, ClassLoader.getSystemClassLoader(), 0, true, null);

				DISPLAY_CONTROL_CLASS = classLoader.loadClass("com.android.server.display.DisplayControl");



				Method loadLibraryMethod = Runtime.class.getDeclaredMethod("loadLibrary0", Class.class, String.class);

				loadLibraryMethod.setAccessible(true);

				loadLibraryMethod.invoke(Runtime.getRuntime(), DISPLAY_CONTROL_CLASS, "android_servers");

			} else {

				DISPLAY_CONTROL_CLASS = null;

			}

		} catch (Exception e) {

			throw new AssertionError(e);

		}

	}



	public static void main(String... args) {

		System.out.println("Display mode: " + args[0]);

		int mode = Integer.parseInt(args[0]);



		try {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && DISPLAY_CONTROL_CLASS != null) {

				Method getPhysicalDisplayIdsMethod = DISPLAY_CONTROL_CLASS.getMethod("getPhysicalDisplayIds");

				Method getPhysicalDisplayTokenMethod = DISPLAY_CONTROL_CLASS.getMethod("getPhysicalDisplayToken", long.class);



				long[] displayIds = (long[]) getPhysicalDisplayIdsMethod.invoke(null);

				if (displayIds != null) {

					for (long displayId : displayIds) {

						IBinder token = (IBinder) getPhysicalDisplayTokenMethod.invoke(null, displayId);

						setDisplayPowerMode(token, mode);

					}

				}

			} else {

				setDisplayPowerMode(getBuiltInDisplay(), mode);

			}

		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			System.exit(0);

		}

	}



	private static IBinder getBuiltInDisplay() throws Exception {

		Method method;

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

			method = SURFACE_CONTROL_CLASS.getMethod("getBuiltInDisplay", int.class);

			return (IBinder) method.invoke(null, 0);

		} else {

			method = SURFACE_CONTROL_CLASS.getMethod("getInternalDisplayToken");

			return (IBinder) method.invoke(null);

		}

	}



	private static void setDisplayPowerMode(IBinder displayToken, int mode) throws Exception {

		Method method = SURFACE_CONTROL_CLASS.getMethod("setDisplayPowerMode", IBinder.class, int.class);

		method.invoke(null, displayToken, mode);

	}

}
