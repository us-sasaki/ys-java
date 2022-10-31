---
order: 50
title: Examples
layout: default
toc: true
---

## measurement の１時間ごとの平均の計算

入力データが以下のようであったとします。

    {
      "c8y_TemperatureMeasurement": {
        "T": {
          "value": ...,
          "unit": "C"
        }
      },
      "time":"...",
      "source": {
        "id":"..."
      },
      "type": "c8y_TemperatureMeasurement"
    }

平均を生成するため、モジュールに次のステップが必要です。

 - デバイスごとに measurement を分けるコンテキスト
 - １時間のウィンドウ
 - １時間ごとの平均計算の最終結果のみを返却する出力
 - すべて新しい measurement として生成

モジュール


    create context HourlyAvgMeasurementDeviceContext
      partition measurement.source.value from MeasurementCreated;

    @Name("Creating_hourly_measurement")
    context HourlyAvgMeasurementDeviceContext
    insert into CreateMeasurement
    select
      m.measurement.source as source,
      current_timestamp().toDate() as time,
      "c8y_AverageTemperatureMeasurement" as type,
      {
        "c8y_AverageTemperatureMeasurement.T.value", avg(cast(getNumber(m, "c8y_TemperatureMeasurement.T.value"), double)),
        "c8y_AverageTemperatureMeasurement.T.unit", getString(m, "c8y_TemperatureMeasurement.T.unit")
      } as fragments
    from MeasurementCreated.win:time(1 hours) m
    where getObject(m, "c8y_TemperatureMeasurement") is not null
    output last every 1 hours;

## オペレーションが実行されなかったらアラームを生成

デバイスでオペレーションが処理される場合、決まったシーケンスに従います。

 - PENDING (生成後)
 - EXECUTING (オペレーションをデバイスが受け取り、処理を開始)
 - SUCCESSFUL または FAILED (実行結果によります)

所定時間内にオペレーションが SUCCESSFUL や FAILED にならない場合、通常問題があることを示します(例えばデバイスとのコネクションが切れたり、実行中に固まったり)。オペレーションが正常に処理されなかった場合でも、デバイスはオペレーションを FAILED に更新すべきです。この例では10分を処理の許容実行時間として使用しています。次の順であることを確認します。

 - OperationCreated
 - 同一オペレーションに対し、10分以内に SUCCESSFUL または FAILED を設定する OperationUpdated

２つ目の部分には新しいアラーム生成があらわれません。

    @Name("handle_not_finished_operation")
    insert into CreateAlarm  
    select
        o.operation.deviceId as source,
        CumulocitySeverities.MAJOR as severity,
        CumulocityAlarmStatuses.ACTIVE as status,
        "c8y_OperationNotFinishedAlarm" as type,
        current_timestamp().toDate() as time,
        replaceAllPlaceholders("The device has not finished the operation #{id} within 10 minutes", o.operation) as text
    from pattern [
        every o = OperationCreated
        	-> (timer:interval(10 minutes)
        	and not OperationUpdated(
        		operation.id.value = o.operation.id.value
        		and (operation.status in (OperationStatus.SUCCESSFUL, OperationStatus.FAILED))
        	))
    ];

## ビット値の measurement からアラームを生成

デバイスはしばしばレジスタでアラーム状態を保持しており、レジスタ値の意味に関する情報を保持していないことがあります。この例では、デバイスがビット値を含むレジスタ全体を送信することを想定し、ビット値に対応する各アラームを生成します。

バイナリ値によってアラームテキスト、型、重大度の３つの表現を生成します。

    create expression String getFaultRegisterAlarmType(position) [
        switch (position) {
            case 0:
              "c8y_HighTemperatureAlarm";
              break;
            case 1:
              "c8y_ProcessingAlarm";
              break;
            case 2:
              "c8y_DoorOpenAlarm";
              break;
            case 3:
              "c8y_SystemFailureAlarm";
              break;
            default:
              "c8y_FaultRegister" + position + "Alarm";
              break;
        };
    ];

    create expression CumulocitySeverities getFaultRegisterAlarmSeverity(position) [
        importClass(com.cumulocity.model.event.CumulocitySeverities);
        switch (position) {
            case 0:
              CumulocitySeverities.MAJOR;
              break;
            case 1:
              CumulocitySeverities.WARNING;
              break;
            case 2:
              CumulocitySeverities.MINOR;
              break;
            case 3:
              CumulocitySeverities.CRITICAL;
              break;
            default:
              CumulocitySeverities.MAJOR;
              break;
        };
    ];

    create expression String getFaultRegisterAlarmText(position)[
        switch(position) {
            case 0:
              "The machine temperature reached a critical status";
              break;
            case 1:
              "There was an error trying to process data";
              break;
            case 2:
              "Door was opened";
              break;
            case 3:
              "There was a critical system failure";
              break;
            default:
              "An undefined alarm was reported on position " || position || " in the binary fault register";
              break;
        };
    ];


