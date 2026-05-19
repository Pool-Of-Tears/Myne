/**
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
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

package com.starry.myne.helpers

import android.app.NotificationManager
import android.content.Context

/**
 * A helper class to manage Do Not Disturb (Zen) mode.
 *
 * @param context The context to use
 */
class ZenModeManager(context: Context) {
    private val nm = context.getSystemService(NotificationManager::class.java)
    private var originalFilter = NotificationManager.INTERRUPTION_FILTER_ALL

    /**
     * Enable Do Not Disturb mode.
     */
    fun enable() {
        if (nm.isNotificationPolicyAccessGranted) {
            val current = nm.currentInterruptionFilter
            if (current != NotificationManager.INTERRUPTION_FILTER_NONE) {
                originalFilter = current
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
        }
    }

    /**
     * Disable Do Not Disturb mode and restore the original filter.
     */
    fun disable() {
        if (nm.isNotificationPolicyAccessGranted) {
            nm.setInterruptionFilter(originalFilter)
        }
    }

    /**
     * Update the original filter to the current interruption filter.
     * Use this to honor user overrides while DND is enabled.
     */
    fun updateOriginalFilter() {
        if (nm.isNotificationPolicyAccessGranted) {
            originalFilter = nm.currentInterruptionFilter
        }
    }
}
