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
const roundNo = ref(1);
const maxRounds = ref(3);
const scores = ref<Record<string, number>>({});
const roundPhase = ref("drawing");
const roundEndsAt = ref<string | null>(null);
const roundEndReason = ref("");
const nowTick = ref(Date.now());

if (!(window as any).__draw_guess_tick__) {
  (window as any).__draw_guess_tick__ = window.setInterval(() => {
    nowTick.value = Date.now();
  }, 1000);
}

const roomSession = useMultiplayerRoomSession("draw-guess", {
  testUserPrefix: "drawguess",
  customMessageHandler: handleDrawGuessMessage
});

const isDrawer = computed(() => roomSession.currentUser.value?.userId === drawerUserId.value);
const displayPrompt = computed(() => {
  if (roundPhase.value !== "drawing") {
    return prompt.value || "...";
  }
  return isDrawer.value ? prompt.value || "..." : promptMask.value || "...";
});
const secondsRemaining = computed(() => {
  if (!roundEndsAt.value || roundPhase.value !== "drawing") {
    return 0;
  }
  const remainingMs = new Date(roundEndsAt.value).getTime() - nowTick.value;
  return Math.max(0, Math.ceil(remainingMs / 1000));
});
const sortedScores = computed(() =>
  Object.entries(scores.value)
    .map(([userId, score]) => ({ userId: Number(userId), score }))
    .sort((left, right) => right.score - left.score)
);
const canAdvanceRound = computed(() =>
  roomSession.inMatch.value &&
  roundPhase.value === "round_finished" &&
  roundNo.value < maxRounds.value
);
const roundSummary = computed(() => {
  if (roundPhase.value === "drawing") {
    return isDrawer.value
      ? "Draw clearly so the guesser can find the word before time runs out."
      : "Watch the sketch, submit guesses, and race the countdown.";
  }
  if (roundEndReason.value === "timeout") {
    return `Time is up. The word was "${prompt.value || displayPrompt.value}".`;
  }
  if (roomSession.winner.value) {
    return `${roomSession.winner.value} solved the prompt "${prompt.value || displayPrompt.value}".`;
  }
  return "Round finished.";
});

function useDrawGuessSession() {
  syncWindowHelpers();
  return {
    strokes,
    guesses,
    prompt,
    promptMask,
    drawerUserId,
    guessInput,
    roundNo,
    maxRounds,
    scores,
    roundPhase,
    roundEndsAt,
    roundEndReason,
    secondsRemaining,
    sortedScores,
    canAdvanceRound,
    roundSummary,
    isDrawer,
    displayPrompt,
    sendStroke,
    submitGuess,
    startNextRound,
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
      if (payload.playerStones && helpers.currentUser.value) {
        helpers.roleLabel.value = payload.playerStones[String(helpers.currentUser.value.userId)] ?? helpers.roleLabel.value;
      }
      prompt.value = String(payload.state.secretWord ?? "");
      promptMask.value = String(payload.state.promptMask ?? "");
      drawerUserId.value = payload.state.drawerUserId ?? null;
      strokes.value = (payload.state.strokes ?? []).map((entry) => entry.stroke);
      guesses.value = payload.state.guesses ?? [];
      roundNo.value = Number(payload.state.roundNo ?? 1);
      maxRounds.value = Number(payload.state.maxRounds ?? 3);
      roundPhase.value = String(payload.state.phase ?? "drawing");
      roundEndsAt.value = payload.state.roundEndsAt ? String(payload.state.roundEndsAt) : null;
      roundEndReason.value = String(payload.state.roundEndReason ?? "");
      scores.value = (payload.state.scores as Record<string, number> | undefined) ?? {};
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

function startNextRound() {
  const message: ClientWsMessage<{ type: "next_round" }> = {
    type: wsMessageTypes.playerAction,
    gameCode: "draw-guess",
    roomCode: roomSession.activeRoomCode.value,
    payload: {
      type: "next_round"
    },
    timestamp: new Date().toISOString()
  };
  roomSession.sendClientMessage(message);
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
    roundNo: roundNo.value,
    maxRounds: maxRounds.value,
    roundPhase: roundPhase.value,
    roundEndReason: roundEndReason.value || null,
    secondsRemaining: secondsRemaining.value,
    strokes: strokes.value.length,
    guesses: guesses.value,
    scores: scores.value,
    winner: roomSession.winner.value || null
  });
  (window as any).advanceTime = () => Promise.resolve();
}

export { useDrawGuessSession };
