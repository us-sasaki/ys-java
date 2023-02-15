import { 
  IAlarm,
  IEvent,
  IMeasurement,
  IManagedObject,
  FetchClient,
  Realtime
} from "@c8y/client";
import { Subject } from 'rxjs';

import { ThingsCloudClient } from "./thingsCloudClient";

export class ThingsCloudDataSupplier {
  private thingsCloudClient: ThingsCloudClient;
  private realtimeSubscription: any[] = [];

  /**
   * ThingsCloudDataSupplierクラスのコンストラクタです。
   * 各メソッドでThingsCloudの対象テナントからデータを取得し、そのデータをフラット化して返却します。
   * 
   * @class
   * 
   * @param {baseUrl} baseUrl 情報を取得するテナントのURL
   * @param {user} user 情報を取得するための認証情報(ユーザ名)
   * @param {password} password 情報を取得するための認証情報(パスワード)
   * 
   * @desc 
   * *フラット化について*
   * このクラスでは、各メソッドで結果がフラット化され返却されます。例えば、データを取得した結果が
   * ```json
   * [
   *   {
   *     "c8y_Temperature": {
   *       "T": {
   *         "value": 18.0,
   *         "unit": "C"
   *       }
   *     }
   *   },
   *   :
   * ]
   * ```
   * のような JSON 構造だった場合、
   * ```json
   * [
   *   {
   *     "c8y_Temperature_T_value": 18.0,
   *     "c8y_Temperature_T_unit": "C"
   *   },
   *   :
   * ]
   * ```
   * のような形式の変換がなされます。
   * このようなデータ形式の変換操作をフラット化と呼びます。
   */
  constructor(private fetchClient: FetchClient) {
    this.thingsCloudClient = new ThingsCloudClient(fetchClient);
  }

  /**
   * deviceId, measurementsFilterで指定されたデバイスのMeasurement情報を
   * 取得し、フラット化された(階層的なオブジェクト構造があった場合、ルートに設定した)
   * オブジェクトの配列として返却します。
   * 
   * @param {string} deviceId deviceId or groupId (groupId を指定した場合、再帰的に子のデバイスが検索され、対象となります。)
   * @param {object} measurementsFilter Measurement のフィルタ条件を指定する情報
   * 
   * @returns Measurement配列で解決する Promise
   * @example
   * ```typescript
   * async () => {
   *   :
   *   // thingsCloudDataSupplier にインスタンスを格納
   *   :
   *   const measurements = await thingsCloudDataSupplier.getMeasurementsArray('123456', {
   *     type: 'c8y_TemperatureMeasurement',
   *     dateFrom: '2022-02-01T00:00:00.000+09:00',
   *     dateTo: '2022-02-28T23:59:59.999+09:00'
   *   });
   * }
   * ```
   */
  async getMeasurementsArray(
    deviceId: string, 
    measurementsFilter: any
  ): Promise<any[]> {
    measurementsFilter = { ...measurementsFilter };
    const flattenOption = measurementsFilter.flatten;
    delete measurementsFilter.flatten;

    let deviceIds = await this.thingsCloudClient.getSubAssets(deviceId);
    let measurements: IMeasurement[] = [];
    const promises = deviceIds.map(async (deviceId) => {
      return (await this.thingsCloudClient.getMeasurements({deviceId: deviceId, filter: measurementsFilter})).data;
    });
    const measurementsList = await Promise.all(promises);
    measurementsList.forEach(result => {
      measurements = measurements.concat(result);
    });
    if (flattenOption || flattenOption === void 0) measurements = this.flatten(measurements);
    measurements.forEach(f => f.time = new Date(f.time));
    return measurements;
  }

  /**
   * deviceId, alarmsFilterで指定されたデバイスのAlarm情報を
   * 取得し、フラット化された(階層的なオブジェクト構造があった場合、ルートに設定した)
   * オブジェクトの配列として返却します。
   * 
   * @param {string} deviceId deviceId or groupId (groupId を指定した場合、再帰的に子のデバイスが検索され、対象となります。)
   * @param {object} alarmsFilter Alarm のフィルタ条件を指定する情報
   * 
   * @returns Alarm配列で解決する Promise
   * @example
   * ```typescript
   * async () => {
   *   :
   *   // thingsCloudDataSupplier にインスタンスを格納
   *   :
   *   const alarms = await thingsCloudDataSupplier.getAlarmsArray('123456', {
   *     type: 'c8y_UnavailabilityAlarm',
   *     dateFrom: '2022-02-01T00:00:00.000+09:00',
   *     dateTo: '2022-02-28T23:59:59.999+09:00',
   *     status: 'CLEARED'
   *   });
   * }
   * ```
   */
   async getAlarmsArray(deviceId: string, alarmsFilter: any): Promise<any[]> {
    alarmsFilter = { ...alarmsFilter };
    const flattenOption = alarmsFilter.flatten;
    delete alarmsFilter.flatten;
    let deviceIds = await this.thingsCloudClient.getSubAssets(deviceId);
    let alarms: IAlarm[] = [];
    const promises = deviceIds.map(async (deviceId) => {
      return (await this.thingsCloudClient.getAlarms({ deviceId: deviceId, filter: alarmsFilter})).data;
    });
    const alarmsList = await Promise.all(promises);
    alarmsList.forEach(result => {
      alarms = alarms.concat(result);
    });
    
    if (flattenOption || flattenOption === void 0) alarms = this.flatten(alarms);
    // alarms.forEach(f => f.time = new Date(f.time)); IAlarm の time は string 
    
    return alarms;
  }

