package de.unibremen.opensores.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ServerProperties Wrapper Class for getting
 * a server config Properties Object.
 *
 * @author Lorenz Huether
 */
public class ServerProperties {

    public static Properties props = null;

    /**
    * Constructor set to private because of
    * Singleton pattern.
    */
    private ServerProperties() {}

    /**
    * Opens the properties File.
    *
    * @return Properties Server properties.
    *
    * @throws IOException If the file cannot be opened.
    */
    public static Properties getProperties() throws IOException {
        if (props == null) {
            props = new Properties();
            InputStream in = ServerProperties.class.getClassLoader()
                .getResourceAsStream("config.properties");
            props.load(in);
            in.close();
        }
        return props;
    }

    /**
    * Returns a value from theproperties File.
    *
    * @param namespace of properties.
    *
    * @param key name of the key.
    *
    * @return String value.
    *
    * @throws IOException If the file cannot be opened.
    */
    public static String getValue(String namespace, String key)
            throws IOException {
        if (props == null) {
            props = new Properties();
            InputStream in = ServerProperties.class.getClassLoader()
                .getResourceAsStream("config.properties");
            props.load(in);
            in.close();
        }
        String req = namespace + "." + key;
        return props.getProperty(req);
    }
}
