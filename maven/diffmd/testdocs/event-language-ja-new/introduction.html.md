---
order: 10
title: はじめに
layout: default
---

## 概要
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：9行目 ━━━━━━━━━━            ┃
┃Using the Cumulocity real-time event processing, you can add your own     ┃
┃logic to your IoT solution. This 【includes,】 but 【】 is not limited    ┃
┃【to, data analytics logic.】 To define new analytics, you will use the   ┃
┃Cumulocity Event Language. The language allows to analyze incoming        ┃
┃【data】 using a powerful pattern and time window based query language.   ┃
┃【It also allows you to】 create, update and delete your data in          ┃
┃real-time.                                                                ┃
┃━━━━━━━━━━ 原文更新後(1)：9行目 ━━━━━━━━━━            ┃
┃Using the Cumulocity real-time event processing, you can add your own     ┃
┃logic to your IoT solution. This 【includes data analytics logic】 but    ┃
┃【it】 is not limited 【to it.】 To define new analytics, you will use    ┃
┃the Cumulocity Event Language. The language allows to analyze incoming    ┃
┃【data. It is】 using a powerful pattern and time window based query      ┃
┃language. 【You can】 create, update and delete your data in real-time.   ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

Cumulocityのリアルタイムイベント処理を使って、あなたのIoTソリューションに独自のロジックを追加できます。これには（これに限りませんが）データ解析ロジックが含まれます。新しい解析を定義するには、Cumulocityイベント言語を使います。
この言語によって、強力なパターン、ウィンドウベースのクエリ言語を使って入力データを解析することができます。
さらにこの言語により、データの生成、更新、削除をリアルタイムに行うことができます。

リアルタイム解析の典型的なユースケースには以下のようなものがあります。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：12行目 ━━━━━━━━━━           ┃
┃【*】 Remote control: Turn a device off if it's temperature 【goes】 over ┃
┃40 degrees.                                                               ┃
┃━━━━━━━━━━ 原文更新後(1)：13行目 ━━━━━━━━━━           ┃
┃【                                                                        ┃
┃*】 Remote control: Turn a device off if it's temperature 【rises】 over  ┃
┃40 degrees.                                                               ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫
* 遠隔操作：温度が40℃を超えたら、デバイスの電源を切る
┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
* 値評価：負のメータ値、または前回値より低いメータ値を捨てる
* 派生データ：1日,1自動販売機での売上高を計算する
* 集計：1日,1顧客での自動販売機の売上総和を求める
* 通知：自分のマシンのいずれかの電源が落ちていたらメールを送ってほしい
* 圧縮：車全体は５分間隔で場所を更新(でも画面で見ている車はリアルタイムに送信)

続く章ではCumulocityイベント言語がどのように利用できるか、どうやって独自の解析やサーバサイド業務ロジック、自動化をするのかの基本を記述しています。


## イベントストリーム
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：23行目 ━━━━━━━━━━           ┃
┃In the Cumulocity Event Language data flows 【through】 streams. You can  ┃
┃create events in streams and listen 【for】 events created in streams.    ┃
┃━━━━━━━━━━ 原文更新後(1)：25行目 ━━━━━━━━━━           ┃
┃In the Cumulocity Event Language data flows 【in】 streams. You can       ┃
┃create events in streams and listen 【to】 events created in streams.     ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

Cumulocityイベント言語では、データはストリームを流れます。ストリームにイベントを生成したり、ストリームに生成されたイベントを監視することができます。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
### 定義済みのストリーム
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：27行目 ━━━━━━━━━━           ┃
┃There are some predefined streams to interact with several Cumulocity     ┃
┃APIs. For each input stream Cumulocity will automatically create a new    ┃
┃event when the respective API call was 【made e.g. if】 a measurement was ┃
┃created via REST API there will be a new event in the MeasurementCreated  ┃
┃stream.                                                                   ┃
┃For interacting with the Cumulocity backend you can create an event on    ┃
┃the respective output stream and Cumulocity will automatically execute    ┃
┃either the database query or create the API calls necessary for sending   ┃
┃mails, sms, 【etc. e.g. to】 create a new alarm in the database you can   ┃
┃create a new event in the CreateAlarm stream.                             ┃
┃━━━━━━━━━━ 原文更新後(1)：29行目 ━━━━━━━━━━           ┃
┃There are some predefined streams to interact with several Cumulocity     ┃
┃APIs. For each input stream Cumulocity will automatically create a new    ┃
┃event when the respective API call was 【made. If】 a measurement was     ┃
┃created via REST API there will be a new event in the MeasurementCreated  ┃
┃stream.                                                                   ┃
┃For interacting with the Cumulocity backend you can create an event on    ┃
┃the respective output stream and Cumulocity will automatically execute    ┃
┃either the database query or create the API calls necessary for sending   ┃
┃mails, sms, 【or similar. To】 create a new alarm in the database you can ┃
┃create a new event in the CreateAlarm stream.                             ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

