package com.gitlab.grcc.commit.graph.gitlab

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.GitLabApiClient

data class Group(val id: String)

@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllGroup(groupId: String): Set<Group> {
    return buildSet {
        add(Group(groupId))
        val subGroupJson = requestJson(ApiEndPoint.GetSubGroup(groupId)) ?: return@buildSet
        subGroupJson.asJsonArray.forEach {
            addAll(getAllGroup(groupId + "%2F" + it.asJsonObject["path"].asString))
        }
    }
}