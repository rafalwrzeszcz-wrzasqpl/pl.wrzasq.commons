/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.daemon;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Properties;

/**
 * Class for maintaining core application metadata.
 *
 * <p>
 * Note: This class was primarily designed for the needs that were very specific for Chillout Development. The general
 * usage of this class will probably be very limited, we put it here to move more of our codebase available publicly.
 * </p>
 */
public class Package
{
    /**
     * Default property name for version.
     */
    public static final String PROPERTY_VERSION = "application.version";

    /**
     * Default core properties file path.
     */
    public static final String DEFAULT_RESOURCE = "/META-INF/application.properties";

    /**
     * Default application package.
     */
    public static final Package DEFAULT_PACKAGE = new Package();

    /**
     * Package version.
     */
    protected String version;

    /**
     * Initialize with default properties resource.
     */
    public void init()
    {
        this.init(Package.class.getResource(Package.DEFAULT_RESOURCE));
    }

    /**
     * Initialize properties from given resource.
     *
     * @param url Properties location.
     */
    public void init(URL url)
    {
        // default version - if production properties are not there
        this.version = "devel";

        if (url != null) {
            Properties properties = new Properties();
            try (InputStream stream = url.openStream()) {
                properties.load(stream);
                this.version = properties.getProperty(Package.PROPERTY_VERSION);
            } catch (IOException error) {
                // it's not a critical problem in our case, just report it
                this.version = "error";
            }
        }
    }

    /**
     * Returns package version.
     *
     * @return Current version.
     */
    public synchronized String getVersion()
    {
        // first try to load the version from properties file
        if (this.version == null) {
            this.init();
        }

        return this.version;
    }
}
