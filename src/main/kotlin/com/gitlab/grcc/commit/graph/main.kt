package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.gitlab.getAllGroup
import com.gitlab.grcc.commit.graph.gitlab.getAllProject
import com.gitlab.grcc.commit.graph.http.ApiEndPoint.Companion.slashTo2F
import com.gitlab.grcc.commit.graph.http.GitLabApiClient

@ExperimentalStdlibApi
suspend fun main() {
    print("AccessToken: ")
    val accessToken = readLine()
    if (accessToken.isNullOrBlank()) return
    print("GroupId: ")
    val groupId = readLine()
    if (groupId.isNullOrBlank()) return
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
