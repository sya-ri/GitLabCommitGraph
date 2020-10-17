package com.gitlab.grcc.commit.graph.gitlab

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.ApiEndPoint.Companion.slashTo2F
import com.gitlab.grcc.commit.graph.http.GitLabApiClient

/**
 * プロジェクトデータ
 */
data class Project(val name: String, val groupId: String)

/**
 * グループに属したプロジェクトを取得します
 */
@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllProject(groupId: String, page: Int = 1): Set<Project> {
    return buildSet {
        val result = request(
            ApiEndPoint.GetProject(groupId.slashTo2F),
            "page" to "$page", // ページを指定して取得
            "per_page" to "100", // ページ毎の取得数を設定
            "simple" to "true", // 簡易的なデータで取得(高速化)
            "include_subgroups" to "true" // サブグループのプロジェクトも含める
        )
        if (result !is GitLabApiClient.RequestResult.Success) {
            throw GitLabApiClient.RequestFailureException(result)
        }
        val totalPage = result.response.headers["X-Total-Pages"]?.toIntOrNull() // 合計ページ数を取得
        if (totalPage != null && page < totalPage) addAll(getAllProject(groupId, page + 1))
        result.json.asJsonArray.forEach {
            val jsonObject = it.asJsonObject
            val name = jsonObject["name"].asString // プロジェクト名
            val projectGroupId = jsonObject["path_with_namespace"].asString // プロジェクトへのパス
            add(Project(name, projectGroupId))
        }
    }
}