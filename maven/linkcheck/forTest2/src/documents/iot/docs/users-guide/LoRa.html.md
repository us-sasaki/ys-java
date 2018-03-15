---
order: 90
title: LoRa Actility
layout: default
---

テスト用のリンク
[ファイルがないリンク](/iot/docs/users-guide/notfound)
[外部リンク](http://www.ntt.com/)
<a href="http://developer.ntt.com/hoge>外部リンク</a>
<img src="http://developer.ntt.com/hoge.png>外部イメージ</img>
![ファイルがない画像リンク](/iot/docs/users-guide/notfound.png)


## <a name="overview"></a>概要
Things Cloud ではActilityのThingPark Wirelessを介してLoRaデバイスと接続し、以下の機能が使用できます。

* Thngs Cloud デバイスマネジメントを使用してLoRaデバイスのプロビジョニング/プロビジョニング解除が簡単にできます。その際、ThingParkユーザインターフェイスでの作業は必要ありません。
* ウェブ基盤のUIを使用してアップストリームペイロードパケットのデコード。
* Things Cloudのイベントを通じて、生のデバイスデータをデバッグし、後処理を実行。
* Things Cloud の操作よりダウンストリームデータをデバイスへ送信。
* 既存のThings Cloud機能をLoRaデバイスでも活用。機能例：接続監視、デバイス管理、ダッシュボード上のデータ可視化、リアルタイム解析など。

下図ではThings CloudのLoRa Actility統合の概要を記載しております。

![Cumulocity LoRa Actility integration](/iot/docs/users-guide/actility/Cumulocity-LoRa-Actility-integration.png)

以下のセクションでそれぞれの方法について説明しています。

* Things Cloud 上で[ThingPark アカウント証明書の設定。](#configure-credentials)
* Things Cloudのデバイスデータベースを使用して[デバイスタイプを作成。](#create-device-types)
* Things Cloud 上で[デバイスを登録](#register-device) し、Actilityのペイロードを可視化する。
* ThingsParkで[デバイスのプロビジョニング解除。](#deprovision-device)
* デバイスに[オペレーションを送信。](#configurable-port)

注意：サブスクリプションにこの機能が含まれている必要があります。このドキュメント上に記載されている機能が表示されない場合は、サポートへお問い合わせ下さい。

## <a name="configure-credentials"></a>ThingPark アカウント資格情報の設定

Things CloudでLoRaデバイスを使用する前に、Things Cloundアドミニストレーションアプリケーション上でThingsParkアカウントの詳細を設定する必要があります。新規に資格情報を作成、または既存の資格情報を置き換える場合はアドミニストレーションアプリケーションを開き、ナビゲータの"設定"で"接続"を選択して下さい。

### <a name="create-new-credentials"></a>新規アカウント資格情報の作成

初めて"接続”を開くと資格情報の入力が求められます。
次の情報を入力します：

- **プロフィールID**: ThingsParkのアカウントと環境によって異なります。例えばDev1 ThingsPark環境を使用している場合、プロフィールIDは”dev1-api”となります。
- **ユーザ名**: ThingParkで使用しているユーザ名
- **パスワード**: ThingParkで使用しているパスワード
- **アプリケーション EUI**: デバイスのアプリケーションプロバイダを一意に識別するIEEE EUI64アドレススペースのグローバルアプリケーションIDになります。

プロフィールID、ユーザ名、およびパスワードは、アクセストークンを取得してThingsParkプラットフォームへさらにリクエストを送信するために使用されます。アカウントの資格情報を置き換えることによってアクセストークンを更新することも可能です。

![Register devices](/iot/docs/users-guide/actility/credentials-new-2.png)

"保存”のボタンをクリックして下さい。問題がなければ、"資格情報は保存されました”とメッセージが表示されます。

### <a name="replace-credentials"></a>アカウント資格情報の置き換え

資格情報を置き換える場合は、"資格情報の置き換え"ボタンをクリックします。

プロフィールID、ユーザ名、パスワード、およびアプリケーションEUIを入力します。"プロフィールID"と”アプリケーションEUI”に関しては上記の[新規アカウント資格情報の作成](#create-new-credentials)をご参照下さい。

<img src="/iot/docs/users-guide/actility/providerCredentials2.png" alt="Account credentials" style="max-width: 100%">

"保存"ボタンをクリックして下さい。これで資格情報は新規のものに置き換えられます。

## <a name="create-device-types"></a>デバイスデータベースを利用してデバイスタイプを作成
LoRaデバイスのデータを処理するにあたって、Things Cloudはデバイスのペイロード形式を理解する必要があります。

デバイスデータベースを利用してデバイスタイプを作成するには、デバイスマネジメントアプリケーションへ移動し、ナビゲータの"デバイスタイプ"で"デバイスデータベース”を選択します。既存のデータタイプをインポートをするか、新規にデバイスタイプを作成することができます。

### <a name="import-device-type"></a>定義済みのデバイスタイプのインポート
デバイスデータペース画面の"インポート”ボタンをクリックします。

例えば、"LoRaWAN Demonstrator"など、定義済みのデバイスタイプを選択し、"インポート”ボタンをクリックします。

<img src="/iot/docs/users-guide/actility/deviceDatabaseImport.png" alt="Import a predefined device type" style="max-width: 60%">

また、ファイルからデバイスタイプをロードしてインポートすることもできます。

### <a name="create-new-device-type"></a>新規デバイスタイプの作成

デバイスデータベース画面の"新規"ボタンをクリックします。
以下の画面が開きます：

<img src="/iot/docs/users-guide/actility/deviceDatabase-createNew.png" alt="Create new device type" style="max-width: 100%">

"LoRa"をデバイスタイプとして選択し、デバイスタイプの名前を入力します。

次にUIセクションで、メッセージタイプを決定します。LoRaデバイスはタイプごとに異なるエンコーディングで異なるメッセージを送信することが可能です。デバイスに応じて、メッセージのFPortパラメータ(Source: FPort)またはメッセージペイロード自身のサブセット(Source: Payload)のいずれかを調べることで、タイプを判別できます。

”ソース”のドロップダウンボックスで、メッセージタイプのエンコード形式を選択します。

- **FPort**: メッセージのFPortパラメータを調べることでメッセージのタイプを判断できる場合
- **Payload**: メッセージペイロード自身のサブセットを調べることでメッセージタイプを判断できる場合

次のペイロード構造の例では、最初のバイト（ハイライト箇所）がメッセージタイプソースを示します。

<img src="/iot/docs/users-guide/actility/payload-example1.png" alt="Example payload: message type source" style="max-width: 100%">

ユーザインタフェースでは、このようなメッセージタイプソースの情報を次のように入力できます：ペーロードでメッセージタイプ情報が開始する"開始ビット”部分とこの情報の長さを表す"ビット数”部分で定義します。例えば、開始ビット＝"0"　ビット数＝"8"などを入力します。

<img src="/iot/docs/users-guide/actility/messagetype-payload.png" alt="Message type payload" style="max-width: 100%">

"追加"ボタンをクリックし、値の設定を作成します。

<img src="/iot/docs/users-guide/actility/deviceDatabase1a.png" alt="Device type: new" style="max-width: 100%">

次のような画面が表示されます。例題のように、該当する値を設定します。

<img src="/iot/docs/users-guide/actility/deviceDatabase4.png" alt="Value configuration: new" style="max-width: 60%">

この値の設定でメッセージタイプのペイロード値をThings Cloudデータへマッピングされます。

デバイスメッセージの仕様に従って”メッセージID”を設定し、それをThings Cloudデータにマッピングします。 メッセージIDは、メッセージタイプを識別する数値です。 これは、デバイスタイプのメインページ（PayloadまたはFPort）で指定されたソースにあるメッセージIDと一致します。 メッセージIDは10進数ではなく16進数で入力する必要があります。

このペイロード構造例では、メッセージIDは”１”となっています。

<img src="/iot/docs/users-guide/actility/payload-example2.png" alt="Example payload: message type source" style="max-width: 100%">

<img src="/iot/docs/users-guide/actility/deviceDatabase4a.png" alt="Value configuration: message type" style="max-width: 60%">

”値”リストに分類するために新規値の一般情報を入力して下さい。この値に関する"名前”は"カテゴリの表示”の下に表示されます。

"値の選択”では値を抽出する場所を定義します。"開始ビット"部分に値情報が開始する場所を入力し、"ビット数"部分に情報の長さを指定して下さい。

次の例では"Channel 1 Type"情報はbyte 2で開始し（開始ビット="16")、長さは1 byteとなっています。（ビット数="8").

<img src="/iot/docs/users-guide/actility/payload-example3.png" alt="Example payload: value selection" style="max-width: 100%">

<img src="/iot/docs/users-guide/actility/deviceDatabase4b.png" alt="Value selection" style="max-width: 60%">

16進数の値は10進数で変換され、その後、”値の正規化”が適用されます。

"値の正規化"では、プラットフォームに格納する前に生の値を変換する方法を定義し、次の値を入力します：

- **乗数**: この値は"値の選択”から抽出された値と乗算されます。10進数、負数、および正数が使用可能です。デフォルトでは１に設定されています。
- **オフセット**: この値は、加算または減算されるオフセット値を定義します。10進数、負数、および正数が使用可能です。デフォルトでは０に設定されています。
- **単位** (オプション): 値と共に保存される単位を定義することができます。（例えば、摂氏温度単位”ｃ”など）

ペイロードのデコード方法の詳細については、各デバイスのマニュアルをご参照下さい。

必要に応じて、次のオプションを選択して下さい。”符号付き”（値が符号付き数値の場合）または”パック10進数”（値がBCDエンコードの場合）。
機能の項目では、このデバイスタイプの動作方法を定義します：

- **メジャーメントを送信**: デコードされた値でメジャーメントを作成
- **アラームを発生**: 値が0でない場合にアラームを生成
- **イベントを送信**: デコードされた値でイベントを作成
- **マネージドオブジェクトを更新**: デコードされた値でマネージドオブジェクトのフラグメントを更新

また、メジャーメント、イベント、およびマネージドオブジェクトフラグメント内に複数の値を含む入れ子構造をもつことができます。メジャーメントの場合、同じタイプのプロパティが併合され、入れ子構造が作成されます。イベントまたはマネジドオブジェクトの場合は同じフラグメントをもつプロパティが併合され、入れ子構造が作成されます。（以下のデバイスタイプ："Position"の入れ子構造[例](#nested-structure-example)もご参照下さい。）

"OK"をクリックすることで、デバイスタイプに値が追加されます。

![Value configurations of created device type](/iot/docs/users-guide/actility/deviceDatabase1.png)

"保存"をクリックし、定義された値でデバイスタイプを作成します。

**単一プロパティ例**

次の図は値（バッテリレベル）が変化したときにメジャーメントを送信するメッセージの一例です。

<img src="/iot/docs/users-guide/actility/deviceDatabase2.png" alt="Value configuration in detail: measurement" style="max-width: 50%">


**<a name="nested-structure-example"></a>入れ子構造例**

次の図は、GPSデバイスの現在置を報告するデバイスタイプの入れ子構造の一例です。デバイスタイプ名は”Position"で緯度と経度の値が含まれています。

"メッセージID"は全ての値で同じにする必要があります。上記の手順に従い、残りのパラメータを入力します。"マネージドオブジェクトフラグメント”の部分に"c8y_Position"を入力し、緯度と経度のそれぞれに新しい値を作成します。

<img src="/iot/docs/users-guide/actility/deviceDatabase5-lon.png" alt="Value creation: Longitude-nested" style="max-width: 60%">

<img src="/iot/docs/users-guide/actility/deviceDatabase5-lat.png" alt="Value creation: Latitude-nested" style="max-width: 60%">

次の通りの結果となります：

![Value configuration in detail: nested structure](/iot/docs/users-guide/actility/deviceDatabase5.png)

## <a name="register-device"></a>LoRaデバイスの登録

LoRaデバイスを登録するには、デバイスマネジメントアプリケーションを開き、クイックリンクにある"デバイス登録”をクリックします。"デバイスの登録”をクリックすると以下の画面が表示されます：

![Register devices](/iot/docs/users-guide/actility/deviceRegistration1.png)

"LoRa"をクリックします。

次の画面で以下の必要事項を入力して下さい：

- **デバイスプロファイル**: ドロップダウンリストより、該当するデバイスプロファイルを選択。
- **デバイスタイプ**: ドロップダウンリストより、該当デバイスタイプを選択。
- **デバイスEUI**: デバイスを一意に識別する値です。デバイス自身に記載されています。
- **アプリケーション・キー**: こらはデバイスに固有のAES-128アプリケーションキーです。アプリケーション所有者によってデバイスに割当てられ、JOIN通信の暗号化を担います。このキーはデバイス自身に記載されています。
- **接続プラン**: ドロップダウンリストより、該当する接続プランを選択。

次の図はデバイス登録の一例です。

![Register devices](/iot/docs/users-guide/actility/deviceRegistration3.png)

"次へ"をクリックするとデバイス登録リクエストが送信され、デバイスが作成されます。

イベントが実際に入ってきているか確認することで、デバイスが接続されているか確かめることができます。"イベント"タブを開けばイベントが入ってきているかどうか確認できます。このデバイスに関連するデバイスがここに全て表示されます。

接続されたデバイスの管理と表示についての詳細は[デバイスマネジメント](/iot/docs/users-guide/device-management)をご参照下さい。

## <a name="deprovision-device"></a>LoRa デバイスのプロビジョニング解除

LoRaデバイスのプロビジョニング解除をThingParkプラットフォーム上で行うことができます。これをすると、デバイスはネットワークに接続されなくなります。Things Cloud上で履歴は残りますが、ThingPark上でデバイスは削除されます。

デバイスのプロビジョニングを解除するには、デバイスマネジメントアプリケーションを開き、該当デバイスを選択して下さい。次に歯車アイコンをクリックし、"プロビジョニング解除”を選択して下さい。

<!-- ■■ 実デバイスが登録できていないため、カット(2018/3/2)
<img src="/iot/docs/users-guide/actility/deprovisionDevice.png" alt="Device deprovisioning" style="max-width: 100%">
-->

プロビジョニング解除を確認した後、デバイスはThingPark上でプロビジョニング解除されます。
 
## <a name="configurable-port"></a>オペレーションの送信

オペレーションを送信するには、デバイスマネジメントアプリケーションを開き、オペレーションの送信先となる該当デバイスを選択します。次に"シェル”タブをクリックします。

<!-- ■■ 実デバイスが登録できていないため、カット(2018/3/2)
以下のスクリーンショットでは特定のデバイスタイプの定義済みコマンドと形式のいくつかの例が記載されています。
<img src="/iot/docs/users-guide/actility/predefinedcommands.png" alt="Predefined commands" style="max-width: 100%">
-->

シェルコマンドを入力するか、または">_Command"項目で定義済みのコマンドを表示/編集します。

ポートを定義せずにコマンドを入力すると、デバイスのデフォルトのポート（例えば”１”など）に送信されます。コマンドを入力してポートを定義（"command:port"形式）すると、指定されたポートへ送信されます。

<!-- ■■ 実デバイスが登録できていないため、カット(2018/3/2)
<img src="/iot/docs/users-guide/actility/portConfiguration.png" alt="Port configuration" style="max-width: 100%">
-->

"実行"をクリックすると、オペレーションがデバイスへ送信されます。送信のタイミングはThingPark Actilityに依存します。








