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

package im.vector.app.config

import im.vector.app.BuildConfig
import im.vector.app.FeatureToggle
import im.vector.app.features.analytics.AnalyticsConfig

val analyticsConfig: AnalyticsConfig = object : AnalyticsConfig {
    override val isEnabled = !FeatureToggle.DISABLE_ANALYTICS && BuildConfig.APPLICATION_ID == "im.vector.app.debug"
    override val postHogHost = "https://posthog-poc.lab.element.dev"
    override val postHogApiKey = "rs-pJjsYJTuAkXJfhaMmPUNBhWliDyTKLOOxike6ck8"
    override val policyLink = "https://element.io/cookie-policy"
}
