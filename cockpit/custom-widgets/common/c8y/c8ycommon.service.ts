import { Injectable } from "@angular/core";
import { C8yService } from "./c8y.service";
import { IManagedObject } from "@c8y/client";

@Injectable()
export class C8yCommonService {

  constructor(private c8yService: C8yService) {}

  /**
   * MO ID に対し、サブアセットを取得する。
   * ID がデバイスの場合、単独のデバイスが設定され、アセットの場合、再帰的に
   * 子アセットのデバイスが設定される。
   *
   * @param {string} moId MO ID
   * 
   * @returns デバイスIDの配列
   */
  async getSubAssets(moId: string): Promise<string[]> {
    const mos = await this.getSubAssetMOs(moId);
    return mos.map( mo => mo.id );
  }
  
  /**
   * MO ID に対し、サブアセットを取得する。
   * ID がデバイスの場合、単独のデバイスが設定され、アセットの場合、再帰的に
   * 子アセットのデバイスが設定される。
   *
   * @param {string} moId MO ID
   * 
   * @returns デバイス ManagedObject の配列
   */
  async getSubAssetMOs(moId: string): Promise<IManagedObject[]> {
    const devices: IManagedObject[] = [];
    await this.getSubAssetMOsImpl(moId, devices);
    
    return devices;
  }

  private async getSubAssetMOsImpl(moId: string, devices: IManagedObject[]): Promise<void> {
    const res = await this.c8yService.getMO(moId);
  
    if (res.data.childAssets.references.length > 0) {
      // 指定されたのはグループ
      for (const data of res.data.childAssets.references) {
        await this.getSubAssetMOsImpl(data.managedObject.id, devices); // 再帰
      }
    } else {
      // 指定されたのはデバイス
      if (devices.map( mo => mo.id).indexOf(res.data.id) > -1) return;
      devices.push(res.data);
    }
  }

  /**
   * 指定された ManagedObject および子、孫、、の ManagedObject を取得し、返却します。
   * @param parentAssetId 親アセット(またはデバイス)ID
   * @returns 自分自身、および子アセット/子デバイスからなる ManagedObject 配列
   */
    async getAllSubAssets(parentAssetId: string): Promise<any[]> {
    const assets = [];
    await this.getAllSubAssetsImpl(parentAssetId, assets);
    return assets;
  }

  private async getAllSubAssetsImpl(parentAssetId: string, assets: any[]): Promise<void> {
    const parentAsset: IManagedObject = await this.c8yService.getManagedObjectById(parentAssetId);
    if (assets.map( (asset: IManagedObject) => asset.id).indexOf(parentAsset.id) == -1)
      assets.push(parentAsset);
    if (parentAsset.childAssets.references.length > 0) {
      for (const child of parentAsset.childAssets.references) {
        await this.getAllSubAssetsImpl(child.managedObject.id, assets);
      }
      // const promises = parentAsset.childAssets.references.map( async (child) => 
      //   await this.getAllSubAssetsImpl(child.managedObject.id, assets) );
      // Promise.all(promises);
    }
  }
}