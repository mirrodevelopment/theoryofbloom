import java.nio.file.Paths;

public class CheckDir {
    public static void main(String[] args) {
        System.out.println("user.dir: " + System.getProperty("user.dir"));
        System.out.println("Paths.get(...): " + Paths.get(System.getProperty("user.dir"), "content", "site-content.json").toAbsolutePath());
    }
}
