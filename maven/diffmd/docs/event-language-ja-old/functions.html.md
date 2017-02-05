---
order: 30
title: Functions
layout: default
toc: true
---

## 概要

Cumulocityイベント言語では関数を利用できます。この章では利用可能な標準搭載の関数を網羅しています。独自に関数定義を行う場合は[[ここ| ]]のガイドを見てください。

## Java関数

すべてのモジュールで、次のライブラリが自動的にインポートされます。

    java.lang.*
    java.math.*
    java.text.*
    java.util.*

これらのライブラリにある任意の関数を利用することができます。

例：

java.util.Random を使う場合

    create variable Random generator = new Random();

    insert into CreateMeasurement
    select
      "12345" as source,
      "c8y_TemperatureMeasurement" as type,
      current_timestamp().toDate() as time,
      {
        "c8y_TemperatureMeasurement.T1.value", generator.nextInt(12) + 18,
        "c8y_TemperatureMeasurement.T1.unit", "C",
        "c8y_TemperatureMeasurement.T2.value", generator.nextInt(12) + 18,
        "c8y_TemperatureMeasurement.T2.unit", "C",
        "c8y_TemperatureMeasurement.T3.value", generator.nextInt(12) + 18,
        "c8y_TemperatureMeasurement.T3.unit", "C",
        "c8y_TemperatureMeasurement.T4.value", generator.nextInt(12) + 18,
        "c8y_TemperatureMeasurement.T4.unit", "C",
        "c8y_TemperatureMeasurement.T5.value", generator.nextInt(12) + 18,
        "c8y_TemperatureMeasurement.T5.unit", "C"
      } as fragments
    from pattern[every timer:at(*, *, *, *, *, */30)];

java.math.BigDecimal を使う場合

    select
      getNumber(m, "c8y_TemperatureMeasurement.T.value").divide(new BigDecimal(3),2,RoundingMode.HALF_UP)
    from MeasurementCreated m;

## データベース関数

履歴データにアクセスするため、データベースに直接次のような関数を利用することができます。
ほとんどの関数は、複数形式に対応しています：

-   findOne...(...): クエリ結果がただ１つのオブジェクトを含むことを期待し、そうでなければ失敗する。
-   findFirst...(...): クエリ結果の最初のオブジェクトを返却する。結果がなければ "null" を返却
-  findAll...(...): クエリ結果のすべてのオブジェクトを返却する。

以下は利用可能なすべての関数のリストです。略号 "..." は "findOne", "findFirst", "findAll" に置き換えてください。

