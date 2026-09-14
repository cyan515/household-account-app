# Household Account App

Ktor、PostgreSQL、Exposed で構成された家計簿 API です。データベースのスキーマは Flyway、接続は HikariCP で管理します。

## 必要なもの

- JDK 17
- Docker Engine または Colima などの Docker 互換環境
- Docker Compose v2

Gradle はリポジトリに含まれる Wrapper を使用するため、別途インストールする必要はありません。初回のビルドとテストでは Gradle の依存関係と PostgreSQL イメージを取得するため、ネットワーク接続が必要です。

## Docker Compose で起動する

環境変数の雛形をコピーします。

```sh
cp .env.example .env
```

`.env` の `DB_PASSWORD` と `JWT_SECRET` に推測されにくい値を設定してください。`.env` はコミットしないでください。JWT 用には 32 バイト以上の値が必要で、たとえば次のコマンドで生成できます。

```sh
openssl rand -hex 32
```

アプリと PostgreSQL を起動します。

```sh
docker compose up --build
```

PostgreSQL のヘルスチェック成功後にアプリが起動します。`http://localhost:8080/` が `Hello Ktor!` を返せば準備完了です。

終了するときは次を実行します。

```sh
docker compose down
```

## JVM 上でアプリを起動する

先に「Docker Compose で起動する」の環境変数準備を行い、`.env` の `DB_PASSWORD` と `JWT_SECRET` を設定してください。

PostgreSQL だけを Docker Compose で起動します。

```sh
docker compose up -d db
```

`.env` を現在のシェルへ読み込み、Gradle Wrapper からアプリを起動します。

```sh
set -a
source .env
set +a
./gradlew run
```

`.env.example` の `DB_URL` は、ホスト上のアプリから Docker の PostgreSQL へ接続する値になっています。

## テスト

テストでは Testcontainers が一時的な PostgreSQL コンテナを起動します。先に Docker 互換環境を起動してから実行してください。

```sh
./gradlew test --no-daemon
```

## 環境変数

| 変数 | 用途 | Docker Compose の既定値 |
| --- | --- | --- |
| `PORT` | HTTP ポート | `8080` |
| `DB_PORT` | ホストへ公開する PostgreSQL のポート | `5432` |
| `DB_NAME` | PostgreSQL のデータベース名 | `household_db` |
| `DB_USER` | PostgreSQL のユーザー名 | `household_user` |
| `DB_PASSWORD` | PostgreSQL のパスワード | 必須 |
| `DB_URL` | JDBC 接続先 | Compose 内では `db` サービスを使用 |
| `DB_MAXIMUM_POOL_SIZE` | コネクションプールの最大接続数 | `10` |
| `DB_MINIMUM_IDLE` | 維持する最小アイドル接続数 | `1` |
| `DB_CONNECTION_TIMEOUT_MILLIS` | DB 接続の待機上限（ミリ秒） | `30000` |
| `DB_BASELINE_ON_MIGRATE` | 既存 DB を Flyway 管理へ移行するためのフラグ | `false` |
| `JWT_SECRET` | JWT の署名に使う 32 バイト以上の秘密値 | 必須 |

## トラブルシューティング

テストや起動時に Docker へ接続できない場合は、Docker Desktop、Docker Engine、または Colima が起動しているか確認します。

```sh
docker info
docker compose version
```

コンテナの状態とログは次のコマンドで確認できます。

```sh
docker compose ps
docker compose logs app db
```

ホスト側ですでに `.env` の `DB_PORT` または `PORT` が使用されている場合は、既存プロセスを停止するかポート設定を変更してください。`DB_PORT` を変更して JVM 上でアプリを起動する場合は、`DB_URL` のポートも同じ値へ変更します。

## データベースマイグレーション

新しいデータベースでは、アプリ起動時に Flyway がスキーマ作成と初期カテゴリ投入を自動実行します。

旧バージョンのアプリで作成済みのデータベースを初めて Flyway 管理へ移す場合だけ、事前にバックアップを取得したうえで `DB_BASELINE_ON_MIGRATE=true` を指定してください。初回起動が成功したら `false` に戻します。新規データベースでは有効にしないでください。

開発データを含む Docker ボリュームまで削除して作り直す場合は、次のコマンドを使います。この操作では保存済みデータも削除されます。

```sh
docker compose down --volumes
```