  /**
   * deviceId, eventsFilterで指定されたデバイスのEvent情報を
   * 取得し、フラット化された(階層的なオブジェクト構造があった場合、ルートに設定した)
   * オブジェクトの配列として返却します。
   * 
   * @param {string} deviceId deviceId or groupId (groupId を指定した場合、再帰的に子のデバイスが検索され、対象となります。)
   * @param {object} eventsFilter Event のフィルタ条件を指定する情報
   * 
   * @returns Event配列で解決する Promise
   * @example
   * ```typescript
   * async () => {
   *   :
   *   // thingsCloudDataSupplier にインスタンスを格納
   *   :
   *   const events = await thingsCloudDataSupplier.getEventsArray('123456', {
   *     type: 'c8y_UpdateEvent',
   *     dateFrom: '2022-02-01T00:00:00.000+09:00',
   *     dateTo: '2022-02-28T23:59:59.999+09:00'
   *   });
   * }
   * ```
   */
  async getEventsArray(deviceId: string, eventsFilter: any): Promise<any[]> {
    eventsFilter = { ...eventsFilter };
    const flattenOption = eventsFilter.flatten;
    delete eventsFilter.flatten;
    let deviceIds = await this.thingsCloudClient.getSubAssets(deviceId);
    let events: IEvent[] = [];
    const promises = deviceIds.map(async (deviceId) => {
      return (await this.thingsCloudClient.getEvents({ deviceId: deviceId, filter: eventsFilter})).data;
    });
    const eventsList = await Promise.all(promises);
    eventsList.forEach(result => {
      events = events.concat(result);
    });
    if (flattenOption || flattenOption === void 0) events = this.flatten(events);
    // events.forEach(f => f.time = new Date(f.time)); IEvent の time は string
    return events;

  }


  /**
   * deviceIdで指定されたデバイスのMOリストをオブジェクトの配列として返却します
   * 
   * @param {string} deviceId deviceId or groupId (groupId を指定した場合、再帰的に子のデバイスが検索され、対象となります。)
   * 
   * @returns IManagedObject配列で解決する Promise
   * @example
   * ```typescript
   * 
   * ```
   */
  async getDevicesArray(deviceId: string): Promise<any[]> {
    return await this.thingsCloudClient.getSubAssetMOs(deviceId);
  }

  /**
   * 対象のオブジェクトを監視し、メジャーメントのリアルタイム通知を受け取ります
   * 
   * @param {string} target 監視対象オブジェクト deviceId or '*' (テナント内全てのデバイスが対象)
   * 
   * @returns リアルタイム通知で受け取ったメジャーメント
   * @example
   * ```typescript
   *    thingsCloudDataSupplier.getRealtimeMeasurement('12345')
   *      .subscribe(notifiedData => {
              updateData();
   *      })
   * ```
   */
  getRealtimeMeasurement(target: string): Subject<IMeasurement> {
    const notified: Subject<IMeasurement> = new Subject();
    const subscription = this.thingsCloudClient.getRealtime().subscribe(`/measurements/${target}`, (notifiedData) => {
      notified.next(notifiedData.data.data);
    });
    this.realtimeSubscription.push(subscription);

    return notified;
  }  

  /**
   * 対象のオブジェクトを監視し、アラームのリアルタイム通知を受け取ります
   * 
   * @param {string} target 監視対象オブジェクト deviceId or '*' (テナント内全てのデバイスが対象) 
   * 
   * @returns リアルタイム通知で受け取ったアラーム
   * @example
   * ```typescript
   *    thingsCloudDataSupplier.getRealtimeAlarm('12345')
   *      .subscribe(notifiedData => {
   *         updateData();
   *       })
   *  ```
   */
  getRealtimeAlarm(target: string): Subject<IAlarm> {
    const notified: Subject<IAlarm> = new Subject();  
    const subscription = this.thingsCloudClient.getRealtime().subscribe(`/alarms/${target}`, (notifiedData) => {
      notified.next(notifiedData.data.data);
    });
    this.realtimeSubscription.push(subscription);
    
    return notified;
  }  

