package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.api.http.GitLabApiClient
import javax.swing.JFrame
import javax.swing.JOptionPane

fun GitLabApiClient.setOnFailure(frame: JFrame) {
    onFailure = {
        fun showErrorMessage(message: String) {
            JOptionPane.showMessageDialog(frame, message, "エラー", JOptionPane.ERROR_MESSAGE)
        }

        when (it) {
            GitLabApiClient.RequestResult.Failure.NotContent -> {
                showErrorMessage("Jsonの取得に失敗")
            }
            GitLabApiClient.RequestResult.Failure.BadRequest -> {
                showErrorMessage("APIリクエストに必要な値が足りません")
            }
            GitLabApiClient.RequestResult.Failure.Unauthorized -> {
                setAccessToken(frame, "アクセストークンを再入力")
            }
            GitLabApiClient.RequestResult.Failure.Forbidden -> {
                showErrorMessage("アクセスすることが許可されていません")
            }
            GitLabApiClient.RequestResult.Failure.NotFound -> {
                showErrorMessage("プロジェクトもしくはグループが見つかりませんでした")
            }
            GitLabApiClient.RequestResult.Failure.MethodNotAllowed -> {
                showErrorMessage("そのリクエストはサポートされていません")
            }
            GitLabApiClient.RequestResult.Failure.Conflict -> {
                showErrorMessage("競合が発生しました")
            }
            GitLabApiClient.RequestResult.Failure.RequestDenied -> {
                showErrorMessage("リクエストが拒否されました")
            }
            GitLabApiClient.RequestResult.Failure.Unprocessable -> {
                showErrorMessage("処理に失敗しました")
            }
            GitLabApiClient.RequestResult.Failure.ServerError -> {
                showErrorMessage("サーバーでエラーが発生しました")
            }
            GitLabApiClient.RequestResult.Failure.UnHandle -> {
                showErrorMessage("ハンドリングされていないレスポンスコードです")
            }
        }
    }
}