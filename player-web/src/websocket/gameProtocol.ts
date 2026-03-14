export const wsMessageTypes = {
  roomJoin: "ROOM_JOIN",
  roomLeave: "ROOM_LEAVE",
  roomReady: "ROOM_READY",
  roomCancelReady: "ROOM_CANCEL_READY",
  matchStart: "MATCH_START",
  playerAction: "PLAYER_ACTION",
  gameStateSync: "GAME_STATE_SYNC",
  matchEnd: "MATCH_END",
  heartbeat: "HEARTBEAT",
  error: "ERROR"
} as const;

export type WsMessageType = (typeof wsMessageTypes)[keyof typeof wsMessageTypes];

export interface ClientWsMessage<TPayload = unknown> {
  type: WsMessageType;
  gameCode?: string;
  roomCode?: string;
  matchCode?: string;
  payload?: TPayload;
  timestamp: string;
}

export interface ServerWsEnvelope {
  type: WsMessageType | string;
  gameCode?: string;
  roomCode?: string;
  matchCode?: string;
  payload?: string;
}

export interface RoomPlayerState {
  userId: number;
  readyStatus: number;
  seatNo: number;
  onlineStatus?: number;
}

export interface RoomStateMessage {
  roomCode: string;
  gameCode: string;
  roomStatus: number;
  currentPlayers: number;
  maxPlayers: number;
  players: RoomPlayerState[];
}

export interface MatchStartPayload {
  status?: string;
  matchId?: number;
  matchCode?: string;
  playerStones?: Record<string, "black" | "white">;
}

export interface GameStateSyncPayload<TState = Record<string, unknown>> {
  state: TState;
  playerStones?: Record<string, "black" | "white">;
}

export interface MatchEndPayload {
  winnerStone?: string | null;
}

export interface ErrorPayload {
  message?: string;
}

export interface MoveActionPayload {
  type: "move";
  row: number;
  col: number;
  stone: "black" | "white";
}

export function encodeClientMessage(message: ClientWsMessage): string {
  return JSON.stringify({
    ...message,
    payload: message.payload === undefined ? undefined : JSON.stringify(message.payload)
  });
}

export function parseServerEnvelope(raw: string): ServerWsEnvelope | null {
  try {
    return JSON.parse(raw) as ServerWsEnvelope;
  } catch {
    return null;
  }
}

export function parsePayload<TPayload>(payload?: string): TPayload | null {
  if (!payload) {
    return null;
  }
  try {
    return JSON.parse(payload) as TPayload;
  } catch {
    return null;
  }
}

export function isRoomStateMessage(candidate: unknown): candidate is RoomStateMessage {
  if (!candidate || typeof candidate !== "object") {
    return false;
  }
  const value = candidate as Record<string, unknown>;
  return typeof value.roomCode === "string" && Array.isArray(value.players);
}
