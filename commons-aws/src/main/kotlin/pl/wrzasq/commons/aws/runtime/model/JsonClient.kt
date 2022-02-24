/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime.model

import com.amazonaws.services.lambda.runtime.Client
import kotlinx.serialization.Serializable

/**
 * JSON implementation of client metadata.
 *
 * @param installationId Installation ID.
 * @param appTitle Application title.
 * @param appVersionName Application version name.
 * @param appVersionCode Application version code.
 * @param appPackageName Application package name.
 */
@Serializable
data class JsonClient(
    private val installationId: String,
    private val appTitle: String,
    private val appVersionName: String,
    private val appVersionCode: String,
    private val appPackageName: String
) : Client {
    override fun getInstallationId() = installationId

    override fun getAppTitle() = appTitle

    override fun getAppVersionName() = appVersionName

    override fun getAppVersionCode() = appVersionCode

    override fun getAppPackageName() = appPackageName
}
