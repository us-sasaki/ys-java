---
order: 40
title: Advanced use cases
layout: default
toc: true
---

## fragmentのカスタマイズ

Cumulocity APIにより、データ構造の自由な変更が可能となります。Cumulocityイベント言語でもそれがあてはまります。出力ストリームにはそれぞれカスタマイズできるfragmentを追加することができます。キー、値ペアのリストで、fragmentフィールドを設定することで、ストリームにfragmentを追加することができます。キーは値に対するフルJsonPathです。

    {
      key1, value1,
      key2, value2,
      key3, value3
    } as fragments

例１：

    insert into CreateMeasurement
    select
      "12345" as source,
      "c8y_TemperatureMeasurement" as type,
      current_timestamp().toDate() as time,
      {
        "c8y_TemperatureMeasurement.T1.value", 1,
        "c8y_TemperatureMeasurement.T1.unit", "C",
        "c8y_TemperatureMeasurement.T2.value", 2,
        "c8y_TemperatureMeasurement.T2.unit", "C",
        "c8y_TemperatureMeasurement.T3.value", 3,
        "c8y_TemperatureMeasurement.T3.unit", "C",
        "c8y_TemperatureMeasurement.T4.value", 4,
        "c8y_TemperatureMeasurement.T4.unit", "C",
        "c8y_TemperatureMeasurement.T5.value", 5,
        "c8y_TemperatureMeasurement.T5.unit", "C"
      } as fragments
    from EventCreated;

この結果は、次のJSON形式になります：

    {
      "type": "c8y_TemperatureMeasurement",
      "time": "...",
      "source": {
        "id": "12345"
      },
      "c8y_TemperatureMeasurement": {
        "T1": {
          "value": 1,
          "unit": "C"
        },
        "T2": {
          "value": 1,
          "unit": "C"
        },
        "T3": {
          "value": 1,
          "unit": "C"
        },
        "T4": {
          "value": 1,
          "unit": "C"
        },
        "T5": {
          "value": 1,
          "unit": "C"
        },
      }
    }

例２：

    insert into CreateManagedObject
    select
      "MyCustomDevice" as name,
      "customDevice" as type,
      {
        "c8y_IsDevice", {},
        "c8y_SupportedOperations", {"c8y_Restart", "c8y_Command"},
        "c8y_Hardware.serialNumber", "mySerialNumber",
        "c8y_Hardware.model", "myDeviceModel",
        "com_cumulocity_model_Agent", {},
        "c8y_RequiredAvailability.responseInterval", 30
      } as fragments
    from EventCreated e;

この結果は、次のJSON形式になります：

    {
      "name": "MyCustomDevice",
      "type": "customDevice",
      "c8y_IsDevice": {},
      "c8y_RequiredAvailability": {
        "responseInterval": 30
      },
      "c8y_SupportedOperations": [
        "c8y_Restart",
        "c8y_Command"
      ],
      "com_cumulocity_model_Agent": {},
      "c8y_Hardware": {
        "model": "myDeviceModel",
        "serialNumber": "mySerialNumber"
      }
    }


## 高度なトリガ  

いくつかのストリームにおいて、イベント到着によるトリガーの記述は一通りではありません。続く章ではトリガーの別の記述法、複合トリガーについて記載しています。

### Pattern

Pattern によって他のトリガーと連結、複合させることができるようになります。このようなトリガーがあったとします。

    from EventCreated e;
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：122行目 ━━━━━━━━━━          ┃
┃the functionality is identical with this trigger using a 【pattern】      ┃
┃━━━━━━━━━━ 原文更新後(1)：122行目 ━━━━━━━━━━          ┃
┃the functionality is identical with this trigger using a 【pattern.】     ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

機能としては pattern を使用したこのトリガーと同等になります。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    from pattern [every e=EventCreated];
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：126行目 ━━━━━━━━━━          ┃
┃It is also possible to add filters in the 【pattern】                     ┃
┃━━━━━━━━━━ 原文更新後(1)：126行目 ━━━━━━━━━━          ┃
┃It is also possible to add filters in the 【pattern.】                    ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

pattern にフィルターを追加することもできます。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    from pattern [every e=EventCreated(event.type = "myEventType")];
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：130行目 ━━━━━━━━━━          ┃
┃You can trigger by joining 【streams】                                    ┃
┃━━━━━━━━━━ 原文更新後(1)：130行目 ━━━━━━━━━━          ┃
┃You can trigger by joining 【streams.】                                   ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

ストリームを連結してトリガーをつくることができます。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    from EventCreated e unidirectional, AlarmCreated.std:lastevent() a
    where e.event.source = a.alarm.source;
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：140行目 ━━━━━━━━━━          ┃
┃You can also trigger by 【sequences】                                     ┃
┃━━━━━━━━━━ 原文更新後(1)：140行目 ━━━━━━━━━━          ┃
┃You can also trigger by 【sequences.】                                    ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

