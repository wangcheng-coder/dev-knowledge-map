package mapdemo;

import java.net.URL;
import java.net.URLClassLoader;

public class TwoClassLoaders {
    public static void main(String[] args) throws Exception {
        java.io.File dir = new java.io.File(args[0]).getAbsoluteFile();
        String path = dir.getAbsolutePath().replace('\\', '/');
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        URL url = new URL("file://" + path);
        ClassLoader parent = ClassLoader.getPlatformClassLoader();
        try (URLClassLoader first = new URLClassLoader(new URL[] {url}, parent);
             URLClassLoader second = new URLClassLoader(new URL[] {url}, parent)) {
            Class<?> a = first.loadClass("LoadedTwice");
            Class<?> b = second.loadClass("LoadedTwice");
            System.out.println("两个加载器得到的 Class 是同一个对象: " + (a == b));
            System.out.println("名字都是: " + a.getName());
            Object oa = a.getDeclaredConstructor().newInstance();
            System.out.println("第二个加载器的类型能否接纳第一个的实例: " + b.isInstance(oa));
        }
    }
}
