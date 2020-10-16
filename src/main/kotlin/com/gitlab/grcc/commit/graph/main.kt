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
    println()
    print("Getting All Groups ... ")
    val groups = client.getAllGroup(groupId.slashTo2F)
    println(groups.size)
    println()
    print("Getting All Projects ... ")
    val projects = client.getAllProject(groups)
    println(projects.size)
    println()
    println(groups)
    println(projects)
}

data class Group(val id: String)

@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllGroup(groupId: String): Set<Group> {
    return buildSet {
        add(Group(groupId))
        val subGroupJson = request(ApiEndPoint.GetSubGroup(groupId)) ?: return@buildSet
        subGroupJson.asJsonArray.forEach {
            addAll(getAllGroup(groupId + "%2F" + it.asJsonObject["path"].asString))
        }
    }
}

data class Project(val name: String, val groupId: String)

@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllProject(groups: Set<Group>): Set<Project> {
    return buildSet {
        groups.forEach { group ->
            val projectJson = request(ApiEndPoint.GetProject(group.id)) ?: return@buildSet
            projectJson.asJsonArray.forEach {
                val jsonObject = it.asJsonObject
                val name = jsonObject["name"].asString
                val projectGroupId = jsonObject["path"].asString
                add(Project(name, projectGroupId))
            }
        }
    }
}