これはすべての EventCreated に対し、同一デバイスから AlarmCreated が通知された場合、トリガーとなります。

_Note: そのデバイスの最後の AlarmCreated ではなく、全体の最後の AlarmCreated で同一のデバイスからのものであった場合トリガーとなります。_



シーケンスをトリガーとすることもできます。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    from pattern[every (e=EventCreated -> a=AlarmCreated(alarm.source = e.event.source))];

これは AlarmCreated に続き、EventCreated が発生した場合にトリガーとなります。EventCreated が到着したときからスタートし、同一デバイスから AlarmCreated があった場合にトリガーとなります。その後、次の EventCreated を待つことになります。

### Timer

ストリームに対するトリガー文の他に、タイマーによるトリガーを行うこともできます。一定の間隔でトリガーをかけられます。

    from pattern [timer:interval(5 minutes)];
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：154行目 ━━━━━━━━━━          ┃
┃or as a cron 【job】                                                      ┃
┃━━━━━━━━━━ 原文更新後(1)：154行目 ━━━━━━━━━━          ┃
┃or as a cron 【job.】                                                     ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

oまたは cron ジョブとしても実行可能です。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    // timer:at(minutes, hours, daysOfMonth, month, daysOfWeek, (optional) seconds)
    // minutes: 0-59
    // hours: 0-23
    // daysOfMonth: 1-31
    // month: 1-12
    // daysOfWeek: 	0 (Sunday) - 6 (Saturday)
    // seconds: 0-59

    from pattern [timer:at(*, *, *, *, *)]; // trigger every minute
    from pattern [timer:at(*, *, *, *, *, *)]; // trigger every second
    from pattern [timer:at(*/10, *, *, *)]; // trigger every 10 minutes
    from pattern [timer:at(0, 1, *, *, [1,3,5])]; // trigger at 1am every monday, wednesday and friday
    from pattern [timer:at(0, */2, (1-7), *, *)]; // trigger every 2 hours on every day in the first week of every month
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：171行目 ━━━━━━━━━━          ┃
┃For example you can check if there was an event within a certain time     ┃
┃after another 【event】                                                   ┃
┃━━━━━━━━━━ 原文更新後(1)：171行目 ━━━━━━━━━━          ┃
┃For example you can check if there was an event within a certain time     ┃
┃after another 【event.】                                                  ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

タイマーパターンを別のタイマーパターンと組み合わせることもできます。例えば、別のイベントから所定時間内にイベントが発生したか確かめられます。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    from pattern [every e=EventCreated -> (timer:interval(10 minutes) and not a=AlarmCreated)];

これは、EventCreated が発生してから10分以内に AlarmCreated が発生しない場合にトリガーします。

### Outputs
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：179行目 ━━━━━━━━━━          ┃
┃Outputs give you the possibility to not take every event on a stream into ┃
┃account and to directly control 【whe】                                   ┃
┃━━━━━━━━━━ 原文更新後(1)：179行目 ━━━━━━━━━━          ┃
┃Outputs give you the possibility to not take every event on a stream into ┃
┃account and to directly control 【when a statement should output its      ┃
┃result.】                                                                 ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

output を利用すれば、アカウントのストリームにあるイベント全体を取得しなかったり、ダイレクトコントロールすることができます。10秒ごとにmeasurementをとり、それに対して計算したいような場合で、恐らく measurement 全部は不要で、あるサブセットに対して計算したい場合です。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    // will output the last measurement arrived every 1 minute
    from MeasurementCreated e
    where e.measurement.type = "c8y_TemperatureMeasurement"
    output last every 1 minutes;

    // will output the first of every 20 measurements arriving
    from MeasurementCreated e
    where e.measurement.type = "c8y_TemperatureMeasurement"
    output first every 20 events;

    // will output all 20 measurements after the 20th arrived
    from MeasurementCreated e
    where e.measurement.type = "c8y_TemperatureMeasurement"
    output every 20 events;

measurement の総和を計算し、新しい measurement に対して一々更新したくないなどの理由で、アカウントのすべての measurement を取得したい場合、

    select
        sum(getNumber(e, "myCustomMeasurement.mySeries.value")),
        last(*)
    from MeasurementCreated e
    where e.measurement.type = "myCustomMeasurement"
    output last every 50 events;

50の measurement ごとに、この文は(50のではなく、デプロイ後すべての measurementの)総和を出力します。

## イベントウィンドウ

イベントウィンドウにより、さらなる解析に利用できる、ストリームにある複数のイベントを束にすることができます。ウィンドウを生成するには主に2つの方法があります。

