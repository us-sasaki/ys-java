import { 
  IAlarm,
  IEvent,
  IIdentified,
  IManagedObject, 
  IMeasurement,
  Severity,
  AlarmStatus,
  FetchClient,
 } from "@c8y/client";
import { Subject } from "rxjs";

import { IEPLFile, ThingsCloudClient } from './thingsCloudClient';
import { ThingsCloudDataSupplier } from "./thingsCloudDataSupplier";

/**
 * Things Cloud テストに有用なユーティリティ関数
 */
export class ThingsCloudTestingUtil {
  private thingsCloudClient: ThingsCloudClient;
  private thingsCloudDataSupplier: ThingsCloudDataSupplier;
  constructor(private fetchClient: FetchClient) {
    this.thingsCloudClient = new ThingsCloudClient(fetchClient);
    this.thingsCloudDataSupplier = new ThingsCloudDataSupplier(fetchClient);
  }

  private uniqueCount = Date.now();

  /**
   * 指定時間待機します。
   * @param milliSec 待機時間
   * @returns 
   */
  async sleep(milliSec: number): Promise<void> {
    return new Promise( (resolve) => { setTimeout( resolve, milliSec); } );
  }

  /**
   * 12 byteのユニークな16進数文字列(左0埋め24文字)を生成します。
   * テスト用デバイスの identifier など、他テストと干渉しないようにする
   * 目的で利用します。
   */
  testingUnique(): string {
    const now = Date.now();
    return ("00000000000"+(this.uniqueCount++).toString(16)).slice(-12)
     + ("00000000000" + now.toString(16)).slice(-12);
  }

  /**
   * 指定された Promise のステータスを検査します。
   * @param promise 検査対象の promise
   * @returns "pending", "fulfilled", "rejected" のいずれかで解決する Promise
   */
  async getPromiseState(promise: Promise<any>): Promise<"pending" | "fulfilled" | "rejected"> {
    return new Promise( resolve => {
      const uniqueValue = /unique/;

      function pendingOrResolved(value) {
        resolve( (value === uniqueValue)? 'pending':'fulfilled' );
      }

      const race = [promise, Promise.resolve(uniqueValue)]
      Promise.race(race).then(pendingOrResolved, () => resolve('rejected'));
    });
  }

  async getDevices(deviceOrGroupId: string): Promise<IManagedObject[]> {
    return this.thingsCloudClient.getSubAssetMOs(deviceOrGroupId);
  }

  async createDevice(managedObject: Partial<IManagedObject>): Promise<IManagedObject> {
    managedObject.testingUnique = this.testingUnique();
    managedObject.type = managedObject.type || 'com_TCTestingUtilDevice';
    managedObject.c8y_IsDevice = managedObject.c8y_IsDevice || {};
    return this.thingsCloudClient.postManagedObject(managedObject);
  }

  async deleteDevice(id: string): Promise<void> {
    return this.thingsCloudClient.deleteManagedObject(id);
  }

  async postMeasurement(deviceId: string, type: string, fragments?: any, time?: Date): Promise<IMeasurement> {
    const m = await this.thingsCloudClient.postMeasurement(deviceId, type, fragments, time);
    return m;
  }

  async postEvent(deviceId: string, type: string, text: string, time?: Date, fragments?: any): Promise<IEvent> {
    return this.thingsCloudClient.postEvent(deviceId, type, text, time, fragments);
  }

  async postAlarm(deviceId: string, type: string, text: string, severity: Severity, status: AlarmStatus, time?: Date, fragments?: any): Promise<IAlarm> {
    return this.thingsCloudClient.postAlarm(deviceId, type, text, severity, status, time, fragments);
  }

  async putAlarmStatus(alarmId: string, status: AlarmStatus): Promise<IAlarm> {
    return this.thingsCloudClient.putAlarmStatus(alarmId, status);
  }

  async putAlarmSeverity(alarmId: string, severity: Severity): Promise<IAlarm> {
    return this.thingsCloudClient.putAlarmSeverity(alarmId, severity);
  }

