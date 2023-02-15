# カスタムウィジェット開発テンプレート (on Things Cloud v10.6.6)

2023年1月

## このレポジトリについて

このレポジトリは、Things Cloud [WebSDK](https://developer.ntt.com/iot/docs/web/introduction/) を用いてカスタムウィジェット開発を行う際に、簡単に環境を構築するためのテンプレートです。
下記の [インストール、実行](#インストール実行) により、"Empty Widget" という空のカスタムウィジェットを含む cockpit アプリケーションがビルド/実行できます。
定期的なメンテナンスが行われる予定はありません。

## 参考URL

[WebSDK](https://developer.ntt.com/iot/docs/web/introduction/) の使い方については、以下も参考として下さい。

- [Web SDK for Angular を利用したカスタムウィジェットの作り方1](https://developer.ntt.com/iot/docs/report/websdk019/)
- [Web SDK for Angular を利用したカスタムウィジェットの作り方2](https://developer.ntt.com/iot/docs/report/websdk020/)

## 対象 version

Things Cloud v10.6.6

> Things Cloud の version up (2023年8月末予定) では、Angular バージョンの変更に伴い、WebSDK アーキテクチャが変更されるため、本コンテンツは適用できなくなる見込です。ご注意ください。

## 環境構築

以下をインストール。
- Node.js
- [c8ycli](https://developer.ntt.com/iot/docs/web/angular/#cli)

## インストール、実行

このレポジトリを clone し、以下を実行。

```
$ npm install
```

### 実行(local)

#### package.json 編集

package.json の 7行目にアクセス可能な Things Cloud テナントURLを記載。
```
    :
    "start": "c8ycli server -u https://<<tenant name>>.je1.thingscloud.ntt.com",
    :
```

#### npm start
```
$ npm start
```
build に数分ほど時間を要する。

#### アクセス

ブラウザから以下にアクセス。
http://localhost:9000/apps/cockpit/

- テナントid, ユーザー名、パスワードを入力

> フロントアプリケーションはローカルで build されたもの。(API は指定テナントに転送される)
> cockpit → ダッシュボード → ウィジェットを追加　で "Empty Widget" が選択できる。

### デプロイ方法

指定テナントの cockpit-app をカスタムウィジェット入り app に差し替える(deploy)。

```
$ npm run build
$ npm run deploy
prompt: Instance URL:  (http://demos.cumulocity.com) https://xxx.je1.thingscloud.ntt.com
prompt: Username:  (admin) ユーザー名を入力
prompt: Password:  パスワードを入力
```

アクセス可能な Things Cloud テナントURL, ユーザー名、パスワードを入力。
cockpit をリロード。

