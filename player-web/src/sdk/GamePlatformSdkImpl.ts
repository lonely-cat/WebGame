import { gameApi } from "../api/gameApi";
import type { GamePlatformSDK, MatchResultPayload } from "./types";

export class GamePlatformSdkImpl implements GamePlatformSDK {
  getCurrentUser(): unknown {
    return null;
  }

  getGameConfig(gameCode: string): Promise<unknown> {
    return gameApi.getGameConfig(gameCode);
  }

  reportResult(result: MatchResultPayload): Promise<unknown> {
    return Promise.resolve(result);
  }

  showToast(message: string): void {
    console.info(message);
  }

  playSound(name: string): void {
    console.info(`play sound: ${name}`);
  }

  async updateAssetView(): Promise<void> {
  }

  openResultModal(result: MatchResultPayload): void {
    console.info("match result", result);
  }
}
