## テスト

テスト方法

```powershell
$ npm test
```

または

```powershell
$ npm run karma
```

により、Chrome が開き、Jasmine 上の Card.Spec.js で規定されるテストを実行する。

以降、ファイル更新を watch し、自動でテストを続ける。



## asset(gifファイル) の追加

karma.conf.js の files array に以下エントリを入れる。

```json
      { pattern: 'images/*.gif',
      	watched: false,
      	served: true,
      	included: false
      }
```

これにより、`http://localhost:9876/base/images/c3.gif` のように`base/`配下で asset ファイルが提供される。

さらに `proxies` エントリを加えることで`base/`配下でなくすることができる。

```json
    proxies: {
    	'/images/': '/base/images/'
    },
```

位置は`files` 内ではなくルート

