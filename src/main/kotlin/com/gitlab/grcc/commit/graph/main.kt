package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.gitlab.Commit.Companion.compressDate
import com.gitlab.grcc.commit.graph.gitlab.getAllCommits
import com.gitlab.grcc.commit.graph.gitlab.getAllProject
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import java.text.SimpleDateFormat

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

    // プロジェクトの取得
    println()
    print("Getting All Projects ... ")
    val projects = client.getAllProject(groupId)
    println(projects.size)

    // コミットの取得
    println()
    print("Getting All Commits ... ")
    val commits = client.getAllCommits(projects)
    println(commits.size)

    // デバッグメッセージ
    println()
    println(projects)
    println(commits)
    val dateFormat = SimpleDateFormat("YYYY/MM/dd")
    println(commits.compressDate().map { dateFormat.format(it.key) to it.value })
}
