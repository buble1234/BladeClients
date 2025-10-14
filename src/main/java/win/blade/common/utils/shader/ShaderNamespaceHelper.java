package win.blade.common.utils.shader;

public class ShaderNamespaceHelper {
    private static final ThreadLocal<String> customNamespace = ThreadLocal.withInitial(() -> null);
    
    public static void setCustomNamespace(String namespace) {
        customNamespace.set(namespace);
    }
    
    public static String getCustomNamespace() {
        return customNamespace.get();
    }
    
    public static void clearCustomNamespace() {
        customNamespace.remove();
    }
}