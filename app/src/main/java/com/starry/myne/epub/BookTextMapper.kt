package com.starry.myne.epub


import org.jsoup.Jsoup


object BookTextMapper {
    // <img yrel="{float}"> {uri} </img>
    data class ImgEntry(val path: String, val yrel: Float) {
        /**
         * Deprecated versions: v0
         * Current versions: v1
         */
        companion object {

            fun fromXMLString(text: String): ImgEntry? {
                return fromXMLString_v0(text) ?: fromXMLString_v1(text)
            }

            private fun fromXMLString_v1(text: String): ImgEntry? {
                return Jsoup.parse(text).selectFirst("img")?.let {
                    ImgEntry(
                        path = it.attr("src") ?: return null,
                        yrel = it.attr("yrel").toFloatOrNull() ?: return null
                    )
                }
            }

            private val XMLForm_v0 = """^\W*<img .*>.+</img>\W*$""".toRegex()

            private fun fromXMLString_v0(text: String): ImgEntry? {
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
            return toXMLString_v1()
        }

        private fun toXMLString_v1(): String {
            return """<img src="$path" yrel="${"%.2f".format(yrel)}">"""
        }

        private fun toXMLString_v0(): String {
            return """<img yrel="${"%.2f".format(yrel)}">$path</img>"""
        }
    }
}