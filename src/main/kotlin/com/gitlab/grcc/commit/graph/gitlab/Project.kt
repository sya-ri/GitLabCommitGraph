package com.gitlab.grcc.commit.graph.gitlab

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.GitLabApiClient

data class Project(val name: String, val groupId: String)

@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllProject(groups: Set<Group>, page: Int = 1): Set<Project> {
    return buildSet {
        groups.forEach { group ->
            val (response, json) = request(ApiEndPoint.GetProject(group.id), "page" to "$page", "per_page" to "100") ?: return@buildSet
            val totalPage = response.headers["X-Total-Pages"]?.toIntOrNull()
            if (totalPage != null && page < totalPage) addAll(getAllProject(groups, page + 1))
            json.asJsonArray.forEach {
                val jsonObject = it.asJsonObject
                val name = jsonObject["name"].asString
                val projectGroupId = jsonObject["path"].asString
                add(Project(name, projectGroupId))
            }
        }
    }
}