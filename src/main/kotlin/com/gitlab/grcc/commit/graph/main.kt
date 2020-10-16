package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.gitlab.getAllGroup
import com.gitlab.grcc.commit.graph.gitlab.getAllProject
import com.gitlab.grcc.commit.graph.http.ApiEndPoint.Companion.slashTo2F
import com.gitlab.grcc.commit.graph.http.GitLabApiClient

@ExperimentalStdlibApi
suspend fun main() {
    // アクセストークンを入力
    print("AccessToken: ")
    val accessToken = readLine()
    if (accessToken.isNullOrBlank()) return

    // 取得するトップグループを入力
    print("GroupId: ")
    val groupId = readLine()
    if (groupId.isNullOrBlank()) return

    // ApiClient を定義
    val client = GitLabApiClient(accessToken)

    // グループの取得
    println()
    print("Getting All Groups ... ")
    val groups = client.getAllGroup(groupId.slashTo2F)
    println(groups.size)

    // プロジェクトの取得
    println()
    print("Getting All Projects ... ")
    val projects = client.getAllProject(groups)
    println(projects.size)

    // デバッグメッセージ
    println()
    println(groups)
    println(projects)
}
