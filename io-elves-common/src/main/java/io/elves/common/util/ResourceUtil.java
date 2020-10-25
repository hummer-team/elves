package io.elves.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class ResourceUtil {
    private static final String CLASSPATH_PREFIX = "classpath:";

    /**
     * Returns the URL of the resource on the classpath.
     *
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
     */
    public static URL getResourceUrl(String resource) throws IOException {
        if (resource.startsWith(CLASSPATH_PREFIX)) {
            String path = resource.substring(CLASSPATH_PREFIX.length());

            ClassLoader classLoader = ResourceUtil.class.getClassLoader();

            URL url = (classLoader != null ? classLoader.getResource(path) : ClassLoader.getSystemResource(path));
            if (url == null) {
                throw new FileNotFoundException("Resource [" + resource + "] does not exist");
            }

            return url;
        }

        try {
            return new URL(resource);
        } catch (MalformedURLException ex) {
            return new File(resource).toURI().toURL();
        }
    }

    /**
     * Returns the URL of the resource on the classpath.
     *
     * @param loader   The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
     */
    public static URL getResourceUrl(ClassLoader loader, String resource) throws IOException {
        URL url = null;
        if (loader != null) {
            url = loader.getResource(resource);
        }
        if (url == null) {
            url = ClassLoader.getSystemResource(resource);
        }
        if (url == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return url;
    }

    /**
     * Returns a resource on the classpath as a Stream object.
     *
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        ClassLoader loader = ResourceUtil.class.getClassLoader();
        return getResourceAsStream(loader, resource);
    }

    /**
     * Returns a resource on the classpath as a Stream object.
     *
     * @param loader   The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
     */
    public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
        InputStream in = null;
        if (loader != null) {
            in = loader.getResourceAsStream(resource);
        }
        if (in == null) {
            in = ClassLoader.getSystemResourceAsStream(resource);
        }
        if (in == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return in;
    }

    /**
     * Returns a resource on the classpath as a Properties object.
     *
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
     */
    public static Properties getResourceAsProperties(String resource) throws IOException {
        ClassLoader loader = ResourceUtil.class.getClassLoader();
        return getResourceAsProperties(loader, resource);
    }

    /**
     * Returns a resource on the classpath as a Properties object.
     *
     * @param loader   The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
     */
    public static Properties getResourceAsProperties(ClassLoader loader, String resource) throws IOException {
        Properties props = new Properties();
        InputStream in = getResourceAsStream(loader, resource);
        props.load(in);
        in.close();
        return props;
    }

    /**
     * Returns a resource on the classpath as a Reader object.
     *
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
     */
    public static InputStreamReader getResourceAsReader(String resource, String charsetName) throws IOException {
        return new InputStreamReader(getResourceAsStream(resource), charsetName);
    }

    /**
     * Returns a resource on the classpath as a Reader object.
     *
     * @param loader   The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
     */
    public static Reader getResourceAsReader(ClassLoader loader, String resource, String charsetName)
            throws IOException {
        return new InputStreamReader(getResourceAsStream(loader, resource), charsetName);
    }

    /**
     * Returns a resource on the classpath as a File object.
     *
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
     */
    public static File getResourceAsFile(String resource) throws IOException {
        return new File(getResourceUrl(resource).getFile());
    }

    /**
     * Returns a resource on the classpath as a File object.
     *
     * @param url The resource url to find
     * @return The resource
     */
    public static File getResourceAsFile(URL url) {
        return new File(url.getFile());
    }

    /**
     * Returns a resource on the classpath as a File object.
     *
     * @param loader   The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
     */
    public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
        return new File(getResourceUrl(loader, resource).getFile());
    }

}
