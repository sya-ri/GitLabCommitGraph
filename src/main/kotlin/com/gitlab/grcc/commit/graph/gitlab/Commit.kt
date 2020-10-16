package com.gitlab.grcc.commit.graph.gitlab

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.ApiEndPoint.Companion.slashTo2F
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*


data class Commit(val date: Date) {
    companion object {
        private fun Date.truncateTime(): Date {
            val calendar = Calendar.getInstance()
            calendar.time = this
            return GregorianCalendar(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DATE]).time
        }

        @ExperimentalStdlibApi
        fun List<Commit>.compressDate(): Map<CommitDate, Int> {
            return map { it.date.truncateTime() }.groupingBy { CommitDate(it) }.eachCount()
        }
    }
}

data class CommitDate(private val date: Date): Comparable<CommitDate> {
    companion object {
        private val dateFormat = SimpleDateFormat("MM/dd")
    }

    override fun toString(): String {
        return dateFormat.format(date)
    }

    override fun compareTo(other: CommitDate): Int {
        return date.compareTo(other.date)
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