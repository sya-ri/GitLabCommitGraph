package com.gitlab.grcc.commit.graph.gitlab

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.ApiEndPoint.Companion.slashTo2F
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import com.google.gson.Gson
import org.jfree.data.time.Day
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

data class Commit(val date: Date) {
    companion object {
        private fun Date.truncateTime(): Date {
            val calendar = Calendar.getInstance()
            calendar.time = this
            return GregorianCalendar(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DATE]).time
        }

        @ExperimentalStdlibApi
        fun List<Commit>.compressDate(): Map<Day, Int> {
            return map { it.date.truncateTime() }.groupingBy { Day(it) }.eachCount()
        }
    }
}

@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllCommits(projects: Set<Project>): List<Commit> {
    return buildList {
        projects.forEach { project ->
            val json = requestJson(
                ApiEndPoint.GetCommit(project.groupId.slashTo2F),
                "all" to "true"
            ) ?: return@forEach
            json.asJsonArray.forEach {
                val jsonObject = it.asJsonObject
                val dateJson = jsonObject["created_at"]
                val date = Gson().fromJson(dateJson, Date::class.java)
                add(Commit(date))
            }
        }
    }
}