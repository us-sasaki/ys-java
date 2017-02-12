---
order: 60
title: 研究：円形ジオフェンスアラーム
layout: default
---

## 概要
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：9行目 ━━━━━━━━━━            ┃
┃【The】 module 【shown below】 will give an in depth example 【of】 how   ┃
┃you can create more complex rules. It will use multiple of the features   ┃
┃explained 【】 in the other guide sections.                               ┃
┃━━━━━━━━━━ 原文更新後(1)：9行目 ━━━━━━━━━━            ┃
┃【This】 module 【】 will give an in depth example 【】 how you can       ┃
┃create more complex rules. It will use multiple of the features explained ┃
┃【before】 in the other guide sections.                                   ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

以下に示すモジュールは、もっと複雑なルールを定義する掘り下げた例です。ガイドの各章で説明した機能を組み合わせて使用します。Cumulocity イベント言語をこれから開始する方は[[例|こちらの例]]を見てみて下さい。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
## 前提条件

### 目標
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：16行目 ━━━━━━━━━━           ┃
┃We want our tracking devices that are continuously sending location       ┃
┃events to automatically generate alarms if they move outside 【of】 a     ┃
┃geofence.                                                                 ┃
┃━━━━━━━━━━ 原文更新後(1)：16行目 ━━━━━━━━━━           ┃
┃We want our tracking devices that are continuously sending location       ┃
┃events to automatically generate alarms if they move outside 【】 a       ┃
┃geofence.                                                                 ┃
┃━━━━━━━━━━ 原文更新前(2)：18行目 ━━━━━━━━━━           ┃
┃While it is moving outside it should not create new alarms because the    ┃
┃first one 【is still active and de-duplication would filter the newly     ┃
┃created alarms anyways.】                                                 ┃
┃As soon as the device moves back into the geofence the alarm 【can】 be   ┃
┃cleared.                                                                  ┃
┃━━━━━━━━━━ 原文更新後(2)：18行目 ━━━━━━━━━━           ┃
┃While it is moving outside it should not create new alarms because the    ┃
┃first one 【will keep active.】                                           ┃
┃As soon as the device moves back into the geofence the alarm 【will】 be  ┃
┃cleared.                                                                  ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

W継続的に位置イベントを送信する位置追跡デバイスに対し、ジオフェンスの外に出ると自動的にアラームを生成するようにしたいと思います。このジオフェンスは円とし、デバイスごとに別々に設定できるようにします。アラームは、ジオフェンスの外にデバイスが出た瞬間に生成することとします。最初のアラームが有効となり、新しく生成されたアラームを複製除去フィルターにかけるため、外に出ている間新しいアラームは生成しないようにします。ジオフェンス内に戻ったら、すぐにアラームをクリアします。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
### Cumulocity データモデル

Location イベント構造(必要な部分)：

    {
      "id": "...",
      "source": {
        "id": "...",
      },
      "text": "...",
      "time": "...",
      "type": "...",
      "c8y_Position": {
        "alt": ...,
        "lng": ...,
        "lat": ...
      }
    }

デバイス内に格納されるジオフェンス設定(半径はメーターで定義します)：

    {
      "c8y_Geofence": {
        "lat": ...,
        "lng": ...,
        "radius": ...
      }
    }

さらに設定全体を削除せずにデバイスごとにジオフェンスアラームを有効化/無効化できるようにしたいと思います。デバイスの c8y_SupportedOperations に、"c8y_Geofence" を追加/削除することで実現しましょう。

    {
      "c8y_SupportedOperations": [..., "c8y_Geofence", ...]
    }

### 計算

デバイスの現在地とジオフェンスの中心との距離が決められた半径より大きい場合にデバイスが外に出たことになります。２つの地理座標間の距離を計算できる関数が必要です。


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

上記の関数は、メータで距離を返却します。

## Step 1 入力をフィルターする
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：85行目 ━━━━━━━━━━           ┃
┃The main input for this module will be events. To discard 【not】         ┃
┃matching events as early as possible we will create a filter in one       ┃
┃statement that only 【matches】 events 【that contain a position.】       ┃
┃━━━━━━━━━━ 原文更新後(1)：85行目 ━━━━━━━━━━           ┃
┃The main input for this module will be events. To discard 【non-】        ┃
┃matching events as early as possible we will create a filter in one       ┃
┃statement that only 【matching】 events 【will pass.】                    ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