  async putAlarm(alarmId: string, updater: any): Promise<IAlarm> {
    return this.thingsCloudClient.putAlarm(alarmId, updater);
  }

  /**
   * デバイスからの Measurement を監視、記録する Watcher を作成します。
   * @param deviceId 監視対象のデバイス
   * @returns デバイスからの Measurement を監視、記録する Watcher
   */
  async createMeasurementWatcher(deviceId: string): Promise<Watcher<IMeasurement>> {
    const watcher = new Watcher<IMeasurement>(this.thingsCloudDataSupplier.getRealtimeMeasurement.bind(this.thingsCloudDataSupplier)(deviceId));
    await this.sleep(1000); // 実際に起動するのに時間がかかる
    return watcher;
  }

  /**
   * デバイスからの Event を監視、記録する Watcher を作成します。
   * @param deviceId 監視対象のデバイス
   * @returns デバイスからの Event を監視、記録する Watcher
   */
   async createEventWatcher(deviceId: string): Promise<Watcher<IEvent>> {
    const watcher = new Watcher<IEvent>(this.thingsCloudDataSupplier.getRealtimeEvent.bind(this.thingsCloudDataSupplier)(deviceId));
    await this.sleep(1000);
    return watcher;
  }

  /**
   * デバイスからの Event を監視、記録する Watcher を作成します。
   * @param deviceId 監視対象のデバイス
   * @returns デバイスからの Event を監視、記録する Watcher
   */
   async createAlarmWatcher(deviceId: string): Promise<Watcher<IAlarm>> {
    const watcher = new Watcher<IAlarm>(this.thingsCloudDataSupplier.getRealtimeAlarm.bind(this.thingsCloudDataSupplier)(deviceId));
    await this.sleep(1000);
    return watcher;
  }


  /**
   * デバイスからの ManagedObject を監視、記録する Watcher を作成します。
   * @param deviceId 監視対象のデバイス
   * @returns デバイスからの ManagedObject を監視、記録する Watcher
   */
   async createMOWatcher(deviceId: string): Promise<Watcher<IManagedObject>> {
    const watcher = new Watcher<IManagedObject>(this.thingsCloudDataSupplier.getRealtimeManagedObject.bind(this.thingsCloudDataSupplier)(deviceId));
    await this.sleep(1000);
    return watcher;
  }

  /**
   * Watcherの監視対象を全て削除します。
   */
   removeWatcher(): void {
    this.thingsCloudDataSupplier.unsubscribeRealtime();
  }

  /**
   * 指定された親アセット配下の全アセットを取得します。
   * 配下のアセットが多い場合、処理時間を要します。
   * @param parentAssetId 検索する最上位のアセットID
   * @param filter fragment に関する一致条件
   * @returns 一致条件の fragment を持つ parentAssetId の子全てからなる配列
   */
  async selectAssets(parentAssetId: string, filter?: any): Promise<IManagedObject[]> {
    const mos = await this.thingsCloudClient.getAllSubAssets(parentAssetId);
    return this.subassets(mos, filter);
  }

  /**
   * ManagedObject の配列から、指定条件された fragment/value を持つ ManagedObject を
   * 抽出します。
   * @param mos ManagedObject の配列
   * @param filter ManagedObject の配列の抽出条件
   * @returns 抽出された ManagedObject 配列
   */
  subassets(mos: IManagedObject[], filter?: any): IManagedObject[] {
    if (!filter) return mos;
    return mos.filter( mo => 
      Object.keys(filter).reduce( (acc, key) => (acc && (filter[key] === mo[key])) , true as boolean)
    );
  };

  /**
   * アセット(デバイス)のアセット親子関係を登録します。
   * @param child 子デバイス id
   * @param parent 親デバイス id
   * 
   * @returns 子アセット(デバイス)情報
   */
  async putChildAsset(child: string, parent: string): Promise<IIdentified> {
    return (await this.thingsCloudClient.putChildAsset(child, parent)).data;
  }

