import { computed, ref } from "vue";
import { useMultiplayerRoomSession } from "../../composables/useMultiplayerRoomSession";
import {
  wsMessageTypes,
  parsePayload,
  type ClientWsMessage,
  type DrawGuessGuessPayload,
  type DrawGuessStrokePayload,
  type GameStateSyncPayload,
  type MatchEndPayload,
  type MatchStartPayload,
  type ServerWsEnvelope
} from "../../websocket/gameProtocol";

type Stroke = {
  color: string;
  width: number;
  points: Array<{ x: number; y: number }>;
};

const strokes = ref<Stroke[]>([]);
const guesses = ref<Array<{ userId: number; guess: string }>>([]);
const prompt = ref("");
const promptMask = ref("");
const drawerUserId = ref<number | null>(null);
const guessInput = ref("");

const roomSession = useMultiplayerRoomSession("draw-guess", {
  testUserPrefix: "drawguess",
  customMessageHandler: handleDrawGuessMessage
});

const isDrawer = computed(() => roomSession.currentUser.value?.userId === drawerUserId.value);
const displayPrompt = computed(() => isDrawer.value ? prompt.value || "..." : promptMask.value || "...");

function useDrawGuessSession() {
  syncWindowHelpers();
  return {
    strokes,
    guesses,
    prompt,
    promptMask,
    drawerUserId,
    guessInput,
    isDrawer,
    displayPrompt,
    sendStroke,
    submitGuess,
    syncWindowHelpers,
    ...roomSession
  };
}

function handleDrawGuessMessage(message: ServerWsEnvelope, helpers: {
  currentUser: typeof roomSession.currentUser;
  activeRoomCode: typeof roomSession.activeRoomCode;
  roomPlayers: typeof roomSession.roomPlayers;
  matchCode: typeof roomSession.matchCode;
  roleLabel: typeof roomSession.roleLabel;
  winner: typeof roomSession.winner;
  currentTurnLabel: typeof roomSession.currentTurnLabel;
  pushFeed: (text: string) => void;
  syncWindowHelpers: () => void;
}) {
  if (message.type === wsMessageTypes.matchStart && message.payload) {
    const payload = parsePayload<MatchStartPayload>(message.payload);
    if (payload?.playerStones && helpers.currentUser.value) {
      helpers.roleLabel.value = payload.playerStones[String(helpers.currentUser.value.userId)] ?? "spectator";
    }
    return false;
  }

  if (message.type === wsMessageTypes.playerAction && message.payload) {
    const strokePayload = parsePayload<DrawGuessStrokePayload>(message.payload);
    if (strokePayload?.type === "draw_stroke") {
      strokes.value = [...strokes.value, strokePayload.stroke];
      syncWindowHelpers();
      return true;
    }
    const guessPayload = parsePayload<DrawGuessGuessPayload>(message.payload);
    if (guessPayload?.type === "submit_guess") {
      guesses.value = [...guesses.value, {
        userId: Number((parsePayload<Record<string, unknown>>(message.payload)?.userId as number | undefined) ?? 0),
        guess: guessPayload.guess
      }];
      syncWindowHelpers();
      return true;
    }
  }

  if (message.type === wsMessageTypes.gameStateSync && message.payload) {
    const payload = parsePayload<GameStateSyncPayload<{
      phase?: string;
      secretWord?: string;
      promptMask?: string;
      drawerUserId?: number | null;
      strokes?: Array<{ userId: number; stroke: Stroke }>;
      guesses?: Array<{ userId: number; guess: string }>;
      winnerUserId?: number | null;
      currentTurn?: string;
    }>>(message.payload);
    if (payload) {
      prompt.value = String(payload.state.secretWord ?? "");
      promptMask.value = String(payload.state.promptMask ?? "");
      drawerUserId.value = payload.state.drawerUserId ?? null;
      strokes.value = (payload.state.strokes ?? []).map((entry) => entry.stroke);
      guesses.value = payload.state.guesses ?? [];
      helpers.currentTurnLabel.value = String(payload.state.currentTurn ?? "live");
      helpers.winner.value = payload.state.winnerUserId ? `User #${payload.state.winnerUserId}` : "";
      syncWindowHelpers();
    }
    return true;
  }

  if (message.type === wsMessageTypes.matchEnd && message.payload) {
    const payload = parsePayload<MatchEndPayload & { winnerUserId?: number; secretWord?: string }>(message.payload);
    if (payload?.secretWord) {
      prompt.value = payload.secretWord;
    }
    return false;
  }

  return false;
}

function sendStroke(stroke: Stroke) {
  const message: ClientWsMessage<DrawGuessStrokePayload> = {
    type: wsMessageTypes.playerAction,
    gameCode: "draw-guess",
    roomCode: roomSession.activeRoomCode.value,
    payload: {
      type: "draw_stroke",
      stroke
    },
    timestamp: new Date().toISOString()
  };
  roomSession.sendClientMessage(message);
}

function submitGuess() {
  const guess = guessInput.value.trim();
  if (!guess) return;
  const message: ClientWsMessage<DrawGuessGuessPayload> = {
    type: wsMessageTypes.playerAction,
    gameCode: "draw-guess",
    roomCode: roomSession.activeRoomCode.value,
    payload: {
      type: "submit_guess",
      guess
    },
    timestamp: new Date().toISOString()
  };
  roomSession.sendClientMessage(message);
  guessInput.value = "";
}

function syncWindowHelpers() {
  (window as any).render_game_to_text = () => JSON.stringify({
    mode: roomSession.inMatch.value ? "playing" : "room",
    phase: roomSession.phase.value,
    roomCode: roomSession.activeRoomCode.value || null,
    matchCode: roomSession.matchCode.value || null,
    role: roomSession.roleLabel.value,
    prompt: displayPrompt.value,
    drawerUserId: drawerUserId.value,
    strokes: strokes.value.length,
    guesses: guesses.value,
    winner: roomSession.winner.value || null
  });
  (window as any).advanceTime = () => Promise.resolve();
}

export { useDrawGuessSession };