| 関数名(および類似形式) | 返却型|引数リスト | 
|:----------------------------|:----------|:-------------------------|
| findManagedObjectById | ManagedObject | id*:String*<br/>id*:GId* | 
| findFirstManagedObjectParent<br/>findOneManagedObjectParent | ManagedObject | managedObjectId*:String*<br/>managedObjectId*:GId* | 
| ...ManagedObjectByFragmentType | List | fragmentType*:String* | 
| ...ManagedObjectByType | List | type*:String* | 
| findEventById | Event | id*:String*<br/>id*:GId* | 
| ...EventByFragmentType | List | fragmentType*:String* | 
| ...EventByFragmentTypeAndSource | List | fragmentType*:String*, source*:String* | 
| ...EventByFragmentTypeAndSourceAndTimeBetween | List | fragmentType*:String*, source*:String*, from*:Date*, to*:Date* | 
| ...EventByFragmentTypeAndSourceAndTimeBetweenAndType | List | fragmentType*:String*, source*:String*, from*:Date*, to*:Date*, type*:String* | 
| ...EventByFragmentTypeAndSourceAndType | List | fragmentType*:String*, source*:String*, type*:String* | 
| ...EventByFragmentTypeAndTimeBetween | List | fragmentType*:String*, from*:Date*, to*:Date* | 
| ...EventByFragmentTypeAndTimeBetweenAndType | List | fragmentType*:String*, from*:Date*, to*:Date*, type*:String* | 
| ...EventByFragmentTypeAndType | List | fragmentType*:String*, type*:String* | 
| ...EventBySource | List | source*:String* | 
| findMeasurementById | Measurement | id*:String*<br/>id*:GId* | 
| ...MeasurementByFragmentType | List | fragmentType*:String* | 
| ...MeasurementByFragmentTypeAndSource | List | fragmentType*:String*, source*:String* | 
| ...MeasurementByFragmentTypeAndSourceAndTimeBetween | List | fragmentType*:String*, source*:String*, from*:Date*, to*:Date* | 
| ...MeasurementByFragmentTypeAndSourceAndTimeBetweenAndType | List | fragmentType*:String*, source*:String*, from*:Date*, to*:Date*, type*:String* | 
| ...MeasurementByFragmentTypeAndSourceAndType | List | fragmentType*:String*, source*:String*, type*:String* | 
| ...MeasurementByFragmentTypeAndTimeBetween | List | fragmentType*:String*, from*:Date*, to*:Date* | 
| ...MeasurementByFragmentTypeAndTimeBetweenAndType | List | fragmentType*:String*, from*:Date*, to*:Date*, type*:String* | 
| ...MeasurementByFragmentTypeAndType | List | fragmentType*:String*, type*:String* | 
| ...MeasurementBySource | List | source*:String* | 
| findLastMeasurementByFragmentTypeAndSourceAndTimeBetween | Measurement | fragmentType*:String*, source*:String*, from*:Date*, to*:Date* | 
| findLastMeasurementByFragmentTypeAndSourceAndTimeBetweenAndType | Measurement | fragmentType*:String*, source*:String*, from*:Date*, to*:Date*, type*:String* |
| findLastMeasurementByFragmentTypeAndTimeBetween | Measurement | fragmentType*:String*, from*:Date*, to*:Date* | 
| findLastMeasurementByFragmentTypeAndTimeBetweenAndType | Measurement | fragmentType*:String*, from*:Date*, to*:Date*, type*:String* | 
| findOperationById | Operation | id*:String*<br/>id*:GId* | 
| ...OpererationByAgent | List | agentId*:String* | 
| ...OpererationByAgentAndStatus | List | agentId*:String*, status*:String* | 
| ...OpererationByDevice | List | deviceId*:String* | 
| ...OpererationByDeviceAndStatus | List | deviceId*:String*, status*:String* | 
| ...OpererationByStatus | List | status*:String* | 
| ...OpererationByCreationTimeBetween | List | from*:Date*, to*:Date* | 
| findAlarmById | Alarm | id*:String*<br/>id*:GId* | 
| ...AlarmBySource | List | sourceId*:String* | 
| ...AlarmBySourceAndStatus | List | sourceId*:String*, status*:String* | 
| ...AlarmBySourceAndStatusAndType | List | sourceId*:String*, status*:String*, type*:String* | 
| ...AlarmBySourceAndStatusAndTimeBetween | List | sourceId*:String*, status*:String*, from*:Date*, to*:Date* | 
| ...AlarmBySourceAndTimeBetween | List | sourceId*:String*, from*:Date*, to*:Date* |
| ...AlarmByStatus | List | status*:String* |
| ...AlarmByStatusAndTimeBetween | List | status*:String*, from*:Date*, to*:Date* |
| ...AlarmByTimeBetween | List | from*:Date*, to*:Date* |


## ヘルパー関数

### fragmentアクセス

fragment は次のヘルパー関数でアクセスできます：

-   Object getObject(Object event, String path[, Object defaultValue])
-   String getString(Object event, String path[, String defaultValue])
-   Number getNumber(Object event, String path[, Number defaultValue])
-   Boolean getBoolean(Object event, String path[, Boolean defaultValue])
-   Date getDate(Object event, String path[, Date defaultValue])
-   List getList(Object event, String path[, List defaultValue])

オブジェクト構造内では、JsonPath (ルート要素 $ を使用しない) で追跡できます。

例：

    select
      getNumber(m, "c8y_TemperatureMeasurement.T.value")
    from MeasurementCreated m;

    select
      e.event as event
    from EventCreated e
    where getObject(e, "c8y_Position") is not null;

### キャスト

cast() 関数により Object のような型で受け取ったデータ型を適切な型に変換できることがあります。Javaプリミティブ型へのキャスト：

    cast(myVariable, long)

他の型では、フルパッケージ名を含むクラス名を指定する必要があります。

    cast(event.managedObject.childAssets[0], com.cumulocity.model.ID)

### current_timestamp

current_timestamp() 関数では現在のサーバ時刻を取得できます。Cumulocityストリームで利用できる toDate() 関数によって簡単に Date データ型に変換できます。

