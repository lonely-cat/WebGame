import { computed, reactive, ref } from "vue";
import { authApi } from "../api/authApi";
import { assetApi } from "../api/assetApi";
import { roomApi } from "../api/roomApi";
import { GameSocketClient } from "../websocket/GameSocketClient";
import {
  encodeClientMessage,
  isRoomStateMessage,
  parsePayload,
  parseServerEnvelope,
  wsMessageTypes,
  type ErrorPayload,
  type MatchEndPayload,
  type MatchStartPayload,
  type RoomPlayerState
} from "../websocket/gameProtocol";

type PhaseKey = "auth" | "room" | "match";
type RoomSessionReturn = ReturnType<typeof createMultiplayerRoomSession>;

const sessionCache = new Map<string, RoomSessionReturn>();

export function useMultiplayerRoomSession(gameCode: string, options?: {
  defaultUsername?: string;
  defaultPassword?: string;
  testUserPrefix?: string;
}) {
  const cached = sessionCache.get(gameCode);
  if (cached) {
    return cached;
  }

  const session = createMultiplayerRoomSession(gameCode, options);
  sessionCache.set(gameCode, session);
  return session;
}

function createMultiplayerRoomSession(gameCode: string, options?: {
  defaultUsername?: string;
  defaultPassword?: string;
  testUserPrefix?: string;
}) {
  const loginForm = reactive({
    username: options?.defaultUsername ?? "admin",
    password: options?.defaultPassword ?? "admin123"
  });

  const loading = reactive({
    login: false,
    register: false,
    roomCreate: false
  });

  const authHint = ref("Use the seeded admin account or create a fresh test player.");
  const token = ref(localStorage.getItem("webgame_token") ?? "");
  const currentUser = ref<{ userId: number; username: string } | null>(null);
  const asset = reactive({ coin: 0, score: 0 });
  const socketClient = new GameSocketClient();
  const socketConnected = ref(false);
  const roomCodeInput = ref("");
  const activeRoomCode = ref("");
  const roomFeed = ref<Array<{ id: number; text: string }>>([]);
  const feedId = ref(1);
  const roomPlayers = ref<RoomPlayerState[]>([]);
  const matchCode = ref("");
  const roleLabel = ref("spectator");
  const winner = ref("");
  const currentTurnLabel = ref("pending");
  let socketHandlersAttached = false;

  const inMatch = computed(() => !!matchCode.value && roomPlayers.value.length >= 2);
  const phase = computed<PhaseKey>(() => {
    if (!token.value || !currentUser.value) return "auth";
    if (!activeRoomCode.value || !socketConnected.value || !inMatch.value) return "room";
    return "match";
  });
  const phaseTitle = computed(() => ({
    auth: "Authenticate",
    room: "Assemble the room",
    match: "Play the live match"
  }[phase.value]));
  const phaseDescription = computed(() => ({
    auth: "Sign in so the platform can fetch assets and open the realtime socket.",
    room: "Create or join a room, wait for both players to connect, and then start the match.",
    match: "The room has turned into a live session and is ready for game-specific UI."
  }[phase.value]));
  const canJoinRoom = computed(() => !!roomCodeInput.value && !!token.value && socketConnected.value);
  const canStartMatch = computed(() => !!activeRoomCode.value && !!socketConnected.value && roomPlayers.value.length >= 2);

  ensureSocketHandlers();
  syncWindowHelpers();

  return {
    loginForm,
    loading,
    authHint,
    token,
    currentUser,
    asset,
    socketConnected,
    roomCodeInput,
    activeRoomCode,
    roomFeed,
    roomPlayers,
    matchCode,
    roleLabel,
    winner,
    currentTurnLabel,
    inMatch,
    phase,
    phaseTitle,
    phaseDescription,
    canJoinRoom,
    canStartMatch,
    login,
    registerTestUser,
    quickStart,
    loadAsset,
    createRoom,
    connectSocket,
    waitForSocket,
    joinRoomViaSocket,
    sendReady,
    startMatch,
    playerNameBySeat,
    seatStatus,
    seatTone,
    phaseClass,
    pushFeed,
    syncWindowHelpers
  };

  function ensureSocketHandlers() {
    if (socketHandlersAttached) return;
    socketHandlersAttached = true;

    socketClient.onOpen(() => {
      socketConnected.value = true;
      pushFeed("WebSocket connected.");
      syncWindowHelpers();
    });

    socketClient.onClose(() => {
      socketConnected.value = false;
      pushFeed("WebSocket disconnected.");
      syncWindowHelpers();
    });

    socketClient.onMessage(handleSocketMessage);
  }

  function playerNameBySeat(seatIndex: number, fallback: string) {
    const player = roomPlayers.value[seatIndex];
    return player?.userId ? `User #${player.userId}` : fallback;
  }

  function seatStatus(seatIndex: number) {
    const player = roomPlayers.value[seatIndex];
    if (!player) return "Waiting for player";
    return player.readyStatus === 1 ? "Ready" : "Joined";
  }

  function seatTone(seatIndex: number) {
    const player = roomPlayers.value[seatIndex];
    if (!player) return "empty";
    return player.readyStatus === 1 ? "ready" : "joined";
  }

  function phaseClass(target: PhaseKey) {
    return {
      pill: true,
      active: phase.value === target,
      complete: ["auth", "room", "match"].indexOf(phase.value) > ["auth", "room", "match"].indexOf(target)
    };
  }

  function pushFeed(text: string) {
    roomFeed.value.unshift({ id: feedId.value++, text });
    roomFeed.value = roomFeed.value.slice(0, 12);
  }

  async function login() {
    loading.login = true;
    try {
      const result = await authApi.login({
        username: loginForm.username,
        password: loginForm.password
      }) as any;
      if (!result.success) {
        authHint.value = result.message;
        return;
      }
      token.value = result.data.accessToken;
      localStorage.setItem("webgame_token", token.value);
      currentUser.value = {
        userId: result.data.userId,
        username: result.data.username
      };
      await loadAsset();
      authHint.value = `Logged in as ${result.data.username}`;
      pushFeed(`Signed in as ${result.data.username}`);
    } finally {
      loading.login = false;
      syncWindowHelpers();
    }
  }

  async function registerTestUser() {
    loading.register = true;
    try {
      const suffix = Math.floor(Math.random() * 100000);
      const username = `${options?.testUserPrefix ?? gameCode}_${suffix}`;
      const result = await authApi.register({
        username,
        password: "test123456",
        nickname: `${gameCode} ${suffix}`
      }) as any;
      authHint.value = result.success ? `Created ${username} / test123456` : result.message;
      pushFeed(authHint.value);
    } finally {
      loading.register = false;
    }
  }

  async function quickStart() {
    await login();
    if (!currentUser.value) return;
    connectSocket();
    await waitForSocket();
    await createRoom();
    if (!activeRoomCode.value) return;
    joinRoomViaSocket();
    await pause(220);
    sendReady();
    await pause(220);
    pushFeed("Quick start finished. Invite one more player, then press Start.");
  }

  async function loadAsset() {
    if (!token.value) return;
    const result = await assetApi.getMyAsset() as any;
    if (result.success) {
      asset.coin = Number(result.data.coin ?? 0);
      asset.score = Number(result.data.score ?? 0);
    }
  }

  async function createRoom() {
    loading.roomCreate = true;
    try {
      const result = await roomApi.createRoom({ gameCode, maxPlayers: 2 }) as any;
      if (result.success) {
        roomCodeInput.value = result.data.roomCode;
        activeRoomCode.value = result.data.roomCode;
        matchCode.value = "";
        roleLabel.value = "spectator";
        roomPlayers.value = [];
        pushFeed(`Created room ${result.data.roomCode}`);
      } else {
        pushFeed(result.message);
      }
    } finally {
      loading.roomCreate = false;
      syncWindowHelpers();
    }
  }

  function connectSocket() {
    if (!token.value || !currentUser.value) {
      pushFeed("Login first before opening the socket.");
      return;
    }
    socketClient.disconnect();
    socketClient.connect(token.value, currentUser.value.userId);
  }

  function waitForSocket() {
    if (socketConnected.value) return Promise.resolve();
    return new Promise<void>((resolve) => {
      const startedAt = Date.now();
      const timer = window.setInterval(() => {
        if (socketConnected.value || Date.now() - startedAt > 4000) {
          window.clearInterval(timer);
          resolve();
        }
      }, 100);
    });
  }

  function joinRoomViaSocket() {
    if (!roomCodeInput.value) return;
    activeRoomCode.value = roomCodeInput.value.toUpperCase();
    socketClient.send(encodeClientMessage({
      type: wsMessageTypes.roomJoin,
      gameCode,
      roomCode: activeRoomCode.value,
      payload: { userId: currentUser.value?.userId ?? 0 },
      timestamp: new Date().toISOString()
    }));
    syncWindowHelpers();
  }

  function sendReady() {
    socketClient.send(encodeClientMessage({
      type: wsMessageTypes.roomReady,
      gameCode,
      roomCode: activeRoomCode.value,
      payload: { ready: true },
      timestamp: new Date().toISOString()
    }));
  }

  function startMatch() {
    socketClient.send(encodeClientMessage({
      type: wsMessageTypes.matchStart,
      gameCode,
      roomCode: activeRoomCode.value,
      payload: { start: true },
      timestamp: new Date().toISOString()
    }));
  }

  function handleSocketMessage(event: MessageEvent) {
    const parsed = parseServerEnvelope(event.data);
    if (!parsed) {
      pushFeed(String(event.data));
      return;
    }
    if (parsed.type === wsMessageTypes.matchStart) {
      if (parsed.matchCode) {
        matchCode.value = parsed.matchCode;
      }
      const payload = parsePayload<MatchStartPayload>(parsed.payload);
      if (payload?.matchCode) {
        matchCode.value = payload.matchCode;
      }
      if (currentUser.value && payload?.playerStones) {
        roleLabel.value = payload.playerStones[String(currentUser.value.userId)] ?? "spectator";
      }
      currentTurnLabel.value = "live";
      pushFeed("Match started.");
      syncWindowHelpers();
      return;
    }
    if (parsed.type === wsMessageTypes.gameStateSync) {
      currentTurnLabel.value = "live";
      syncWindowHelpers();
      return;
    }
    if (parsed.type === wsMessageTypes.matchEnd) {
      const result = parsePayload<MatchEndPayload>(parsed.payload);
      winner.value = result?.winnerStone ?? winner.value;
      currentTurnLabel.value = "finished";
      pushFeed(`Match finished. Winner: ${winner.value || "n/a"}`);
      syncWindowHelpers();
      return;
    }
    if (isRoomStateMessage(parsed)) {
      activeRoomCode.value = parsed.roomCode;
      roomPlayers.value = parsed.players ?? [];
      pushFeed(`Room ${parsed.roomCode}: ${parsed.currentPlayers}/${parsed.maxPlayers} players`);
      syncWindowHelpers();
      return;
    }
    if (parsed.type === wsMessageTypes.error) {
      const errorPayload = parsePayload<ErrorPayload>(parsed.payload);
      pushFeed(`Error: ${errorPayload?.message ?? "unknown"}`);
    }
  }

  function syncWindowHelpers() {
    (window as any).render_game_to_text = () => JSON.stringify({
      mode: inMatch.value ? "playing" : "room",
      phase: phase.value,
      roomCode: activeRoomCode.value || null,
      matchCode: matchCode.value || null,
      role: roleLabel.value,
      socketConnected: socketConnected.value,
      roomPlayers: roomPlayers.value,
      turn: currentTurnLabel.value,
      winner: winner.value || null
    });

    (window as any).advanceTime = () => Promise.resolve();
  }

  function pause(ms: number) {
    return new Promise((resolve) => window.setTimeout(resolve, ms));
  }
}
