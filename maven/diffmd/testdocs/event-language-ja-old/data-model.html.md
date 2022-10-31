---
order: 20
title: Data model
layout: default
toc: true
---

## 入力ストリーム

### 一般的な構造

入力ストリームはすべて同様の構造を持っています。

| パラメータ | データ型 | 説明 |
|:--|:----------|:-------------|
| _type | String | イベントの型。下表にある、それぞれのストリームに適用される型値を見てください |
| _mode | String | Cumulocityに送信されるデータの処理モード。[[処理モード]]参照。 |
| _origin | String | イベントの作成元。CEPルールで作成されたデータの場合"cep"となります。 |
| payload | Object | イベントに含まれる実データ |

型

| ストリーム | 型 |
|:--|:----------|
| ManagedObjectCreated | MANAGED_OBJECT_CREATE |
| ManagedObjectUpdated | MANAGED_OBJECT_UPDATE |
| ManagedObjectDeleted | MANAGED_OBJECT_DELETE |
| EventCreated | EVENT_CREATE |
| EventDeleted | EVENT_DELETE |
| MeasurementCreated | MEASUREMENT_CREATE |
| MeasurementDeleted | MEASUREMENT_DELETE |
| OperationCreated | OPERATION_CREATE |
| OperationUpdated | OPERATION_UPDATE |
| AlarmCreated | ALARM_CREATE |
| AlarmUpdated | ALARM_UPDATE |

もっと簡単にアクセスするには、APIに定義されたパラメータを使って、それぞれのストリームのデータ型の payloadを直接受け取ることができます。