このモジュールの主な入力はイベントになります。なるべく早く関係ないイベントを捨てるため、位置情報を含むイベントのみにマッチする一文からなるフィルターをつくります。これらのイベントは新しいストリームに出力します。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    create schema LocationEvent(
      event Event
    );

    @Name('Location_event_filter')
    insert into LocationEvent
    select
      e.event as event
    from EventCreated e
    where getObject(e, "c8y_Position") is not null;

EventCreated の他の情報は不要なため、そのpayload(イベント)を次のストリームに渡します。

## Step 2 必要なデータを集める

次のステップでは、計算用のジオフェンス設定が欲しいため、それを取得します。イベントを見てそれを次のストリームに渡します。

    create schema LocationEventAndDevice (
    	event Event,
    	device ManagedObject
    );

    @Name('fetch_event_device')
    insert into LocationEventAndDevice
    select
    	e.event as event,
    	findManagedObjectById(event.source.value) as device
    from LocationEvent e;

## Step 3 デバイスが c8y_Geofence をサポートするか確認する

デバイスが利用可能であれば、デバイスにジオフェンス設定があるか、有効("c8y_Geofence" が c8y_SupportedOperations に含まれる)であるか確認できます。c8y_SupportedOperations配列を確認するには、デバイスからそれを取り出し、anyOf() 関数を使います。この関数はすべての要素を見て、どれかひとつでも要素の評価式が true になった場合 true を返します。設定のため、デバイスが fragment "c8y_Geofence" を含むかどうかのみを確認します。

    create schema LocationEventWithGeofenceConfig (
    	event Event,
    	eventLat Number,
    	eventLng Number,
    	centerLat Number,
    	centerLng Number,
    	maxDistance Number
    );

    @Name('parse_event_and_device_fragments')
    insert into LocationEventWithGeofenceConfig
    select
    	c.event as event,
      getNumber(e.event, "c8y_Position.lat") as eventLat,
      getNumber(e.event, "c8y_Position.lng") as eventLng,
      getNumber(e.device, "c8y_Geofence.lat") as centerLat,
      getNumber(e.device, "c8y_Geofence.lng") as centerLng,
      getNumber(e.device, "c8y_Geofence.radius") as maxDistance
    from LocationEventAndDevice e
    where  
    	getList(e.device, "c8y_SupportedOperations", new ArrayList()).anyOf(el => el = "c8y_Geofence") = true
    	and getObject(e.device, "c8y_Geofence") is not null;

## Step 4 トリガーを作成する
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：151行目 ━━━━━━━━━━          ┃
┃In the first step we calculate the distance with the function mentioned   ┃
┃【earlier】                                                               ┃
┃━━━━━━━━━━ 原文更新後(1)：151行目 ━━━━━━━━━━          ┃
┃In the first step we calculate the distance with the function mentioned   ┃
┃【earlier:】                                                              ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

前に述べたように、デバイスは、デバイスの現在位置とジオフェンスの中心の間の距離が設定されたジオフェンスの半径より大きい場合にフェンスの外にいることになります。アラームのトリガーのため、2つのイベントを必要とします。それは、デバイスがジオフェンスに入ったときのものと、ジオフェンスを離れたときのものです。
最初のステップとして、先ほど触れた関数で距離を計算します。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    create schema LocationEventWithDistance (
    	event Event,
    	maxDistance Number,
    	distance Number
    );

    @Name('calculate_current_distance')
    insert into LocationEventWithDistance
    select
    	e.event as event,
    	e.maxDistance as maxDistance,
    	cast(distance(centerLat, centerLng, eventLat, eventLng), java.lang.Number) as distance
    from LocationEventWithGeofenceConfig e;

次に最後の２イベントを保持するウィンドウを作成します。

    create schema LocationEventWithDistancePair (
    	firstPos LocationEventWithDistance,
    	secondPos LocationEventWithDistance
    );

    @Name('last_two_positions')
    insert into LocationEventWithDistancePair
    select
    	first(*) as firstPos,
    	last(*) as secondPos
    from LocationEventWithDistance.win:length(2);

これでストリーム LocationEventWithDistancePair にはアラームを生成すべきかどうかわかるすべてのデータを保持します。

## Step 5 アラームを作成する

