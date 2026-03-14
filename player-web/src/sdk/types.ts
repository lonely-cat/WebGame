export interface GameMeta {
  gameCode: string;
  gameName: string;
  mode: "vue" | "static";
  loader: () => Promise<unknown> | unknown;
  multiplayer: boolean;
  routePath: string;
}

export interface GameContext {
  user: unknown;
  gameCode: string;
  roomInfo?: unknown;
  matchInfo?: unknown;
  config?: unknown;
  sdk: GamePlatformSDK;
}

export interface MatchResultPayload {
  gameCode: string;
  score?: number;
  winnerUserId?: number;
  summary?: Record<string, unknown>;
}

export interface GamePlatformSDK {
  getCurrentUser(): unknown;
  getGameConfig(gameCode: string): Promise<unknown>;
  reportResult(result: MatchResultPayload): Promise<unknown>;
  showToast(message: string): void;
  playSound(name: string): void;
  updateAssetView(): Promise<void>;
  openResultModal(result: MatchResultPayload): void;
}
