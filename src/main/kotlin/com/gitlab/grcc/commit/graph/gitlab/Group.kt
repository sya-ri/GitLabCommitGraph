package com.gitlab.grcc.commit.graph.gitlab

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.GitLabApiClient

data class Group(val id: String)

@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllGroup(groupId: String, page: Int = 1): Set<Group> {
    return buildSet {
        add(Group(groupId))
        val (response, json) = request(ApiEndPoint.GetSubGroup(groupId), "page" to "$page", "per_page" to "100") ?: return@buildSet
        val totalPage = response.headers["X-Total-Pages"]?.toIntOrNull()
        if (totalPage != null && page < totalPage) addAll(getAllGroup(groupId, page + 1))
        json.asJsonArray.forEach {
            addAll(getAllGroup(groupId + "%2F" + it.asJsonObject["path"].asString))
        }
    }
}