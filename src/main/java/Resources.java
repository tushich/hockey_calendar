import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public interface Resources {
    public static String getResource(String name)
    {
        String resource = System.getenv(name);
        if(resource == null) {
            Properties prop = new Properties();
            InputStream fis = null;
            try {
                fis = new FileInputStream("src/main/resources/credential.prop");
                prop.load(fis);
                resource = prop.getProperty(name);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resource;
    }
}
