import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public interface Resources {
    static String getResource(String name)
    {
        String resource = System.getenv(name);
        if(resource == null) {
            Properties prop = new Properties();
            InputStream fis = null;
            try {
                URL credUrl = Resources.class.getClassLoader().getResource("credential.prop");// "src/main/resources/credential.prop"
                fis = new FileInputStream(credUrl.getPath());
                prop.load(fis);
                resource = prop.getProperty(name);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(resource == null || resource.isEmpty()) throw new NullPointerException("Не найден ресурс с именем " + name);
        return resource;
    }
}
