package com.gitlab.grcc.commit.graph.gitlab

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.ApiEndPoint.Companion.slashTo2F
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import com.google.gson.Gson
import org.jfree.data.time.Day
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.SortedMap

/**
 * コミットデータ
 */
data class Commit(val date: Date) {
    companion object {
        /**
         * 時刻データを破棄する
         */
        private fun Date.truncateTime(): Date {
            val calendar = Calendar.getInstance()
            calendar.time = this
            return GregorianCalendar(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DATE]).time
        }

        /**
         * コミット一覧を 日付とコミット数 の一覧に変換
         */
        @ExperimentalStdlibApi
        fun List<Commit>.compress(): SortedMap<Day, Int> {
            return map { it.date.truncateTime() }.groupingBy { Day(it) }.eachCount().toSortedMap()
        }
    }
}

/**
 * プロジェクトのコミットを取得します
 */
@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllCommits(projects: Set<Project>): List<Commit> {
    return buildList {
        projects.forEach { project ->
            val json = requestJson(
                ApiEndPoint.GetCommit(project.groupId.slashTo2F),
                "all" to "true" // 全てのコミットを取得
            ) ?: return@forEach
            json.asJsonArray.forEach {
                val jsonObject = it.asJsonObject
                val dateJson = jsonObject["created_at"] // コミットの日付
                val date = Gson().fromJson(dateJson, Date::class.java) // JsonElement を Date クラスに変換
                add(Commit(date))
            }
        }
    }
}