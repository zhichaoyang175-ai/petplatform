import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Probe {
    public static void main(String[] args) throws Exception {
        Class<?> fi = Class.forName("org.dromara.x.file.storage.core.FileInfo");
        System.out.println("=== FileInfo getters ===");
        for (Method m : fi.getMethods()) {
            String n = m.getName();
            if (n.startsWith("get") && m.getParameterCount() == 0 && !Modifier.isStatic(m.getModifiers())) {
                System.out.println(n + " -> " + m.getReturnType().getName());
            }
        }
        System.out.println("=== FileInfo setters ===");
        for (Method m : fi.getMethods()) {
            String n = m.getName();
            if (n.startsWith("set") && m.getParameterCount() == 1 && !Modifier.isStatic(m.getModifiers())) {
                System.out.println(n + " -> " + m.getParameterTypes()[0].getName());
            }
        }
        System.out.println("=== FileRecorder methods ===");
        Class<?> fr = Class.forName("org.dromara.x.file.storage.core.recorder.FileRecorder");
        for (Method m : fr.getMethods()) {
            System.out.println(m.getReturnType().getName() + " " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ")");
        }
    }
}