いくつかのCumulocity APIで使用される定義済みのストリームがあります。各APIコールが発生した際、Cumulocityはそれぞれの入力ストリームに対して新しいイベントを自動生成します。例えば、REST APIでMeasurementが生成された場合、MeasurementCreatedストリームに新しいイベントが生成されます。Cumulocityのバックエンドにアクセスするため、各出力ストリームにイベントを設定することができ、それによりCumulocityは自動的にDBクエリやメール,SMS等の送信に必要なAPIコールを実行します。例えば、データベースに新規アラームを生成するためには、CreateAlarmストリームに新規イベントを作成すればよいです。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
| API | 入力ストリーム | 出力ストリーム | 説明 |
|:--|:----------|:-------------|:----------|
| Inventory | ManagedObjectCreated<br/>ManagedObjectUpdated<br/>ManagedObjectDeleted | CreateManagedObject<br/>UpdateManagedObject<br/>DeleteManagedObject | このイベントグループは１つの管理オブジェクトの生成、変更、削除を表します。 |
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：33行目 ━━━━━━━━━━           ┃
┃【|Events|EventCreated<br/>EventDeleted|CreateEvent<br/>DeleteEvent|This】┃
┃group of events represents creation or deletion of a single Event.|       ┃
┃━━━━━━━━━━ 原文更新後(1)：35行目 ━━━━━━━━━━           ┃
┃【|Events|EventCreated<br/>EventUpdated<br/>EventDeleted|CreateEvent<br/>UpdateEvent<br/>DeleteEvent|This】┃
┃group of events represents creation or deletion of a single Event.|       ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫
| Events | EventCreated<br/>EventDeleted|CreateEvent<br/>DeleteEvent | このイベントグループは１つのイベントの生成、削除を表します。 |
┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
| Measurements | MeasurementCreated<br/>MeasurementDeleted | CreateMeasurement<br/>DeleteMeasurement|このイベントグループは１つのMeasurementの生成、削除を表します。 |
| Device control | OperationCreated<br/>OperationUpdated | CreateOperation<br/>UpdateOperation | このイベントグループは１つのオペレーションの生成、変更を表します。|
| Alarms | AlarmCreated<br/>AlarmUpdated | CreateAlarm<br/>UpdateAlarm|このイベントグループは一つのアラームの生成、変更を表します。 |
| Emails | (適用なし) | SendEmail<br/>SendDashboard | このイベントグループは、メール送信を表します。 |
| SMS | (適用なし) | SendSms | このイベントグループは、SMS送信を表します。 |
| Text-to-speech | (適用なし) | SendSpeech | このイベントグループは、電話の初期化を表します。 |
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：41行目 ━━━━━━━━━━           ┃
┃【Please check out】 the data model to see how the events 【on】 each     ┃
┃stream are structured.                                                    ┃
┃━━━━━━━━━━ 原文更新後(1)：43行目 ━━━━━━━━━━           ┃
┃【Look at】 the data model to see how the events 【for】 each stream are  ┃
┃structured.                                                               ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

それぞれのストリームでイベントがどう構成されるかは、[[データモデル]]を確認して下さい。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
### ストリームにイベントを生成する
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：45行目 ━━━━━━━━━━           ┃
┃Creating an event is done by the keywords `insert into` and `select`.     ┃
┃First you need to specify 【with】 the "insert into" followed by the      ┃
┃stream name for which stream you want to create an event. After that you  ┃
┃can use the "select" clause to specify the parameters of the event.       ┃
┃━━━━━━━━━━ 原文更新後(1)：47行目 ━━━━━━━━━━           ┃
┃Creating an event is done by the keywords `insert into` and `select`.     ┃
┃First you need to specify 【】 the "insert into" followed by the stream   ┃
┃name for which stream you want to create an event. After that you can use ┃
┃the "select" clause to specify the parameters of the event.               ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

