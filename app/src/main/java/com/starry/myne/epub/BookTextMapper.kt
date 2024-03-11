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


package com.starry.myne.epub

import org.jsoup.Jsoup
import java.util.Locale

object BookTextMapper {

    // <img yrel="{float}"> {uri} </img>
    data class ImgEntry(val path: String, val yrel: Float) {

        /**
         * Deprecated versions: v0
         * Current versions: v1
         */
        companion object {

            fun fromXMLString(text: String): ImgEntry? {
                return fromXMLStringV0(text) ?: fromXMLStringV1(text)
            }

            private fun fromXMLStringV1(text: String): ImgEntry? {
                return Jsoup.parse(text).selectFirst("img")?.let {
                    ImgEntry(
                        path = it.attr("src") ?: return null,
                        yrel = it.attr("yrel").toFloatOrNull() ?: return null
                    )
                }
            }

            private val XMLForm_v0 = """^\W*<img .*>.+</img>\W*$""".toRegex()

            private fun fromXMLStringV0(text: String): ImgEntry? {
                // Fast discard filter
                if (!text.matches(XMLForm_v0))
                    return null
                return parseXMLText(text)?.selectFirstTag("img")?.let {
                    ImgEntry(
                        path = it.textContent ?: return null,
                        yrel = it.getAttributeValue("yrel")?.toFloatOrNull() ?: return null
                    )
                }
            }
        }

        fun toXMLString(): String {
            return toXMLStringV1()
        }

        private fun toXMLStringV1(): String {
            return """<img src="$path" yrel="${"%.2f".format(Locale.US, yrel)}">"""
        }

        /*
        private fun toXMLStringV0(): String {
            return """<img yrel="${"%.2f".format(yrel)}">$path</img>"""
        }
        */
    }
}