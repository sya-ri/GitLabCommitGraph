package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.ApiEndPoint.Companion.slashTo2F
import com.gitlab.grcc.commit.graph.http.GitLabApiClient

@ExperimentalStdlibApi
suspend fun main() {
    print("AccessToken: ")
    val accessToken = readLine() ?: return
    print("GroupId: ")
    val groupId = readLine() ?: return

    val client = GitLabApiClient(accessToken)
    println(client.getAllProject(groupId.slashTo2F))
}

data class Project(val name: String, val groupId: String)

@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllProject(groupId: String): Set<Project> {
    return buildSet {
        val subGroupJson = request(ApiEndPoint.GetSubGroup(groupId)) ?: return@buildSet
        subGroupJson.asJsonArray.forEach {
            addAll(getAllProject(groupId + "%2F" + it.asJsonObject["path"].asString))
        }
        val projectJson = request(ApiEndPoint.GetProject(groupId)) ?: return@buildSet
        projectJson.asJsonArray.forEach {
            val jsonObject = it.asJsonObject
            val name = jsonObject["name"].asString
            val projectGroupId = jsonObject["path"].asString
            add(Project(name, projectGroupId))
        }
    }
}