キーワード insert into 、select によってイベント生成ができます。まず"insert into"に続けてイベントを生成するストリーム名を指定する必要があります。そして"select"節をイベントのパラメータ指定に使用します。パラメータは次の構文で指定します：<値> as <パラメータ>
コンマで区切ることで複数のパラメータを指定できます。パラメータ順序は関係ありません。ストリームには"select"節に指定する必須パラメータがある場合があることに注意してください。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
### ストリームのイベントを待ち受ける

ストリーム内の共通的なイベント生成トリガーは、他のストリームに何かが発生した場合です。したがって、他のストリームを待ち受けることでイベント待ち受けができます。キーワード"from"に続け、ストリーム名を指定し、(オプション)イベントを参照するための変数名を文の後ろに続けます。

## 条件

キーワード"where"で条件を追加すれば、入ってくるすべてのイベントに対してイベント生成をせず、指定条件のみに絞ることができます。whereキーワードに続けて真か偽の結果を持つ表現を記述します。and や or で複数の表現を記述することもできます。

## 例
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：58行目 ━━━━━━━━━━           ┃
┃As an example we 【now want to】 create a 【statement that combines the   ┃
┃learnings from the sections on this page.】 It should listen to a stream  ┃
┃and create a new event in another stream whenever the 【】 condition      ┃
┃applies.                                                                  ┃
┃━━━━━━━━━━ 原文更新後(1)：60行目 ━━━━━━━━━━           ┃
┃As an example we 【】 create a 【statement.】 It should listen to a       ┃
┃stream and create a new event in another stream whenever the              ┃
┃【specified】 condition applies.                                          ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

さあ、例としてこのページの章で学んだことを合わせた文を作ってみましょう。ストリームを監視し、条件が適用される場合、別のストリームに新しいイベントを生成します。例として、温度Measurementに対し、アラームを生成したいと思います。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：61行目 ━━━━━━━━━━           ┃
┃1. To create an alarm we need to `insert into` the stream                 ┃
┃【`CreateAlarm`】                                                         ┃
┃2. We need to specify all parameters for the event in the `select`        ┃
┃【clause】                                                                ┃
┃3. We want the alarm to be created when an event `from` the stream        ┃
┃`MeasurementCreated` is 【received】                                      ┃
┃4. We want the alarm only be created under certain conditions of the      ┃
┃event from the `MeasurementCreated` stream which we specific in the       ┃
┃`where` 【clause】                                                        ┃
┃━━━━━━━━━━ 原文更新後(1)：63行目 ━━━━━━━━━━           ┃
┃1. To create an alarm we need to `insert into` the stream                 ┃
┃【`CreateAlarm`.】                                                        ┃
┃2. We need to specify all parameters for the event in the `select`        ┃
┃【clause.】                                                               ┃
┃3. We want the alarm to be created when an event `from` the stream        ┃
┃`MeasurementCreated` is 【received.】                                     ┃
┃4. We want the alarm only be created under certain conditions of the      ┃
┃event from the `MeasurementCreated` stream which we specific in the       ┃
┃`where` 【clause.】                                                       ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫
1. アラームを生成するため、CreateAlarm に対し insert into します。
2. select 節にイベントに必要なすべてのパラメータを指定します。
3. MeasurementCreated ストリームにイベントが発生した場合にアラームを生成するため、from にストリームを指定します。
4. アラームを生成するのを、MeasurementCreated に対する特定の条件に限定するため、where節を使用します。
┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛

でき上がった文はこんな感じになるでしょう。

    insert into CreateAlarm
    select
      measurementEvent.measurement.time as time,
      measurementEvent.measurement.source.value as source,
      "c8y_TemperatureAlarm" as type,
      "Temperature measurement was created" as text,
      "ACTIVE" as status,
      "CRITICAL" as severity
    from MeasurementCreated measurementEvent
    where measurementEvent.measurement.type = "c8y_TemperatureMeasurement";
