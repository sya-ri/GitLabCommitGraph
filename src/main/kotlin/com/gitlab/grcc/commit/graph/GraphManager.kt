package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.gitlab.Commit.Companion.compress
import com.gitlab.grcc.commit.graph.gitlab.Project
import com.gitlab.grcc.commit.graph.gitlab.getAllCommits
import com.gitlab.grcc.commit.graph.gitlab.getAllProject
import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection

object GraphManager {
    @ExperimentalStdlibApi
    fun GitLabApiClient.addGraphFromGroup(data: TimeSeriesCollection, name: String, groupId: String, onSuccess: () -> Unit) {
        GlobalScope.launch {
            // プロジェクトの取得
            val projects = getAllProject(groupId)

            // グラフに反映
            addGraphFromProject(data, name, projects, onSuccess)
        }
    }

    @ExperimentalStdlibApi
    fun GitLabApiClient.addGraphFromProject(data: TimeSeriesCollection, name: String, project: Project, onSuccess: () -> Unit) {
        addGraphFromProject(data, name, setOf(project), onSuccess)
    }

    @ExperimentalStdlibApi
    fun GitLabApiClient.addGraphFromProject(data: TimeSeriesCollection, name: String, projects: Set<Project>, onSuccess: () -> Unit) {
        GlobalScope.launch {
            data.addSeries(TimeSeries(name).apply {
                // コミットの取得
                val commits = getAllCommits(projects)

                // コミットをグラフに反映
                val compressDates = commits.compress()
                var sumCommit = 0
                compressDates.forEach { (day, commit) ->
                    sumCommit += commit
                    add(day, sumCommit.toDouble())
                }
            })
            onSuccess.invoke()
        }
    }
}