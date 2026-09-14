# API 契約

## 正本

HTTP API の機械可読な正本は [`openapi/openapi.yaml`](../openapi/openapi.yaml) です。この文書では、OpenAPI を更新するときの判断基準と運用方法を補足します。

契約を変更する機能は、実装より先、または実装と同じ pull request で OpenAPI を更新します。Route やテストだけを変更して契約を暗黙に変えてはいけません。

## レスポンス形式

- `/` の稼働確認を除き、ボディがあるレスポンスは `application/json` とします。
- ユーザーとレシートの作成成功は `201 Created` とし、ボディは返しません。
- ログイン成功はアクセストークンとトークン種別を JSON オブジェクトで返します。
- 一覧は JSON 配列、カテゴリ別集計はカテゴリ名をキーとする JSON オブジェクトで返します。
- 日時はタイムゾーンを含まない ISO 8601 形式、集計期間の日付は `YYYY-MM-DD` とします。
- 金額は最小通貨単位による正の整数とし、小数や通貨記号は含めません。

## エラー形式

OpenAPI で定義するすべてのエラーレスポンスは、次の JSON 形式に統一します。

```json
{
  "code": "invalid_request",
  "message": "Request is invalid"
}
```

クライアントは安定した `code` を処理の分岐に使用します。`message` は人間向けであり、文言変更を破壊的変更とはみなしません。

| HTTP | code | 用途 |
| --- | --- | --- |
| 400 | `invalid_request` | JSON、必須項目、または基本的な入力値が不正 |
| 400 | `invalid_receipt` | 明細、金額、カテゴリなどレシートの内容が不正 |
| 400 | `invalid_date_range` | 集計期間の形式または前後関係が不正 |
| 401 | `invalid_credentials` | ログイン情報が一致しない |
| 401 | `invalid_token` | JWT がない、無効、期限切れ、または対象ユーザーが存在しない |
| 409 | `user_already_exists` | 同じ名前のユーザーが既に存在する |
| 415 | `unsupported_media_type` | JSON が必要な endpoint へ別の Content-Type が送られた |
| 500 | `internal_server_error` | 予期しない内部エラー |

認証情報や内部例外の詳細はレスポンスへ含めません。同じ 401 でも、ログイン失敗は `invalid_credentials`、保護された endpoint の認証失敗は `invalid_token` として区別します。

## 認証

`/categories` と `/receipts` 以下は Bearer JWT を必要とします。

```http
Authorization: Bearer <accessToken>
```

JWT の不在、署名不正、claim 不足、期限切れをクライアントから区別できる情報は返しません。

## 互換性

OpenAPI の `info.version` は Semantic Versioning に従います。

- 後方互換な endpoint や任意項目の追加は minor version を更新します。
- 説明や例だけの変更は patch version を更新します。
- 既存項目の削除、型変更、必須化、status code の変更は major version を更新します。

現在は `0.x` の初期契約です。破壊的変更でも、変更理由・移行方法・実装 PR を明記してください。

## 実装との同期

この契約は contract-first で導入します。導入時点の実装には、ログイン成功時の JSON 化、401・415 の共通エラー化、ユーザー名・パスワード制約など未反映の項目があります。後続の API 実装 PR で契約へ合わせ、API テストで wire format を固定します。

OpenAPI lint は構文、参照、記述上の問題を検出しますが、Ktor の Route と自動的に比較するものではありません。そのため、request／response の代表例は API テストでも検証します。

## 検証

Docker が起動した状態で、リポジトリルートから次を実行します。

```sh
docker run --rm \
  -e REDOCLY_TELEMETRY=off \
  -e REDOCLY_SUPPRESS_UPDATE_NOTICE=true \
  -v "$PWD:/spec:ro" \
  redocly/cli:2.52.1 \
  lint --config=/spec/redocly.yaml /spec/openapi/openapi.yaml
```