  /**
   * 対象のオブジェクトを監視し、イベントのリアルタイム通知を受け取ります
   * 
   * @param {string} target 監視対象オブジェクト deviceId or '*' (テナント内全てのデバイスが対象)
   * 
   * @returns リアルタイム通知で受け取ったイベント
   * @example
   * ```typescript
   *    thingsCloudDataSupplier.getRealtimeEvent('12345')
   *      .subscribe(notifiedData => {
   *        updateData();
   *      })
   * ```
   */
   getRealtimeEvent(target: string): Subject<IEvent> {
    const notified: Subject<IEvent> = new Subject();
    const subscription = this.thingsCloudClient.getRealtime().subscribe(`/events/${target}`, (notifiedData) => {
      notified.next(notifiedData.data.data);
    });
    this.realtimeSubscription.push(subscription);

    return notified;
  }


  /**
   * 対象のオブジェクトを監視し、マネージドオブジェクトのリアルタイム通知を受け取ります
   * 
   * @param {string} target 監視対象オブジェクト deviceId or '*' (テナント内全てのデバイスが対象)
   * 
   * @returns リアルタイム通知で受け取ったマネージドオブジェクト
   * @example
   * ```typescript
   *    thingsCloudDataSupplier.getRealtimeManagedObject('12345')
   *      .subscribe(notifiedData => {
              updateData();
   *      })
   * ```
   */
  getRealtimeManagedObject(target: string): Subject<IManagedObject> {
    const notified: Subject<IManagedObject> = new Subject();
    const subscription = this.thingsCloudClient.getRealtime().subscribe(`/managedobjects/${target}`, (notifiedData) => {
      notified.next(notifiedData.data.data);
    });
    this.realtimeSubscription.push(subscription);

    return notified;
  }  

  /**
   * リアルタイム通知のチャネル監視を解除します
   */
  unsubscribeRealtime(): void {
    this.realtimeSubscription.forEach(subscription => {
      this.thingsCloudClient.unsubscribeRealtime(subscription);
    });
    this.realtimeSubscription = [];
  }

  /**
   * 指定された object の配列に関し、各 object の階層を平坦化し 1 層に変換します。
   * 例： [{a: 1, b: {c: 5}}] ---> [{a: 1, b_c: 5}]
   * object 値に配列を含む場合、配列内の object は平坦化されないことに注意して下さい。
   * 例： [{a: 1, b: [{c: 5}]}] ---> [{a: 1, b: [{c: 5}]}] (変更なし)
   * 
   * @param objectArray object の配列
   * @returns  各要素が平坦化(つなぎ文字は _)された object の配列(Plot で利用可能な形式)
   * @example
   * ```typescript
   * :
   * // thingsCloudDataSupplier にインスタンスを格納
   * :
   * const array = [{a: 1, b: {c: 5}}];
   * const result = thingsCloudDataSupplier.flatten(array);
   * console.log(result); // expected: [{a: 1, b_c: 5}]
   * ```
   */
  private flatten(objectArray: any[]): any[] {
    objectArray.forEach( obj => this.flattenImpl(obj, '', obj) );
    return objectArray;
  }

  /**
   * flatten の実処理を行います。parent のデータが平坦化され、上書きされます。
   * 
   * @param parent flatten 先のデータを格納する object
   * @param parentKey 階層化されたキー情報を連結したもの
   * @param obj flatten 対象となる object (再帰により子オブジェクトが指定される)
   * @example
   * ```typescript
   * :
   * // thingsCloudDataSupplier にインスタンスを格納
   * :
   * const obj = {a: 1, b: {c: 5}};
   * const result = thingsCloudDataSupplier._flatten(obj, '', obj);
   * console.log(result); // expected: {a: 1, b_c: 5}
   * ```
   */
  private flattenImpl(parent: any, parentKey: string, obj: any): void {
    const keys = Object.keys(obj);
    if (keys.length == 0) return;
    if (parentKey) delete parent[parentKey];
    keys.forEach( key => {
      const v = obj[key];
      const ckey = parentKey + ((parentKey !== '')?'_':'') + key;
      if ( v !== null && typeof v === 'object' && !Array.isArray(v)) {
        this.flattenImpl(parent, ckey, v);
      } else {
        if (parent !== obj) {
          parent[ckey] = v;
        }
      }
    });
  }
}