1. 決まった時間に対するウィンドウ

    select
      avg(getNumber(e, "myCustomMeasurement.mySeries.value")),
      last(*)
    from MeasurementCreated.win:time(1 hours) e
    where e.measurement.type = "myCustomMeasurement";

    select
      avg(getNumber(e, "myCustomMeasurement.mySeries.value")),
      last(*)
    from MeasurementCreated.win:time(1 hours) e
    where e.measurement.type = "myCustomMeasurement"
    output last every 1 hours;

2つの文の違いは、1つ目は MeasurementCreated のたびにトリガーし、１時間分の平均を出力します。2つ目の文は１時間ごとにしかトリガーせず、最後の平均(最後の MeasurementCreated を受け取ったときに算出)しか出力しません。


┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：232行目 ━━━━━━━━━━          ┃
┃2. Windows with a certain amount of 【events】                            ┃
┃━━━━━━━━━━ 原文更新後(1)：232行目 ━━━━━━━━━━          ┃
┃2. Windows with a certain amount of 【events:】                           ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫
2. 決まったイベント数に対するウィンドウ
┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛

    select
      avg(getNumber(e, "myCustomMeasurement.mySeries.value")),
      last(*)
    from MeasurementCreated.win:length(100) e
    where e.measurement.type = "myCustomMeasurement";

    select
      avg(getNumber(e, "myCustomMeasurement.mySeries.value")),
      last(*)
    from MeasurementCreated.win:length(100) e
    where e.measurement.type = "myCustomMeasurement"
    output last every 100 events;
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：247行目 ━━━━━━━━━━          ┃
┃Windows can also be globally 【declared】                                 ┃
┃━━━━━━━━━━ 原文更新後(1)：247行目 ━━━━━━━━━━          ┃
┃Windows can also be globally 【declared:】                                ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

ウィンドウは、グローバスに宣言することができます

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    create window MeasurementCreated.win:length(20) as MyMeasurementWindow;

    select
      avg(getNumber(e, "myCustomMeasurement.mySeries.value")),
      last(*)
    from MyMeasurementWindow e
    where e.measurement.type = "myCustomMeasurement";
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：257行目 ━━━━━━━━━━          ┃
┃Declaring a window gives you also the possibility of clearing the         ┃
┃【window】                                                                ┃
┃━━━━━━━━━━ 原文更新後(1)：257行目 ━━━━━━━━━━          ┃
┃Declaring a window gives you also the possibility of clearing the         ┃
┃【window.】                                                               ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

ウィンドウ宣言によってウィンドウをクリアすることもできます

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    on AlarmCreated delete from MyMeasurementWindow

## 独自のストリームを作成する

複雑なモジュールを１文で作成するのは簡単ではありません。Cumulocity では、イベントフローを制御することができる特定のフローを用意しています。ストリームを定義する必要はありません。未知のストリーム名を使うと、自動的にあなたが設定した入力で生成、定義されます。

    insert into MyEvent
    select
      e.event as e
    from EventCreated e;

    select e.type from MyEvent e;
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：273行目 ━━━━━━━━━━          ┃
┃If you now try to 【add】                                                 ┃
┃━━━━━━━━━━ 原文更新後(1)：273行目 ━━━━━━━━━━          ┃
┃If you now try to 【add:】                                                ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

では、次を追加してみましょう

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    insert into MyEvent
    select
      e as e
    from AlarmCreated e;
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：283行目 ━━━━━━━━━━          ┃
┃You can also explicitly create a new 【stream】                           ┃
┃━━━━━━━━━━ 原文更新後(1)：283行目 ━━━━━━━━━━          ┃
┃You can also explicitly create a new 【stream.】                          ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

この文はデプロイできません。それは、MyEvent はすでにEvent型の変数 e で宣言済みだからです。この文は、 AlarmCreated 型の値を e に設定しようとします。

明示的に新しいストリームを作成できます

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    create schema MyEvent(
      e Event
    );
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：289行目 ━━━━━━━━━━          ┃
┃The general syntax 【is】                                                 ┃
┃━━━━━━━━━━ 原文更新後(1)：289行目 ━━━━━━━━━━          ┃
┃The general syntax 【is:】                                                ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

一般的な構文は、

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    create schema StreamName(
      var1Name var1Type,
      var2Name var2Type,
      var3Name var3Type
    );

