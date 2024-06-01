# ベースイメージ
#FROM openjdk:17-alpine
FROM openjdk:21-jdk-alpine

# 作業ディレクトリを設定
WORKDIR /app

# 依存関係をコピー
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY gradle ./gradle

# 依存関係をインストール
RUN ./gradlew build --no-daemon || return 0

# ソースコードをコピー
COPY src ./src

# アプリケーションをビルド
RUN ./gradlew installDist

# アプリケーションを実行
CMD ["./build/install/backend/bin/backend"]
