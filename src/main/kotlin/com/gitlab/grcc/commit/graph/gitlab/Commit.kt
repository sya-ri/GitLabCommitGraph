package com.gitlab.grcc.commit.graph.gitlab

import com.gitlab.grcc.commit.graph.http.ApiEndPoint
import com.gitlab.grcc.commit.graph.http.ApiEndPoint.Companion.slashTo2F
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import com.google.gson.Gson
import java.util.*

data class Commit(val date: Date)

@ExperimentalStdlibApi
suspend fun GitLabApiClient.getAllCommits(projects: Set<Project>): List<Commit> {
    return buildList {
        projects.forEach { project ->
            val json = requestJson(
                ApiEndPoint.GetCommit(project.groupId.slashTo2F),
                "all" to "true"
            ) ?: return@forEach
            json.asJsonArray.forEach {
                val jsonObject = it.asJsonObject
                val dateJson = jsonObject["created_at"]
                val date = Gson().fromJson(dateJson, Date::class.java)
                add(Commit(date))
            }
        }
    }
}