| API | パラメータ | データ型 |
|:--|:----------|:-------------|
| Inventory | managedObject | [ManagedObject](/guides/event-language/data-model#managedobject) |
| Events | event | [Event](/guides/event-language/data-model#event) |
| Measurements | measurement | [Measurement](/guides/event-language/data-model#measurement) |
| Device control | operation | [Operation](/guides/event-language/data-model#operation) |
| Alarms | alarm | [Alarm](/guides/event-language/data-model#alarm) |

### 管理オブジェクト

クラス： com.cumulocity.model.ManagedObject

| パラメータ | データ型 | 説明 |
|:--|:----------|:-------------|:----------|
| id | ID | 管理オブジェクトのID |
| type | String | 管理オブジェクトの型 |
| name | String | 管理オブジェクトの名称 |
| lastUpdated | Date | 管理オブジェクトの最終更新時間 |
| owner | String | 管理オブジェクトの所有者 |
| childAssets | Object[] | 子アセットすべてのIDの配列 |
| childDevices | Object[] | 子デバイスすべてのIDの配列 |
| assetParents | Object[] | 親アセットすべてのIDの配列 |
| deviceParents | Object[] | 親デバイスすべてのIDの配列 |


親や子への参照の Object[] にはIDのみを含みます。キャスト関数を使用できます：
cast(event.managedObject.childAssets[0], com.cumulocity.model.ID)

例：

    select
      event.managedObject.id,
      event.managedObject.type,
      event.managedObject.name,
      event.managedObject.lastUpdated,
      event.managedObject.owner,
      event.managedObject.childAssets,
      event.managedObject.childDevices,
      event.managedObject.assetParents,
      event.managedObject.deviceParents
    from ManagedObjectCreated event;

### イベント

クラス： com.cumulocity.model.event.Event

| パラメータ | データ型 | 説明 |
|:--|:----------|:-------------|:----------|
| id | ID | イベントのID |
| creationTime | Date | イベントがデータベースに作成された時間 |
| type | String | イベントの型 |
| text | String | イベントのテキスト |
| time | Date | イベントが発生した時間(デバイスから送信されたもの) |
| source | ID | イベントを生成したデバイスのID |

例：

    select
      event.event.id,
      event.event.creationTime,
      event.event.type,
      event.event.text,
      event.event.time,
      event.event.source
    from EventCreated event;


### Measurement

クラス: com.cumulocity.model.measurement.Measurement

| パラメータ | データ型 | 説明 |
|:--|:----------|:-------------|:----------|
| id | [ID](/guides/event-language/data-model#id) | MeasurementのID |
| type | String | Measurementの型 |
| time | Date | Measurementが作成された時間 (デバイスから送信したもの) |
| source | [ID](/guides/event-language/data-model#id) | Measurementを生成したデバイスのID |

例:

    select
      event.measurement.id,
      event.measurement.type,
      event.measurement.time,
      event.measurement.source
    from MeasurementCreated event;


### オペレーション

クラス： com.cumulocity.model.operation.Operation

| パラメータ | データ型 | 説明 |
|:--|:----------|:-------------|:----------|
| id | ID | オペレーションのID |
| creationTime | Date | オペレーションがデータベースに生成された時間 |
| status | OperationStatus | オペレーションの現在ステータス |
| deviceId | ID | オペレーションが実行されるデバイスのID |

例：

    select
      event.operation.id,
      event.operation.creationTime,
      event.operation.status,
      event.operation.deviceId
    from OperationCreated event;

### アラーム

クラス: com.cumulocity.model.event.Alarm

| パラメータ | データ型 | 説明 |
|:--|:----------|:-------------|:----------|
| id | ID | アラームのID |
| creationTime | Date | データベースにアラームが生成された時間 |
| type | String | アラームのタイプ |
| count | long | アクティブな間に報告されたアラームの回数 |
| severity | Severity | アラームの重大度 |
| status | AlarmStatus | アラームのステータス |
| text | String | イベントのテキスト |
| time | Date | イベントが生成された時間(デバイスから送信されたもの) |
| source | ID | アラームを生成したデバイスのID |

例：

    select
      event.alarm.id,
      event.alarm.creationTime,
      event.alarm.type,
      event.alarm.count,
      event.alarm.severity,
      event.alarm.status,
      event.alarm.text,
      event.alarm.time,
      event.alarm.source
    from AlarmCreated event;

## 出力ストリーム

### 一般的な構造

出力ストリームによって、 Cumulocity のデータに対して CREATE, UPDATE, DELETE を行うことができます。データの更新や削除を行う場合、対象となるオブジェクトのIDを準備する必要があります。データ生成では、Cumulocity はイベント処理中に設定されていなければIDを生成します。
データ生成では、設定すべき特定のパラメータもあります(REST APIでのものと同様です)。リストにある既定パラメータ以外に、データには自由にカスタムfragmentを追加することもできます。カスタムfragmentを追加する場合、---->this<----を見てください。

注：独自のID生成は管理オブジェクトのみで利用可能です。

### 管理オブジェクト

| 利用可能な出力 |
|:-|
| CreateManagedObject |
| UpdateManagedObject |
| DeleteManagedObject |
 | パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| id | ID または String | 管理オブジェクトのID | UPDATE, DELETEの場合 |
| type | String | 管理オブジェクトの型 | いいえ |
| name | String | 管理オブジェクト名 | いいえ |
| owner | String | 管理オブジェクトの所有者。イベント処理で生成されたデータで設定されていない場合、所有者は"cep"になります | いいえ |
| childAssets | Set<String> または Set<ID> | 子アセットすべてのID | いいえ |
| childDevices | Set<String> または Set<ID> | 子デバイスすべてのID | いいえ |

例：

    insert into CreateManagedObject
    select
      "myManagedObject" as name,
      "myType" as type
    from EventCreated event;

    insert into UpdateManagedObject
    select
      "12345" as id,
      "myNewManagedObject" as name
    from EventCreated event;

    insert into DeleteManagedObject
    select
      "12345" as id
    from EventCreated event;


### イベント

| 利用可能な出力 |
|:----------------|
| CreateEvent |
| DeleteEvent |

| パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| id | ID or String | イベントのID | DELETE時 |
| type | String | イベントの型 | CREATE時 |
| text | String | イベントのテキスト | CREATE時 |
| time | Date | イベントが生成された時間(デバイスが送信したもの) | CREATE時 |
| source | ID または String | イベントを生成したデバイスのID | CREATE時 |

例：

    insert into CreateEvent
    select
      "copiedEventType" as type,
      "This event was copied" as text,
      event.event.time as time,
      event.event.source as source
    from EventCreated event;

    insert into DeleteEvent
    select
      "12345" as id
    from EventCreated event;

### Measurement

| 利用可能な出力 |
|:----------------|
| CreateMeasurement |
| DeleteMeasurement |

| パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| id | ID または String | MeasurementのID | DELETE時 |
| type | String | Measurementの型 | CREATE時 |
| time | Date | Measurement が生成された時間(デバイスから送信されたもの) | CREATE時 |
| source | ID または String | Measurementを生成したデバイスのID | CREATE時 |

例：

    insert into CreateMeasurement
    select
      "c8y_TemperatureMeasurement" as type,
      event.event.time as time,
      event.event.source as source,
      {
        "c8y_TemperatureMeasurement.T.value", 5
      } as fragments
    from EventCreated event;

    insert into DeleteMeasurement
    select
      "12345" as id
    from EventCreated event;

### オペレーション

| 利用可能な出力 |
|:----------------|
| CreateOperation |
| UpdateOperation |

| パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| id | ID または String | オペレーションのID | UPDATE時 |
| status | [[OperationStatus]] または String | オペレーションの現在の状態 | CREATE時 |
| deviceId | ID または String | オペレーションの対象となるデバイスのID | CREATE時 |

例：

    insert into CreateOperation
    select
      OperationStatus.PENDING as status,
      event.event.source as deviceId,
      {
        "c8y_Restart", {}
      } as fragments
    from EventCreated event;

    insert into UpdateOperation
    select
      "12345" as id,
      OperationStatus.EXECUTING as status
    from EventCreated event;

### アラーム

| 利用可能な出力 |
|:----------------|
| CreateAlarm |
| UpdateAlarm |

| パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| id | ID または String | アラームのID | UPDATE時 |
| type | String | アラームの型 | CREATE時 |
| severity | Severity または String | アラームの重大度 | CREATE時 |
| status | AlarmStatus または String | アラームの状態 | CREATE時 |
| text | String | イベントのテキスト | CREATE時 |
| time | Date | イベントが生成された時間(デバイスから送信されたもの) | CREATE時 |
| source | ID または String | アラームを生成したデバイスのID | CREATE時 |

例：

    insert into CreateAlarm
    select
      "c8y_HighTemperatureAlarm" as type,
      event.event.time as time,
      event.event.source as source,
      CumulocitySeverities.WARNING as severity,
      CumulocityAlarmStatuses.ACTIVE as status,
      "The device has high temperature" as text
    from EventCreated event;

    insert into UpdateAlarmn
    select
      "12345" as id,
      CumulocityAlarmStatuses.ACKNOWLEDGED as status
    from EventCreated event;

## 特殊なストリーム

この章で触れるストリームはCumulocityのデータベースにアクセスせず、外部サービスの起動を行います。

### SendMail

| パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| receiver | String | 受信者のメールアドレス | はい |
| cc | String | CCのメールアドレス | いいえ |
| bcc | String | BCCのメールアドレス | いいえ |
| replyTo | String | 送信メールに対するリプライの受信メールアドレス | いいえ |
| subject | String | メールの件名の行 | はい |
| text | String | メール本文 | はい |

receiver, cc, bcc には複数のメールアドレスを設定できます。それには、コンマ区切りですべてのメールアドレスを含んだ文字列を設定してください。
例　"receiver1@mail.com,receiver2@mail.com"

例：

    insert into SendEmail
    select
      "receiver1@cumulocity.com,receiver2@cumulocity.com" as receiver,
      "cc@cumulocity.com" as cc,
      "bcc@cumulocity.com" as bcc,
      "reply@cumulocity.com" as replyTo,
      "Example mail" as subject,
      "This mail was sent to test the SendEmail stream in Cumulocity" as text
    from AlarmCreated;

### SendDashboard

| パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| receiver | String | 受信者のメールアドレス | はい |
| cc | String | CCのメールアドレス | いいえ |
| bcc | String | BCCのメールアドレス | いいえ |
| replyTo | String | 送信メールに対するリプライの受信メールアドレス | いいえ |
| subject | String | メールの件名の行 | はい |
| text | String | メール本文 | はい |
| dashboardUrl | String | メールへの添付ファイルのURL | はい |

receiver, cc, bcc には複数のメールアドレスを設定できます。それには、コンマ区切りですべてのメールアドレスを含んだ文字列を設定してください。
例　"receiver1@mail.com,receiver2@mail.com"

注：　この機能は、あなたのテナントでダッシュボード送信できるサーバサイドエージェントが有効な場合のみ利用できます。[[この機能を有効化のしかた |  ]]

例：

    insert into SendDashboard
    select
      "receiver1@cumulocity.com,receiver2@cumulocity.com" as receiver,
      "cc@cumulocity.com" as cc,
      "bcc@cumulocity.com" as bcc,
      "reply@cumulocity.com" as replyTo,
      "Example dashboard" as subject,
      "https://mytenant.cumulocity.com/apps/cockpit/index.html" as dashboardUrl,
      "This mail contains an attached screenshot of the home dashboard" as text
    from AlarmCreated;


### SendSms

| パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| receiver | String | 受信者の電話番号 | はい |
| text | String | SMS本文。最大160文字 | はい |
| deviceId | String | SMSを生成したデバイスID。このデバイスに対しログイベントが生成される。 | いいえ |

receiver には複数の電話番号を設定できます。それには、"+49123456789,+49987654321"のようにコンマ区切りですべての電話番号を含んだ文字列を設定してください。技術的にはCumulocityでは国コードの設定は不要ですが、SMSゲートウェイが必要とするかもしれないため、設定することを推奨します。"0049"や"+49"のようないずれの表記も可能です(ドイツの場合)。

注：　この機能は、あなたのテナントがSMSプロバイダにつながっている場合のみ利用可能です。詳細は、 support@cumulocity.com にお問い合わせ下さい。

例：

    insert into SendSms
    select
      "+49123456789" as receiver,
      "This sms was sent to test the SendSms stream in Cumulocity" as text,
      "12345" as deviceId
    from AlarmCreated;

### SendPush

このストリームを使うことで Cumulocity から Telekom プッシュサービスを経由してモバイルアプリケーションにプッシュ通知を送ることができます。

| パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| type | String | Push Provider Type. Currently only TELEKOM is possible. | yes |
| message | String | The body of the push message. | yes |
| deviceId | String | The ID of the device generating the push message. | yes |

_Note:_

注：　この機能はあなたのテナントがプッシュプロバイダにつながっている場合のみ利用可能です。詳細は、 support@cumulocity.com にお問い合わせ下さい。

例：

    insert into SendPush
    select
    "TELEKOM" as type,
    "sample push message" as message,
    a.alarm.source.value as deviceId
    from AlarmCreated a;

### SendSpeech

| パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| phoneNumber | String | 受信者の電話番号 | はい |
| textToSpeech | String | 受信者に発声するテキスト | はい |
| deviceId | String | 電話通知を生成したデバイスのID。このデバイスに対しログイベントが生成されます | はい |
| attempts | Long | 受信者につながらない場合のリダイアル量(0 = リダイアルしない) | はい |
| timeout | Long | リダイアル間隔(分) | はい |
| alarmId | String | 電話通知アラームのID(確認用) | はい |
| questionText | String | 受信者に発声する確認質問 | いいえ |
| acknowledgeButton | Long | 電話確認で受信者にプッシュしてもらう番号 | いいえ |

注：　この機能はあなたのテナントがスピーチプロバイダにつながっている場合のみ利用可能です。詳細は、 support@cumulocity.com にお問い合わせ下さい。

例：

    insert into SendSpeech
    select
      "+4923456789" as phoneNumber,
      "Your device lost power connection." as textToSpeech,
      2 as attempts,
      5 as timeout,
      "12345" as deviceId,
      "67890" as alarmId,
      "To acknowledge this call please press button 5" as questionText,
      5 as acknowledgeButton
    from EventCreated e

## 追加のデータモデル

### ID

クラス： com.cumulocity.model.ID

| パラメータ | データ型 | 説明 | 必須 |
|:--|:----------|:-------------|:----------|
| value | String | 実際のID値 |
| type | String | IDの型 |
| name | String | デバイス名(measurement.source 内のような場合のみデバイスにリンクします |

例：

    select
      event.measurement.source.value,
      event.measurement.source.type,
      event.measurement.source.name
    from MeasurementCreated event;


### OperationStatus

クラス： com.cumulocity.model.operation.OperationStatus

OperationStatus は次のような列挙型です： PENDING, SUCCESSFUL, FAILED, EXECUTING

例：

    insert into UpdateOperation
    select
      event.operation.id.value as id,
      OperationStatus.FAILED as status
    from OperationCreated event;

### Severity(重大度)

クラス： com.cumulocity.model.event.Severity

Severity は CumulocitySeverities で定義される列挙型です。CumulocitySeverities は次の値を提供します： CRITICAL, MAJOR, MINOR, WARNING

例：

    insert into UpdateAlarm
    select
      event.alarm.id.value as id,
      CumulocitySeverities.MAJOR as severity
    from AlarmCreated event;

### AlarmStatus

クラス： com.cumulocity.model.event.AlarmStatus

AlarmStatus は、CumulocityAlarmStatused で定義される列挙型です。CumulocityAlarmStatuses は次の値を提供します： ACTIVE, ACKNOWLEDGED, CLEARED

例：

    insert into UpdateAlarm
    select
      event.alarm.id.value as id,
      CumulocityAlarmStatuses.ACKNOWLEDGED as status
    from AlarmCreated event;
