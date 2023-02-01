/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2023 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.json

import kotlinx.serialization.json.Json

/**
 * Default JSON serialization configuration.
 */
val Default = Json(from = Json.Default) {
    ignoreUnknownKeys = true
}
