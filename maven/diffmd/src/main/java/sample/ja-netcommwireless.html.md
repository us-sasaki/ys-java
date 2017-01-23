---
title: Netcomm
layout: default
---

## 概要

以下のセクションでは、NetCommルーターをCumulocityと併せて使用する方法を説明します。具体的に、以下の作業の手順を説明します。

* ルーターの[構成設定](#configure) 。
* 自分のCumulocityアカウントへのルーターの[接続](#connect)。
* [WAN、LANおよびDHCPのパラメーターの構成設定](#network)。
*  [ソフトウェアおよびファームウェア](#software)の管理。
*  [システムリソース](#system)の監視。
* 内蔵[GPS](#gps)機能の使用。
* 内蔵[GPIO](#gpio)ピンの使用。
* [構成設定](#rdb)パラメーターの閲覧。
* 機器を[SMSモード](#sms_mode)で使用する場合の構成設定。
* [機器のシェル](#shell)経由でのテキストコマンドの遠隔実行。
* Get [イベント通知](#notifications)の取得。
* [Modbus](#modbus)機器の接続。
* [各種ログ](#logs)の閲覧。

以下のセクションでは、NetComm [エージェント](/guides/devices/netcomm-release)パッケージがルーターにインストール済みであると想定します。このエージェントは [NTC-6200](http://www.netcommwireless.com/product/m2m/ntc-6200)および[NTC-140W](http://www.netcommwireless.com/product/4g-wifi-m2m-router)と互換性があります。 ルーター特有の特徴について詳しくは、ルーターのホームページの「Downloads（ダウンロード）」セクションに記載されているそれぞれのマニュアルをご覧ください。

## <a name="configure"></a>ルーターの構成設定

Cumulocityに対応するための構成設定は、ルーターのウェブユーザーインターフェース経由で行うことができます。その場合、ルーターのマニュアルに記載の通り、ユーザーインターフェースへログインします。「System（システム）」タブへナビゲートし、「Internet of Things（モノのインターネット）」のメニュー項目をクリックします。

![Cumulocity構成設定](/guides/devices/netcomm/routerconf.png)

「Cumulocity agent（Cumulocityエージェント）」のトグルスイッチが「ON」に設定され、「Server（サーバー）」欄に記載のURLが自分の接続したいCumulocityインスタンスのURLであるかどうか、検証します。例として、以下を使用します。

* https://developer.cumulocity.com/s ：Cumulocityへの接続。
* https://management.ram.m2m.telekom.com/s ：Deutsche Telekomによるモノのインターネットへの接続。

任意で、以下の機能向けにデータ収集を有効化することもできます。

* GPIOアナログ測定：アナログ入力電圧を送信します（秒単位）。
* GPS位置間隔：現在のGPS位置を更新します（秒単位）。
* GPS位置イベント：GPS位置トレースを送信します（秒単位）。
* システムリソース測定：CPU使用状況、メモリ使用状況およびネットワークトラフィックに関する情報を取得します（秒単位）。

これらのオプションはすべて、初期設定では無効の状態です（間隔は0に設定されています）。

ウェブインターフェースもCumulocityへの接続状態を表示します。

（バージョン2.xの場合）

 * Off（オフ）：ソフトウェアは無効の状態です。
 * Initializing（初期化中）：ソフトウェアを初期化中です。
 * Registering（登録中）：機器はCumulocityへの登録を待機中です（次のセクション参照）。
 * Starting（起動中）：ソフトウェアがすべてのコンポーネントを起動します。
 * No credentials（認証情報なし）：機器がCumulocityにアクセスするための認証情報を取得しなかったか、認証情報が無効とされたか、または認証情報が誤っていました。
 * Started（起動済み）：ソフトウェアは起動しています。
 * Connecting（接続処理中）：ソフトウェアはCumulocityへ接続処理中です。
 * Connected（接続済み）：ソフトウェアはCumulocityへ接続された状態です。
 * Disconnected（接続解除）：ソフトウェアはCumulocityへ接続されていません。
 * Reconnecting（再接続中）：ソフトウェアは接続を再試行中です。
 * Stopping（終了処理中）：ソフトウェアは終了処理中です。

（バージョン3.xの場合）
* Checking network connection（ネットワーク接続確認中）：起動状態でモバイルネットワーク接続を待機中です。
* Bootstrapping（ブートストラップ）：認証情報をロード、または認証情報をCumulocityにリクエストします。
* Integrating（統合処理中）：Cumulocityへ接続処理中です。
* Loading plugins（プラグインをロード中）：Luaプラグインをロード中です。
* Connected（接続済み）：エージェントはCumulocityへの接続に成功しました。
* No server URL（サーバーURLなし）：サーバーURLが存在しないか、または無効です。
* Bootstrap failed（ブートストラップ失敗）：Cumulocityから認証情報を取得できません。
* Integration failed（統合失敗）：Cumulocityへ接続できません。
* Create threads failed（スレッド作成失敗）：レポーターの開始または機器のプッシュを行うことができません。

## <a name="connect"></a>ルーターの接続

ご自身のNetCommルーターをCumulocityに登録するには、ルーターの製造番号を「_機器ID_」として設定する必要があります。登録手順はユーザーガイドの「[Connecting devices（機器の接続）](/guides/users-guide/device-management/#device-registration)」セクションに記載されています。製造番号は下記の通り、ルーターの後面に印刷されています。あるいは、ルーターのウェブユーザーインターフェースでも確認できます。「System（システム）」へナビゲートし、「Internet of Things（モノのインターネット）」へ進み、「Device ID（機器ID）」フィールドをご確認ください。

> バージョン2.xのユーザー、または2.xから3.xへアップグレードするユーザーは、ルーターのMACアドレスを使用してください。MACアドレスを入力する際は必ず、小文字と数字のみ使用してください。MACアドレスをコロンで区切らないでください。例えば、画像からのMACアドレスは以下のように入力されることになります。

	006064dda4ae

![MACアドレス](/guides/devices/netcomm/mac.png)

「accept（承諾）」ボタンをクリックした後、「All devices（すべての機器）」へナビゲートすると、ルーターが登録後にここへ表示されるはずです。ルーターの初期設定名は「&lt;型式&gt; (S/N &lt;製造番号&gt;)」です。「&lt;型式&gt;」は機器の型式名を指します。例えば、上記のルーターの場合、「NTC-6200-02 (S/N 165711141901036)」と表示されます。ルーターをクリックすると、詳細情報を閲覧したり、本書の後続セクションに記載の機能へアクセスしたりすることができます。登録済みのルーターをリスト内の他の機器と区別するため、ルーター名を「Info（情報）」タブ上で変更することができます。このタブにはルーターの製造番号やSIMカードデータなどの基本情報も表示されます。名称変更後、「Info」ページの下部に表示される「save changes（変更を保存）」ボタンをクリックすることをお忘れなく。

![機器の詳細](/guides/devices/netcomm/info.png)

## <a name="network"></a>ネットワークパラメーターの構成設定

下記のスクリーンショットに示されている通り、「Network（ネットワーク）」タブ内で、不可欠なモバイルネットワーク（WAN）やローカルエリアネットワーク（LAN）の閲覧および構成設定を行うことができます。

ユーザーインターフェースに表示されるモバイルネットワーク（WAN）パラメーターは、ルーターに保存される最初のプロファイルに相当します。これらのパラメーターの構成設定を遠隔操作により、直接またはSMS経由で行うことができます。

SMS構成設定の場合、SMSコマンドを受け付けるようルーターを構成設定する必要があります。SMS構成設定の関連パラメーターに関するルーターのマニュアルをご覧いただくか、またはルーターのウェブユーザーインターフェースをご使用ください。また、自分のアカウントでSMSゲートウェイを構成設定する必要もあります。 SMSゲートウェイのセットアップについては [サポート担当](https://support.cumulocity.com) へお問い合わせください。 Device Shellについて詳しくは [ユーザーガイド](https://cumulocity.com/guides/users-guide/device-management/#shell)をご覧ください。

> 注記：IPとSMSモードの両方を介してWANパラメーターを構成設定するには、Cumulocity 7.26が必要です。APN構成設定を誤ると、機器がモバイルネットワーク接続を失い、限られたSMS機能による管理しかできなくなります。

![WANパラメーター](/guides/devices/netcomm/wan.png)

LANおよびDHCPのパラメーターの構成設定を、Cumulocityから直接行うこともできます。

![LANパラメーター](/guides/devices/netcomm/lan.png)

## <a name="software"></a>ソフトウェアおよびファームウェアの管理

ルーターにインストール済みのソフトウェアおよびファームウェアの遠隔管理を、[機器管理ユーザーガイド](/guides/users-guide/device-management#software-repo)に記載の通り、Cumulocityが提供する標準のソフトウェア／ファームウェア管理機能を使用して行うことができます。

ソフトウェアパッケージは [ipkg](http://en.wikipedia.org/wiki/Ipkg) 形式である必要があり、また「&lt;パッケージ名&gt;\_&lt;バージョン&gt;\_&lt;arch&gt;.ipk」という命名法に従う必要があります。文字を含むバージョン番号はサポートされません。パッケージ管理方法（インストール、アップグレード、ダウングレード、削除）はすべて、ルーターのパッケージマネージャー経由でサポートされます。ソフトウェアパッケージに従属物がある場合、必ずそれらを先にインストールしてください。

> 「smartrest-agent\_&lt;version&gt;\_arm.ipk」というパッケージは、NetCommエージェントであることを意味します。このパッケージをCumulocityから削除してはなりません。

> 2.1.1より古いバージョンからアップグレードする場合、機器の再登録が必要です。

ファームウェアをルーター上にアップロードおよびインストールすることもできます。ファームウェアを正常にアップグレードするため、対象ファームウェアにエージェントパッケージが含まれることを確認してください。エージェントパッケージが対象ファームウェアに含まれていないと、インストール後にエージェントが起動しません。 ファームウェアファイルはNetcommの命名法（「&lt;name&gt;\_&lt;version&gt;.cdi」）に従う必要があります。

![ソフトウェア／ファームウェア](/guides/devices/netcomm/software.png)

## <a name="system"></a>システムリソースの監視

ルーターのシステムリソース使用状況に関する統計を記録して、トラブルシューティングに役立てることができます。以下の統計を取得することができます。

* CPU負荷（単位：パーセント）
* メモリ使用料および総メモリ容量（単位：MB）
* すべてのインターフェース上でのアップリンクおよびダウンリンクのトラフィック（単位：KB/秒）

初期設定では、リソース統計収集が無効となっています。有効化する場合、 [ルーターユーザーインターフェース](#configure) における「System resources measurements（システムリソース測定）」の収集間隔を非ゼロに設定するか、または [Device Shell](#shell)を使用して以下の通り設定します。

	set service.cumulocity.plugin.system_resources.interval=<interval>

「Measurements（測定結果）」タブまたはダッシュボードから、収集されたデータにアクセスすることができます。

## <a name="gps"></a>GPSの使用

ルーターの所在特定または追跡を行う場合、GPSアンテナをルーターへ接続し、ルーターのGPS機能を有効化します。次いで「GPS position interval（GPS位置間隔）」および／または「GPS position event（GPS位置イベント）」の値を非ゼロに設定することにより、データ収集頻度を [設定](#configure) します。「GPS position interval」は、ルーターの現在位置の更新頻度を定義します。「GPS position event"」は、追跡を目的に現在位置を位置更新イベントとして保存する頻度を定義します。同様に、これらのパラメーターを以下の通りDevice Shellから設定することもできます。

	set service.cumulocity.plugin.ntc6200.gps.update_interval=<update interval>
	set service.cumulocity.plugin.ntc6200.gps.interval=<event interval>

構成設定変更を適用した後、最初のGPSデータの到着までしばらく待ってから、ページを再読み込みします。そうすると、「Location（位置）」タブと「Tracking（追跡）」タブが表示されるはずです。詳しくはユーザーガイドの「[Location](/guides/users-guide/device-management#location)」および「[Tracking](/guides/users-guide/device-management#tracking)」のセクションをご覧ください。

## <a name="gpio"></a>GPIOの使用

以下のGPIO機能がサポートされています。

* アナログ入力の電圧を測定結果としてCumulocityへ送信する。
* デジタル入力が1になるとアラームを起動し、0になるとアラームを解除する。
* デジタル出力にCumulocityから遠隔操作で書き込む。

個別のIO設定について詳しくは、ご自身のルーターの関連資料をご覧ください。利用可能な機能は、機器の型式によって異なる場合があります。 例えば、NTC 6200型はGPIOピン1～3に対応する一方、NTC 140W型はGPIOピン1にしか対応しません。

### アナログ入力

GPIOピンの入力電圧のポーリングを定期的に行い、結果をCumulocity送信したい場合、「[GPIO analog measurements（GPIOアナログ測定）](#configure)」の値を非ゼロに設定します。あるいは以下の通り、Device Shellを使用します。

	set service.cumulocity.plugin.ntc6200.gpio.interval=<interval>
	set service.cumulocity.gpio.<port>.notify=measurement

&lt;port&gt;はGPIOピンの付番を指します。NTC-6200の場合、&lt;port&gt;の値は1、2または3のいずれかである一方、NTC-140Wの場合、&lt;port&gt;の値は1のみです。結果は可視化されて「Measurements（測定結果）」に表示されます。

### デジタル入力

デジタル入力からアラームを起動することができます。これらの構成設定を、ルーターユーザーインターフェースを使用するか、またはDevice Shell経由で行うことができます。 形式は以下の通りです。

	set service.cumulocity.gpio.<port>.notify=alarm
	set service.cumulocity.gpio.<port>.debounce.interval=<SECONDS>
	set service.cumulocity.gpio.<port>.alarm.text=<ALARM_TEXT>
	set service.cumulocity.gpio.<port>.alarm.severity=<severity>

通知パラメーターとして可能な値は以下の通りです。

* off：ピンはあらゆる通知について無効です。
* alarm：ピンの読み取り値が「high（高）」の場合にアラームが起動されます。
* measurement：電圧のアナログ読み取り値が測定結果として送信されます。

デバウンス間隔はGPIO入力からの電気ノイズを低減します。 つまり、間隔が短いほど値のノイズが大きくなりますが、信号の変化に対する反応は速くなります。初期設定のデバウンス間隔は10分間です。

「text（テキスト）」プロパティの設定により、初期設定のアラームテキストを無効にすることができます。初期設定ではこの値が空の状態で、「gpio&lt;N&gt; is active（gpio&lt;N&gt;が有効です）」をテキストとして使用します。&lt;N&gt;はGPIOピンの付番を指します。

有効なアラーム重大度は以下の通りです。

 * WARNING（警告）
 * MINOR（軽微）
 * MAJOR（重大） [初期設定]
 * CRITICAL（極めて重大）

入力は1秒毎に、変化がないか確認されます。

### デジタル出力

「Relay array（リレー配列）」プラグインを使用して、デジタル出力を制御することができます。下記のスクリーンショットをご覧ください。GPIOピンの付番はルーターでの付番と同じです。NTC-6200型の場合、3通りのGPIOピンを設定できますが、NTC-140W型では最初のピンのみ有効です。

![Relay Array](/guides/devices/netcomm/relayarray.png)

## <a name="rdb"></a>構成設定管理

ユーザー構成設定データを検索、修正および保存することができます。 To do 実行する場合、ルーターの「[Configuration（構成設定）](/guides/users-guide/device-management#operation-monitoring)」タブへナビゲートし、「CONFIGURATION」ウィジェット内の「Reload（リロード）」ボタンをクリックして構成設定データをリクエストします。ダウンロードに数秒かかります。構成設定データが到着すると、パラメーター一覧と各パラメーターに対応する値が表示されます。その後、構成設定に変更を加え、機器に戻す形で保存することができます。

構成設定のスナップショットを機器にリクエストし、それを後で他の機器に適用することもできます。

エージェントのバージョン3.11およびCumulocityのバージョン7.26以降、RDBスナップショットにも対応するようになっていますが、これは構成設定のいわば上位集合です。これは主にトラブルシューティングが目的です。

![RDBセットアップ](/guides/devices/netcomm/rdb.png)

> Cumulocity 6.9より前まで、このウィジェットは「Control（制御）」タブに含まれていました。 Cumulocity 6.9以降、機器の非テキスト部分を含めた構成設定全体のスナップショットを取得したり、構成設定の参照スナップショットを機器に送り返したりすることもできます。

## <a name="sms_mode"></a> 機器をSMSモードで使用する場合の構成設定

機器向けのSMSコマンドを使用する場合、ルーターのウェブインターフェースを開き、「Services（サービス）」から「SMS messaging（SMSメッセージング）」、そして「Diagnostics（診断）」へとナビゲートします。機器の構成設定手順は以下の通りです。

* 「Only accept authenticated SMS messages（認証済みSMSメッセージのみ受け付ける）」を無効化するか、または許可された送信者をホワイトリストに追加する。パスワードの使用には対応していません。
* 他の設定をオンにする。

![SMSモードの有効化](/guides/devices/netcomm/sms_mode.png)

> 詳しくは「[Control devices via SMS（SMS経由での機器の制御）](/guides/reference/device-control#control_via_sms)」をご覧ください。

## <a name="shell"></a>Device Shell

Device Shellを使用して、個別の構成設定パラメーターを機器から読み書きするほか、診断用コマンドを実行することもできます。 詳しくは [ユーザーガイド](/guides/users-guide/device-management#shell)をご覧ください。有効なパラメーターおよび診断用コマンドについては、Netcomm関連資料をご覧ください。一般的な形式は以下の通りです。

* 「get &lt;parameter&gt;」：パラメーターを機器から読み取る。
* 「set &lt;parameter&gt;=&lt;value&gt;」：パラメーターを機器に書き込む。
* 「execute &lt;command&gt;」：診断用コマンドを機器上で実行する。

セミコロンをセパレーターとして使用すると、複数のget、setおよびexecuteコマンドを実行できます。使用頻度の高いパラメーターおよびコマンドにアクセスするには、「Get Predefined（既定のものを取得）」リンクをクリックします。

![Device Shell](/guides/devices/netcomm/shell.png)

## <a name="notifications"></a>イベント通知

ルーターは一定のシステムイベントを通知として報告します。これらをアラームとしてCumulocityへ転送することができます。システムイベントは、例えばモバイルネットワークの問題のトラブルシューティングに役立ちます。 様々な種類のイベントやそれらの転送方法について詳しくは、Netcomm関連資料（例えば、ユーザーガイドの「Event notification（イベント通知）」セクション）をご覧ください。イベントをアラームとして転送する場合、ローカルホスト上のポート1331宛に送信するUDP宛先を設定します（「Destination configuration（宛先構成設定）」セクション参照）。

![イベント通知](/guides/devices/netcomm/notifications.png)

## <a name="modbus"></a>Cloud Fieldbus

Modbus-TCPおよびModbus-RTUのスレーブにそれぞれLAN経由およびシリアルポート経由で接続し、Cumulocity内で遠隔管理することができます。実行するには以下を行う必要があります。

Modbus-TCPの設定手順

* LAN接続を確立する。 上記の「[Network（ネットワーク）](#network)」タブと、Modbus機器上の対応する機器構成機構を使用して、ルーターと自分のModbus-TCPスレーブとの間のIP通信を有効化する。
* 初期設定の502と異なるポートを使用している場合、NetComm機器のウェブUI上のCumulocityメニュー内でModbus-TCPポートの構成設定を行う。「[Configuring the router（ルーターの構成設定）](#configure)」をご覧ください。

Modbus-RTUの設定手順

* ルーターと自分のModbus-RTUスレーブを、シリアルケーブル経由で接続する。
* Device Shell経由で以下の通りシリアルポートの構成設定を行う。

        set serial.iomode.default=<mode>

`<mode>`はre232、rs422またはrs485のいずれかとすることができます。モード変更後、機器の再起動が必要となる場合があります。

> 初期設定のシリアルポート`/dev/ttyAPP4`は、追加の構成設定を行わなくても機能するはずです。空の場合、または別のポートを構成設定する必要がある場合、機器のウェブUI内のCumulocityメニューで構成設定可能です。「[Configuring the router（ルーターの構成設定）](#configure)」をご覧ください。

> USB／シリアル変換装置の中には、エコーモードが初期設定で有効となっているものもあり、これはModbus通信の作動を完全に止めてしまう可能性があります。そのような変換装置をお持ちの場合、無効化手順についてメーカーにお問い合わせください。

> NTC-140W型はModbus RTUに対応していませんので、該当する機能がUIに表示されません。


その場合の手順は以下の通りです。

* [サポート担当](https://support.cumulocity.com)に連絡し、自分のアカウントをCloud Fieldbusアプリケーションで使えるようにしてもらう。
* [Cloud Fieldbusのユーザーガイド](/guides/users-guide/cloud-fieldbus)に記載の通り、Modbus通信の構成設定を行う。
* 機器のウェブUI内のCumulocityメニューで「Modbus read only（Modbus読み取り専用）」プロパティを設定することにより、書き込みパーミッションを有効化または無効化する。「[Configuring the router（ルーターの構成設定）](#configure)」をご覧ください。 0に設定すると書き込みパーミッションの許可を意味し、1はModbusの書き込みパーミッションを許可しないことを意味します。

## <a name="logs"></a>ログビュアー

各種ログを機器からダウンロードし、閲覧することができます。ログファイルはかなり大きい場合がありますので、関心のある内容のみ閲覧した場合、フィルタリング基準を設定することができます。

右側で日付範囲を設定し、ログファイルを選択することができます。次に、テキストを検索し、合致する行のみ機器から取得することができます合致する行を制限することもできます。

取得されたログが下のリストに表示されます。クリックするとログファイルの内容がページの下部に表示されます。前回リクエストしたログが自動的に開かれます。

![ログビュアー](/guides/devices/netcomm/logs.png)