  /**
   * アセット(デバイス)の親子関係を登録します。
   * @param child 子デバイス id
   * @param parent 親デバイス id
   * 
   * @returns 子デバイス情報
   */
  async putChildDevice(child: string, parent: string): Promise<IIdentified> {
    return (await this.thingsCloudClient.putChildDevice(child, parent)).data;
  }

  async getEPLFileList(): Promise<IEPLFile[]> {
    return await this.thingsCloudClient.getEPLFileList();
  }

  async getEPLFile(id: string): Promise<IEPLFile> {
    return await this.thingsCloudClient.getEPLFile(id);
  }

  async postEPLFile(eplFile: Partial<IEPLFile>): Promise<IEPLFile> {
    return await this.thingsCloudClient.postEPLFile(eplFile);
  }

  async putEPLFile(id: string, eplFile: Partial<IEPLFile>): Promise<IEPLFile> {
    return await this.thingsCloudClient.putEPLFile(id, eplFile);
  }

  async deleteEPLFile(id: string): Promise<void> {
    return await this.thingsCloudClient.deleteEPLFile(id);
  }
  
  unsubscribeRealtime(): void {
    this.thingsCloudDataSupplier.unsubscribeRealtime();
  }

}

export class Watcher<T> {
  private record: T[] = [];

  constructor(
    private subject: Subject<T>) {      
    this.subject.subscribe(
      (data) => { this.record.push(data); }
    );
  }

  private async addTimeoutTo<T>(promise: Promise<T>, time?: number): Promise<void | T> {
    let timer: any;
    const timeout = new Promise<void>( resolve => {
      timer = setTimeout( resolve, time || 6000); // 30000
    });
    const prom = async () => {
      const result = await promise;
      clearTimeout(timer);
      return result;
    };
    return Promise.race([timeout, prom()]);
  }

  unsubscribe(): void {
    this.subject.unsubscribe();
  }

  /**
   * 現在までで受け取った Measurement から filter 条件に合うものが存在するか判定し、
   * 存在した場合、最後に受け取ったものを返却します。
   * @param filter チェック対象を限定する filter
   * @returns 存在した場合、最後にうけとったもの、または undefined
   */
  checkRecord(filter?: any): void | T {
    if (!filter && this.record.length > 0) return this.record[this.record.length - 1];
    const match = this.record.filter( model =>
      Object.keys(filter)
      .reduce( (acc, key) => acc && (filter[key] === model[key]) ,true as boolean)
    );
    if (match.length === 0) return;
    return match[match.length - 1];
  }

  /**
   * 現在までで受け取った Measurement から filter 条件に合うものを受け取るか、
   * タイムアウトまで待ちます。デフォルトのタイムアウトは 6 秒です。
   * @param filter チェック対象を限定する filter
   * @param time タイムアウト(msec)。省略時 6 秒
   * @returns 受け取った場合、最後にうけとったもの、または(タイムアウト時)undefined
   */
  async waitFor(filter?: any, time?: number): Promise<void | T> {
    const recorded = this.checkRecord(filter);
    if (recorded) return recorded;
    return this.addTimeoutTo(
      new Promise<T>( resolve => {
        this.subject.subscribe(
          (measurement) => {
            if (filter) {
              const matches = Object.keys(filter)
                .reduce( (acc, key) => acc && (filter[key] === measurement[key]) ,true);
              if (matches) resolve(measurement);
            } else {
              resolve(measurement);
            }
          }
        );
     })
    , time);
  }

  /**
   * この watcher が捕捉したイベントすべてのコピーを返却します。
   * records には、createXXWatcher(deviceId) で指定された、deviceId
   * (省略時はすべてのデバイス)を source とする XX のデータが含まれます。
   * @returns records
   */
  getRecords(): T[] {
    const snapshot = Array.from(this.record);
    return snapshot;
  }
}
