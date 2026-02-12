package com.thefull.m3uparser

data class ParseResult(
    val live: MutableList<M3UItem> = mutableListOf(),
    val movies: MutableList<M3UItem> = mutableListOf(),
    val series: MutableList<M3UItem> = mutableListOf()
)

class M3UParser {

    fun parse(content: String): ParseResult {

        val result = ParseResult()

        val seenLive = HashSet<String>()
        val seenMovies = HashSet<String>()
        val seenSeries = HashSet<String>()

        val lines = content.split("\n")

        var info = ""

        for (raw in lines) {

            val line = raw.trim()
            if (line.isEmpty()) continue

            if (line.startsWith("#EXTINF")) {
                info = line
                continue
            }

            if (!line.startsWith("http") && !line.startsWith("rtmp")) {
                info = ""
                continue
            }

            val url = line

            val name = cleanName(extractName(info))
            val group = normalizeGroup(extractAttr(info, "group-title"))
            val logo = extractAttr(info, "tvg-logo")

            val type = detectType(url, group, name)

            if (type == "live") {

                val k = (name + group + url).lowercase()
                if (!seenLive.contains(k)) {
                    result.live.add(M3UItem(name, url, logo, group))
                    seenLive.add(k)
                }

            } else if (type == "movie") {

                val k = (name + group + url).lowercase()
                if (!seenMovies.contains(k)) {
                    result.movies.add(M3UItem(name, url, logo, group))
                    seenMovies.add(k)
                }

            } else {

                val k = (name + group + url).lowercase()
                if (!seenSeries.contains(k)) {
                    result.series.add(M3UItem(name, url, logo, group))
                    seenSeries.add(k)
                }
            }

            info = ""
        }

        return result
    }

    private fun extractAttr(line: String, key: String): String {
        val r = Regex("""$key="(.*?)"""")
        return r.find(line)?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun extractName(info: String): String {
        val n = info.substringAfter(",", "").trim()
        return if (n.isEmpty()) "Sem Nome" else n
    }

    private fun cleanName(n: String): String {
        var x = n
        x = x.replace(Regex("""\[[^\]]*\]"""), "")
        x = x.replace(Regex("""\([^\)]*\)"""), "")
        x = x.replace(Regex("""\b(4k|8k|uhd|fhd|hd|sd|1080p|720p|2160p)\b""", RegexOption.IGNORE_CASE), "")
        x = x.replace(Regex("""\s+"""), " ")
        return x.trim()
    }

    private fun normalizeGroup(g: String): String {
        var x = g.trim()
        if (x.isEmpty()) return "Canais"
        x = x.replace(Regex("""\s+"""), " ")
        return x
    }

    private fun detectType(url: String, group: String, name: String): String {

        val u = url.lowercase()
        val g = group.lowercase()
        val n = name.lowercase()

        if (u.contains("/series/")) return "series"
        if (u.contains("/movie/")) return "movie"

        if (Regex("""s\d{1,2}e\d{1,2}""").containsMatchIn(n)) return "series"
        if (Regex("""\d{1,2}\s*x\s*\d{1,2}""").containsMatchIn(n)) return "series"

        if (g.contains("serie")) return "series"
        if (g.contains("filme")) return "movie"

        if (Regex("""\.(mp4|mkv|avi|mov|wmv|flv|m4v)($|\?)""").containsMatchIn(u)) return "movie"

        if (Regex("""\.(ts|m3u8|mpd)($|\?)""").containsMatchIn(u)) return "live"

        return "live"
    }
}
