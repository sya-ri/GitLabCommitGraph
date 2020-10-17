package com.gitlab.grcc.commit.graph.gitlab

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.ApiEndPoint.Companion.slashTo2F
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import com.google.gson.Gson
import org.jfree.data.time.Day
import java.util.*

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
suspend fun GitLabApiClient.getAllCommits(projects: Set<Project>): List<Commit>? {
    return buildList {
        projects.forEach { project ->
            addAll(getAllCommits(project) ?: return null)
        }
    }
}

/**
 * プロジェクトのコミットを取得します
 */
@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllCommits(project: Project, page: Int = 1): List<Commit>? {
    return buildList {
        val result = request(
            ApiEndPoint.GetCommit(project.groupId.slashTo2F),
            "page" to "$page", // ページを指定して取得
            "per_page" to "100", // ページ毎の取得数を設定
            "all" to "true" // 全てのコミットを取得
        )
        if (result !is GitLabApiClient.RequestResult.Success) {
            onFailure.invoke(result as GitLabApiClient.RequestResult.Failure)
            return null
        }
        val nextPage = result.response.headers["x-next-page"]?.toIntOrNull() // 次のページを取得
        if (nextPage != null) addAll(getAllCommits(project, nextPage) ?: return null)
        result.json.asJsonArray.forEach {
            val jsonObject = it.asJsonObject
            val dateJson = jsonObject["created_at"] // コミットの日付
            val date = Gson().fromJson(dateJson, Date::class.java) // JsonElement を Date クラスに変換
            add(Commit(date))
        }
    }
}