Javaプリミティブ型とインポートされた[[関数#Java関数|Javaライブラリ]] 、Cumulocity のデータ型(Event, Measurement, ManagedObject のような)や他のストリームを利用することができます。

    create schema TwoMyEvents(
      firstEvent MyEvent,
      secondEvent MyEvent
    );

_Note: ストリーム名はユニークであり、一度宣言したストリームは(明示的か暗黙的かによらず)あなたのすべてのモジュールで利用可能です_

## 独自の関数を作成する

総和や平均のようなものよりも複雑な関数を作りたい場合、独自の便利関数や表現を作成することができます。関数を記述するにあたり、記述言語として JavaScript を利用できます。importClass を使えば、あなたの表現に Java クラスを追加することもできます。

例：

与えられた重大度を上げる。(JavaScript利用)

    create expression CumulocitySeverities js:increaseSeverity(severity) [
    	importClass (com.cumulocity.model.event.CumulocitySeverities);
    	if(severity == CumulocitySeverities.WARNING) {
    		CumulocitySeverities.MINOR;
    	} else if(severity == CumulocitySeverities.MINOR) {
    		CumulocitySeverities.MAJOR;
    	} else if(severity == CumulocitySeverities.MAJOR) {
    		CumulocitySeverities.CRITICAL;
    	} else {
    		severity
    	}
    ];

２つの地理座標の間の距離を計算します。

    create expression distance(lat1, lon1, lat2, lon2) [
      var R = 6371000;
      var toRad = function(arg) {
        return arg * Math.PI / 180;
      };
      var lat1Rad = toRad(lat1);
      var lat2Rad = toRad(lat2);
      var deltaLatRad = toRad(lat2-lat1);
      var deltaLonRad = toRad(lon2-lon1);

      var a = Math.sin(deltaLatRad/2) * Math.sin(deltaLatRad/2) +
        Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(deltaLonRad/2) * Math.sin(deltaLonRad/2);

      var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

      var d = R * c;
      d;
    ];

## 変数
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：351行目 ━━━━━━━━━━          ┃
┃You can define variables in your 【modules】                              ┃
┃━━━━━━━━━━ 原文更新後(1)：351行目 ━━━━━━━━━━          ┃
┃You can define variables in your 【modules.】                             ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

モジュール内では変数を定義できます。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    create variable String myEmailText = "Hello World";
    create variable List supportedOperationsList = cast({"c8y_Restart", "c8y_Relay"}, java.util.List);

実行中に動的に値が変化する変数を定義できます。

    create variable String latestEventType;

    on EventCreated e set latestEventType = e.event.type;


## コンテキスト

コンテキストによって、定義された値によりイベントを操作、ソートできます。何らかの measurement についての計算を定義したい場合、普通はその measurement をもつすべてのデバイスに対して実行されるようにしたいでしょう。

ここに例をあげます。

    select
      avg(getNumber(e, "myCustomMeasurement.mySeries.value")),
      last(*)
    from MeasurementCreated.win:length(100) e
    where e.measurement.type = "myCustomMeasurement";

これは１つのデバイスに対しては完璧に機能します。しかしながら、２つのデバイスになった瞬間、平均計算は両方のデバイスにわたって計算されます。なぜなら、すべての measurement は MeasurementCreated 内で終了するからです。この文は、デバイスごとに measurement を区別することを意識していません。コンテキスト作成は、入力イベントをどこで切ればよいかの情報を文に教えることに似ています。

    create context DeviceAwareContext
      partition by measurement.source.value from MeasurementCreated;
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：385行目 ━━━━━━━━━━          ┃
┃Now we can add the context to the 【statement】                           ┃
┃━━━━━━━━━━ 原文更新後(1)：385行目 ━━━━━━━━━━          ┃
┃Now we can add the context to the 【statement:】                          ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

このコンテキスト定義では、MeasurementCreated ストリーム内でコンテキストキー(これによってイベントを切りたい)は measurement.source.value (デバイスのID)を見ればわかるということを宣言しています。

文にコンテキストを追加できるようになりましたね。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    context DeviceAwareContext
    select
      avg(getNumber(e, "myCustomMeasurement.mySeries.value")),
      last(*)
    from MeasurementCreated.win:length(100) e
    where e.measurement.type = "myCustomMeasurement";

これで平均は、各デバイスそれぞれで計算されるようになりました。

コンテキストは、コンテキストで宣言された入力をもつ文に対してのみ適用できます。コンテキストが意識することなる入力が必要な複数の文がある場合、コンテキスト内のそれぞれの入力とコンテキストキーがどこであるかを設定する必要があります

    create context DeviceAwareContext
      partition by
        measurement.source.value from MeasurementCreated,
        alarm.source.value from AlarmCreated,
        event.source.value from EventCreated,
        operation.deviceId.value from OperationCreated;
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：406行目 ━━━━━━━━━━          ┃
┃You can also create context keys of multiple 【values】                   ┃
┃━━━━━━━━━━ 原文更新後(1)：406行目 ━━━━━━━━━━          ┃
┃You can also create context keys of multiple 【values:】                  ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

複数の値にコンテキストキーを作成することもできます。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    create context DeviceAwareContext
      partition by measurement.source.value and measurement.type from MeasurementCreated;

このコンテキストは各デバイスの持つパーティションのみでなく、各measurement型に対して作成されます。
