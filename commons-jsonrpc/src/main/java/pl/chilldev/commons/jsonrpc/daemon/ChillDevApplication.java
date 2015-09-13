/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.daemon;

import pl.chilldev.commons.daemon.Package;

/**
 * Basic implementation of application class used as a stub in Chillout Development projects.
 */
public class ChillDevApplication extends AbstractSpringApplication
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDaemonName()
    {
        return this.getPackageToScan();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDaemonVersion()
    {
        return Package.DEFAULT_PACKAGE.getVersion();
    }
}
