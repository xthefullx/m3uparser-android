package com.thefull.m3uparser

class M3UParser {

    fun parse(content: String): List<M3UItem> {

        val list = mutableListOf<M3UItem>()
        val lines = content.split("\n")

        var name = ""
        var logo = ""
        var group = ""

        for (line in lines) {

            if (line.startsWith("#EXTINF")) {

                name = line.substringAfter(",").trim()

                logo = Regex("""tvg-logo="(.*?)"""")
                    .find(line)?.groupValues?.get(1) ?: ""

                group = Regex("""group-title="(.*?)"""")
                    .find(line)?.groupValues?.get(1) ?: ""

            } else if (line.startsWith("http")) {

                list.add(
                    M3UItem(
                        name = name,
                        url = line.trim(),
                        logo = logo,
                        group = group
                    )
                )
            }
        }

        return list
    }
}