アラームを生成するために、２つのイベントが必要です。１つは半径以下の距離となったもの、もう一つは半径以上の距離となったものです。これによって、デバイスがまさにジオフェンスを離れたことになります。

    @Name('create_geofence_alarm')
    insert into CreateAlarm
    select
    	pair.firstPos.event.source as source,
    	"ACTIVE" as status,
    	current_timestamp().toDate() as time,
    	"c8y_GeofenceAlarm" as type,
    	"MAJOR" as severity,
    	"Device moved out of circular geofence" as text
    from LocationEventWithDistancePair pair
    where pair.firstPos.distance.doubleValue() <= pair.firstPos.maxDistance.doubleValue()
    and pair.secondPos.distance.doubleValue() > pair.secondPos.maxDistance.doubleValue();

## Step 6 アラームをクリアする

アラームをクリアするには、最後の状態を切り替え、さらに、IDを得るため現在のアクティブアラームを掴む必要があります。

    @Name('clear_geofence_alarm')
    insert into UpdateAlarm
    select
        findFirstAlarmBySourceAndStatusAndType(pair.firstPos.event.source.value, "ACTIVE", "c8y_GeofenceAlarm").getId().getValue() as id,
        "Device moved back into circular geofence" as text,
        "CLEARED" as status
    from LocationEventWithDistancePair as pair
    where pair.firstPos.distance.doubleValue() > pair.firstPos.maxDistance.doubleValue()
    and pair.secondPos.distance.doubleValue() <= pair.secondPos.maxDistance.doubleValue();

## Step 7 デバイスコンテキストを作成する
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：218行目 ━━━━━━━━━━          ┃
┃Our rule is already working now but there is 【still】 one issue 【left.  ┃
┃So far we did not pay attention】 to 【what device】 send the location    ┃
┃event.                                                                    ┃
┃If 【】 device A sends a location event which is inside its geofence and  ┃
┃the 【next】 event is from 【】 device B which is outside the 【rule】    ┃
┃would create an alarm.                                                    ┃
┃The alarm would be generated for device A because when creating the alarm ┃
┃we 【take】 the source of the first arriving event as source for the      ┃
┃alarm creation.                                                           ┃
┃We need to configure that the window 【that】 holds the latest two events ┃
┃should only hold events of the same device.                               ┃
┃If there is an event from another device a new window should be created   ┃
┃so 【that in the end】 there is 【a separate】 window for 【every】       ┃
┃device.                                                                   ┃
┃━━━━━━━━━━ 原文更新後(1)：218行目 ━━━━━━━━━━          ┃
┃Our rule is already working now but there is 【】 one issue 【left:       ┃
┃where】 to 【】 send the location event.                                  ┃
┃If 【a】 device A sends a location event which is inside its geofence and ┃
┃the 【following】 event is from 【a】 device B which is outside the       ┃
┃【geofence it】 would create an alarm.                                    ┃
┃The alarm would be generated for device A because when creating the alarm ┃
┃we 【regard】 the source of the first arriving event as source for the    ┃
┃alarm creation.                                                           ┃
┃We need to configure that the window 【which】 holds the latest two       ┃
┃events should only hold events of the same device.                        ┃
┃If there is an event from another device a new window should be created   ┃
┃so 【】 there is 【one】 window for 【each】 device.                      ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

今回のロジックはこれで動きますが、まだもう一つやり残しがあります。今までロケーションイベントを送信したデバイスが何なのか注意していませんでした。
デバイスAがジオフェンスに入ったというロケーションイベントが発生し、次にデバイスBが外に出た場合でもこのロジックではアラームが発生します。アラーム生成時、最初に到着したイベントの source を アラーム生成の source とみなすことにより、デバイスAのアラームが生成されます。最後の２イベントを保持するウィンドウは、同一デバイスのイベントのみを保持するように設定しなければなりません。他のデバイスからのイベントがあったら、新しいウィンドウを生成すべきです。これにより、結局それぞれのデバイスに対して別々のウィンドウが存在することになります。
このことはコンテキストにより実現できます。ウィンドウを生成するところでコンテキストを生成すればよいです。コンテキストのパーティションは、デバイスごとにエンジンが別々のコンテキストを自動生成できるよう、デバイスIDとするのがよいでしょう。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    create context GeofenceDeviceContext
	   partition by event.source.value from LocationEventWithDistance;
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃
┃━━━━━━━━━━ 原文更新前(1)：230行目 ━━━━━━━━━━          ┃
┃Now we can add the context to the statement where we create the window. A ┃
┃context can only 【】 applied to statements where the input of the        ┃
┃statement is configured in the context.                                   ┃
┃Otherwise the engine would not know which value to take 【to create】     ┃
┃context partitions.                                                       ┃
┃━━━━━━━━━━ 原文更新後(1)：230行目 ━━━━━━━━━━          ┃
┃Now we can add the context to the statement where we create the window. A ┃
┃context can only 【be】 applied to statements where the input of the      ┃
┃statement is configured in the context.                                   ┃
┃Otherwise the engine would not know which value to take 【for creating】  ┃
┃context partitions.                                                       ┃
┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫

これでウィンドウを作成する文にコンテキストを追加できます。コンテキストは、文の入力がそのコンテキストで設定されている文にのみ適用できます。そうしないと、エンジンはどの値をコンテキストパーティション生成に使えばいいかわからなくなってしまいます。

┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    @Name('last_two_positions')
    context GeofenceDeviceContext
    insert into LocationEventWithDistancePair
    select
      first(*) as firstPos,
      last(*) as secondPos
    from LocationEventWithDistance.win:length(2);

## すべて結合

ここまでで、すべての部品を１モジュールにまとめることができます。文の順序は構いません。唯一例外は、カスタムモデル(スキーマ、関数、コンテキスト、変数、...)を使うときは、それらを使う前に宣言しなければならないことです。

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

    create schema LocationEvent(
      event Event
    );

    create schema LocationEventAndDevice (
      event Event,
      device ManagedObject
    );

    create schema LocationEventWithGeofenceConfig (
      event Event,
      eventLat Number,
      eventLng Number,
      centerLat Number,
      centerLng Number,
      maxDistance Number
    );

    create schema LocationEventWithDistance (
      event Event,
      maxDistance Number,
      distance Number
    );

    create schema LocationEventWithDistancePair (
      firstPos LocationEventWithDistance,
      secondPos LocationEventWithDistance
    );

    create context GeofenceDeviceContext
     partition by event.source.value from LocationEventWithDistance;

    @Name('Location_event_filter')
    insert into LocationEvent
    select
      e.event as event
    from EventCreated e
    where getObject(e, "c8y_Position") is not null;

    @Name('fetch_event_device')
    insert into LocationEventAndDevice
    select
      e.event as event,
      findManagedObjectById(event.source.value) as device
    from LocationEvent e;

    @Name('parse_event_and_device_fragments')
    insert into LocationEventWithGeofenceConfig
    select
      c.event as event,
      getNumber(e.event, "c8y_Position.lat") as eventLat,
      getNumber(e.event, "c8y_Position.lng") as eventLng,
      getNumber(e.device, "c8y_Geofence.lat") as centerLat,
      getNumber(e.device, "c8y_Geofence.lng") as centerLng,
      getNumber(e.device, "c8y_Geofence.radius") as maxDistance
    from LocationEventAndDevice e
    where  
      getList(e.device, "c8y_SupportedOperations", new ArrayList()).anyOf(el => el = "c8y_Geofence") = true
      and getObject(e.device, "c8y_Geofence") is not null;

    @Name('calculate_current_distance')
    insert into LocationEventWithDistance
    select
      e.event as event,
      e.maxDistance as maxDistance,
      cast(distance(centerLat, centerLng, eventLat, eventLng), java.lang.Number) as distance
    from LocationEventWithGeofenceConfig e;

    @Name('last_two_positions')
    context GeofenceDeviceContext
    insert into LocationEventWithDistancePair
    select
      first(*) as firstPos,
      last(*) as secondPos
    from LocationEventWithDistance.win:length(2);

    @Name('create_geofence_alarm')
    insert into CreateAlarm
    select
      pair.firstPos.event.source as source,
      "ACTIVE" as status,
      current_timestamp().toDate() as time,
      "c8y_GeofenceAlarm" as type,
      "MAJOR" as severity,
      "Device moved out of circular geofence" as text
    from LocationEventWithDistancePair pair
    where pair.firstPos.distance.doubleValue() <= pair.firstPos.maxDistance.doubleValue()
    and pair.secondPos.distance.doubleValue() > pair.secondPos.maxDistance.doubleValue();

    @Name('clear_geofence_alarm')
    insert into UpdateAlarm
    select
        findFirstAlarmBySourceAndStatusAndType(pair.firstPos.event.source.value, "ACTIVE", "c8y_GeofenceAlarm").getId().getValue() as id,
        "Device moved back into circular geofence" as text,
        "CLEARED" as status
    from LocationEventWithDistancePair as pair
    where pair.firstPos.distance.doubleValue() > pair.firstPos.maxDistance.doubleValue()
    and pair.secondPos.distance.doubleValue() <= pair.secondPos.maxDistance.doubleValue();