例：

    insert into CreateAlarm
    select
      "c8y_HighTemperatureAlarm" as type,
      current_timestamp().toDate() as time,
      event.event.source as source,
      CumulocitySeverities.WARNING as severity,
      CumulocityAlarmStatuses.ACTIVE as status,
      "The device has high temperature" as text
    from EventCreated event;

### inMaintenanceMode

inMaintenanceMode() 関数で、デバイスがメンテナンスモードにあるかをすぐ調べられます。ID をパラメータとして取り、boolean 値を返却します。

例：

    insert into SendEmail
    select
      "receiver1@cumulocity.com,receiver2@cumulocity.com" as receiver,
      "cc@cumulocity.com" as cc,
      "bcc@cumulocity.com" as bcc,
      "reply@cumulocity.com" as replyTo,
      "Example mail" as subject,
      "This mail was sent to test the SendEmail stream in Cumulocity" as text
    from EventCreated e
    where not inMaintenanceMode(e.event.source);

### replaceAllPlaceholders

テキストの装飾に連結を利用することができます。

    insert into SendEmail
    select
      "receiver1@cumulocity.com,receiver2@cumulocity.com" as receiver,
      "cc@cumulocity.com" as cc,
      "bcc@cumulocity.com" as bcc,
      "reply@cumulocity.com" as replyTo,
      "Example mail" as subject,
      "An event with the text "  |  |  e.event.text  |  |  " has been created." as text
    from EventCreated e;

テキストが長くなり、データから動的に決まる値を多く持つ場合、replaceAllPlaceholders() 関数を利用することができます。この関数のもうひとつの利点は、現在のオブジェクトのみではなく、アラームを生成したりmeasuamentやイベントを発生させたオブジェクトの情報すべてにアクセスできることにあります。テキスト文字列に、値を示すJsonPath(ルート要素の$を除いたもの)を入れたハコ(placeholder)を置き、#{} でくくります。デバイス由来のデータにアクセスするには、JsonPath を source で開始してください。

ハコを含む文字列と、ハコを埋めるオブジェクトを指定して関数を呼べます。この場合、sourceデバイスが自動的に検索されます。

例：

    create variable string myMailText =
    "The device #{source.name} with the serial number #{source.c8y_Hardware.serialNumber} created an event with the text #{text} at #{time}. The device is located at #{source.c8y_Address.street} in #{source.c8y_Address.city}.";

    insert into SendEmail
    select
      "receiver1@cumulocity.com,receiver2@cumulocity.com" as receiver,
      "cc@cumulocity.com" as cc,
      "bcc@cumulocity.com" as bcc,
      "reply@cumulocity.com" as replyTo,
      "Example mail" as subject,
      replaceAllPlaceholders(myMailText, e.event) as text
    from EventCreated e;

### toNumberSetParameter

toNumberSetParameter() 関数により、モジュール外のタイマーパターンを設定できます。タイマーパターンを含むモジュールをデプロイすると、デプロイ時にパターンが固定され、モジュールを再デプロイするまで変更できません。しかしながら、デプロイ時に変数がすぐ解決できるなら、タイマーパターンを変数で設定することができます。これにより、例えば管理オブジェクトに格納されたデプロイ時にロードされたタイマーパターンを再設定することができます。toNumberSetParameter() 関数は、文字列をタイマーパターンの入力となる NumberSetParameter 型に変換します。タイマーパターンに関するさらなる情報は [[ここ |  ]] を見てください。

例：

    create variable ManagedObject device = findManagedObjectById("12345");
    create variable string minuteVal = getString(device, "config.minute");
    create variable string hourVal = getString(device, "config.hour");
    create variable string dayOfMonthVal = getString(device, "config.day");
    create variable string monthVal = getString(device, "config.month");
    create variable string dayOfWeekVal = getString(device, "config.weekday");

    insert into CreateOperation
    select
      "PENDING" as status,
      "12345" as deviceId,
      { "c8y_Restart", {} } as fragments
    from
     pattern [every timer:at(toNumberSetParameter(minuteVal),
     toNumberSetParameter(hourVal),
     toNumberSetParameter(dayOfMonthVal),
     toNumberSetParameter(monthVal),
     toNumberSetParameter(dayOfWeekVal))];
