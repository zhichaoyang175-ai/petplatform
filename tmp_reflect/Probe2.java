import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Probe2 {
    public static void main(String[] args) throws Exception {
        Class<?> fi = Class.forName("org.dromara.x.file.storage.core.FileInfo");
        System.out.println("=== FileInfo declared fields ===");
        for (Field f : fi.getDeclaredFields()) {
            System.out.println(f.getType().getName() + " " + f.getName());
        }
        System.out.println("=== FilePartInfo getters ===");
        Class<?> fpi = Class.forName("org.dromara.x.file.storage.core.upload.FilePartInfo");
        for (Method m : fpi.getMethods()) {
            String n = m.getName();
            if (n.startsWith("get") && m.getParameterCount() == 0 && !java.lang.reflect.Modifier.isStatic(m.getModifiers()) && !n.equals("getClass")) {
                System.out.println(n + " -> " + m.getReturnType().getName());
            }
        }
    }
}
