/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app

object FeatureToggle {

    private const val ENABLE_CHANGES = true

    const val DISABLE_SERVER_CHANGE = ENABLE_CHANGES
    const val DISABLE_ENC_BY_DEFAULT = ENABLE_CHANGES
    const val ROUTE_USER_DIRECTLY_TO_LOGIN = ENABLE_CHANGES
    const val DISABLE_SLASH_COMMANDS = false
    const val DISABLE_FULL_ENCRYPTION = ENABLE_CHANGES
    const val DISABLE_ROUTE_SPACE_BETA_HEADER = ENABLE_CHANGES
    const val DISABLE_ANALYTICS = ENABLE_CHANGES
    const val REMOVE_COPYRIGHT = ENABLE_CHANGES
}
