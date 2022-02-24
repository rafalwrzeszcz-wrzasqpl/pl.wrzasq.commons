/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.runtime.model

import com.amazonaws.services.lambda.runtime.CognitoIdentity
import kotlinx.serialization.Serializable

/**
 * JSON implementation of Cognito identity metadata.
 *
 * @param identityId Identity ID.
 * @param identityPoolId Cognito identity pool ID.
 */
@Serializable
data class JsonCognitoIdentity(
    private val identityId: String,
    private val identityPoolId: String
) : CognitoIdentity {
    override fun getIdentityId() = identityId

    override fun getIdentityPoolId() = identityPoolId
}