ビット値の measurement を解析するには、文字列化してそれぞれの文字を操作します。getActiveBits() 関数はその処理を行い、measurement のビットが "1" であるビット位置のリストを返却します。処理結果は List ではなく、Listに代わる BitPositionスキーマに適合する map 構造となります。これにより、ストリームであるかのように扱うことができます。これにより、ストリームへの join や、measurement に対するアラームのみではなく、リストにあるあらゆる要素を join した measurement に対してトリガできます。

    create schema BitPosition(
      position int
    );

    create schema MeasurementWithBinaryFaultRegister(
      measurement Measurement,
      faultRegister String
    );

    create expression Collection getActiveBits(value) [
    	importPackage(java.util);
    	var bitOnNumbers = new ArrayList();
            var size = value.length;
    	for(var no = 0; no < size; no++) {
    	    if(value.charAt(no) == "1") {
    		bitOnNumbers.add(Collections.singletonMap('position', size - no - 1));
                }
    	}
    	bitOnNumbers;
    ];

    @Name("extract_fault_register")
    insert into MeasurementWithBinaryFaultRegister
    select
      m.measurement as measurement,
      getString(m, "c8y_BinaryFaultRegister.errors.value") as faultRegister
    from MeasurementCreated m
    where getObject(m, "c8y_BinaryFaultRegister") is not null;

    @Name("creating_alarm")
    insert into CreateAlarm
    select
    	m.measurement.source as source,
            getFaultRegisterAlarmSeverity(bit.position) as severity,
            CumulocityAlarmStatuses.ACTIVE as status,
    	m.measurement.time as time,
    	getFaultRegisterAlarmType(bit.position) as type,
    	getFaultRegisterAlarmText(bit.position) as text
    from
    	MeasurementWithBinaryFaultRegister m unidirectional,
    	MeasurementWithBinaryFaultRegister[getActiveBits(faultRegister)@type(BitPosition)] as bit;

measurement は以下のように生成します。

    {
    	"c8y_BinaryFaultRegister": {
      	"errors": {
        	"value": 10110
        }
      },
      "time":"...",
      "source": {
      	"id":"..."
      },
      "type": "c8y_BinaryFaultRegister"
    }

最後の文は３回トリガーします。

 - bit位置 1 の measurement
 - bit位置 2 の measurement
 - bit位置 4 の measurement

したがって、３回アラームを生成します。


## 消費に関する measurement

何かの現在の水位を測るセンサーがあるとしましょう。このセンサーは Cumulocity に標準的な基準値を送信するとします。この場合、消費量を簡単に作成することができます。２つの measurement の差の計算は有用ですが、measurement が常に一定の間隔で送られる場合のみわかりやすい意味を持ちます。時間間隔の差にしたがう、１時間あたりの絶対的な差を計算しましょう。
あるデバイスに対する隣り合った２つの measurement の値と時間の差を比較します(コンテキストを必要とします)。

    create schema FillLevelMeasurement(
      measurement Measurement,
      value double
    );

    create schema AdjacentFillLevelMeasurements(
    	firstValue double,
    	lastValue double,
    	firstTime Date,
    	lastTime Date,
    	source String
    );

    create context ConsumptionMeasurementDeviceContext
          partition measurement.source.value from FillLevelMeasurement;

    create expression double calculateConsumption(firstValue, lastValue, firstTime, lastTime) [
      if (lastTime == firstTime) {
        0;
      } else {
        ((firstValue - lastValue) * 3600000) / (lastTime - firstTime);
      }
    ];

    @Name("filter_fill_level_measurements")
    insert into FillLevelMeasurement
    select
      m.measurement as measurement,
      cast(getNumber(m, "c8y_WaterTankFillLevel.level.value"), double) as value
    from MeasurementCreated m
    where getObject(m, "c8y_WaterTankFillLevel") is not null;

    @Name("combine_two_latest_measurements")
    context ConsumptionMeasurementDeviceContext
    insert into AdjacentFillLevelMeasurements
    select
      first(m.value) as firstValue,
      first(m.measurement.time) as firstTime,
      last(m.value) as lastValue,
      last(m.measurement.time) as lastTime,
      context.key1 as source
    from FillLevelMeasurement.win:length(2) m;

    @Name("create_consumption_measurement")
    insert into CreateMeasurement
    select
      m.lastTime as time,
      m.source as source,
      "c8y_HourlyWaterConsumption" as type,
      {
        "c8y_HourlyWaterConsumption.consumption.value", calculateConsumption(m.firstValue, m.lastValue, m.firstTime.toMillisec(), m.lastTime.toMillisec()),
        "c8y_HourlyWaterConsumption.consumption.unit", "l/h"
      } as fragments
    from AdjacentFillLevelMeasurements m;
