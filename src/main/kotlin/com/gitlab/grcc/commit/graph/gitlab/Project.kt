package com.gitlab.grcc.commit.graph.gitlab

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.GitLabApiClient

data class Project(val name: String, val groupId: String)

@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllProject(groups: Set<Group>): Set<Project> {
    return buildSet {
        groups.forEach { group ->
            val projectJson = requestJson(ApiEndPoint.GetProject(group.id)) ?: return@buildSet
            projectJson.asJsonArray.forEach {
                val jsonObject = it.asJsonObject
                val name = jsonObject["name"].asString
                val projectGroupId = jsonObject["path"].asString
                add(Project(name, projectGroupId))
            }
        }
    }
}