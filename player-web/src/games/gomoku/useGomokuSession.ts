import { computed, reactive, ref } from "vue";
import { authApi } from "../../api/authApi";
import { assetApi } from "../../api/assetApi";
import { roomApi } from "../../api/roomApi";
import { GameSocketClient } from "../../websocket/GameSocketClient";
import {
  isRoomStateMessage,
  parsePayload,
  parseServerEnvelope,
  type ErrorPayload,
  type MatchEndPayload,
  type MatchStartPayload,
  type MoveActionPayload,
  type RoomPlayerState,
  type ServerWsEnvelope,
  type GameStateSyncPayload
} from "../../websocket/gameProtocol";

type Stone = "black" | "white" | null;
type RoomPlayer = RoomPlayerState;

const boardSize = 15;
const canvasSize = 720;
const padding = 48;
const cell = (canvasSize - padding * 2) / (boardSize - 1);

const loginForm = reactive({
  username: "admin",
  password: "admin123"
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
const board = ref<Stone[][]>(createEmptyBoard());
const moveHistory = ref<Array<{ row: number; col: number; stone: Exclude<Stone, null> }>>([]);
const currentTurn = ref<Exclude<Stone, null>>("black");
const winner = ref("");
const roomPlayers = ref<RoomPlayer[]>([]);
const animationCounter = ref(0);
const matchCode = ref("");
const myStone = ref<Exclude<Stone, null> | "spectator">("spectator");

let socketHandlersAttached = false;

const inMatch = computed(() => !!matchCode.value && roomPlayers.value.length >= 2);
const phase = computed(() => {
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
  auth: "Use the seeded admin account or a test account so the page can fetch assets and open the realtime socket.",
  room: "Create or join a room, wait for both players to connect, and only then start the match.",
  match: "The server validates every move and broadcasts the authoritative state back to both clients."
}[phase.value]));
const boardHeading = computed(() => inMatch.value ? "Authoritative live board" : "Waiting for both players");
const boardStatus = computed(() => inMatch.value ? `${currentTurn.value} to move` : "staging room");
const boardCopy = computed(() => inMatch.value
  ? "Moves are only painted after the backend accepts them, so both browsers stay in sync."
  : "The board becomes interactive only after both players are seated and the match starts.");
const currentTurnLabel = computed(() => winner.value ? "finished" : currentTurn.value);
const myStoneLabel = computed(() => myStone.value);
const canJoinRoom = computed(() => !!roomCodeInput.value && !!token.value && socketConnected.value);
const canStartMatch = computed(() => !!activeRoomCode.value && !!socketConnected.value && roomPlayers.value.length >= 2);
const blackPlayerName = computed(() => playerNameBySeat(0, "Seat 1"));
const whitePlayerName = computed(() => playerNameBySeat(1, "Seat 2"));

function useGomokuSession() {
  ensureSocketHandlers();
  syncWindowHelpers();
  return {
    boardSize,
    canvasSize,
    padding,
    cell,
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
    board,
    moveHistory,
    currentTurn,
    winner,
    roomPlayers,
    matchCode,
    myStone,
    inMatch,
    phase,
    phaseTitle,
    phaseDescription,
    boardHeading,
    boardStatus,
    boardCopy,
    currentTurnLabel,
    myStoneLabel,
    canJoinRoom,
    canStartMatch,
    blackPlayerName,
    whitePlayerName,
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
    handleBoardClick,
    renderBoardToCanvas,
    seatStatus,
    seatTone,
    phaseClass,
    resetBoard,
    syncWindowHelpers
  };
}

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

function createEmptyBoard(): Stone[][] {
  return Array.from({ length: boardSize }, () => Array.from({ length: boardSize }, () => null));
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

function phaseClass(target: "auth" | "room" | "match") {
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

function resetBoard() {
  board.value = createEmptyBoard();
  moveHistory.value = [];
  currentTurn.value = "black";
  winner.value = "";
  syncWindowHelpers();
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
    const username = `gomoku_${suffix}`;
    const result = await authApi.register({
      username,
      password: "test123456",
      nickname: `Gomoku ${suffix}`
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
    const result = await roomApi.createRoom({ gameCode: "gomoku", maxPlayers: 2 }) as any;
    if (result.success) {
      roomCodeInput.value = result.data.roomCode;
      activeRoomCode.value = result.data.roomCode;
      matchCode.value = "";
      myStone.value = "spectator";
      roomPlayers.value = [];
      resetBoard();
      pushFeed(`Created room ${result.data.roomCode}`);
    } else {
      pushFeed(result.message);
    }
  } finally {
    loading.roomCreate = false;
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
  socketClient.send({
    type: "ROOM_JOIN",
    gameCode: "gomoku",
    roomCode: activeRoomCode.value,
    payload: JSON.stringify({ userId: currentUser.value?.userId ?? 0 }),
    timestamp: new Date().toISOString()
  });
  syncWindowHelpers();
}

function sendReady() {
  socketClient.send({
    type: "ROOM_READY",
    gameCode: "gomoku",
    roomCode: activeRoomCode.value,
    payload: JSON.stringify({ ready: true }),
    timestamp: new Date().toISOString()
  });
}

function startMatch() {
  socketClient.send({
    type: "MATCH_START",
    gameCode: "gomoku",
    roomCode: activeRoomCode.value,
    payload: JSON.stringify({ start: true }),
    timestamp: new Date().toISOString()
  });
}

function handleBoardClick(event: MouseEvent, target: HTMLCanvasElement | null) {
  if (!inMatch.value || !activeRoomCode.value || !socketConnected.value || winner.value || myStone.value === "spectator") {
    return;
  }
  if (myStone.value !== currentTurn.value) {
    pushFeed(`It is ${currentTurn.value}'s turn.`);
    return;
  }
  if (!target) return;
  const rect = target.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;
  const col = Math.round((x - padding) / cell);
  const row = Math.round((y - padding) / cell);
  if (row < 0 || row >= boardSize || col < 0 || col >= boardSize || board.value[row][col]) {
    return;
  }
  sendMove(row, col, currentTurn.value);
}

function sendMove(row: number, col: number, stone: Exclude<Stone, null>) {
  const payload: MoveActionPayload = { type: "move", row, col, stone };
  socketClient.send({
    type: "PLAYER_ACTION",
    gameCode: "gomoku",
    roomCode: activeRoomCode.value,
    payload: JSON.stringify(payload),
    timestamp: new Date().toISOString()
  });
}

function applyMove(row: number, col: number, stone: Exclude<Stone, null>) {
  if (board.value[row][col] || winner.value) return;
  board.value[row][col] = stone;
  moveHistory.value.push({ row, col, stone });
  if (checkWinner(row, col, stone)) {
    winner.value = stone;
    pushFeed(`${stone} wins the round.`);
  } else {
    currentTurn.value = stone === "black" ? "white" : "black";
  }
  syncWindowHelpers();
}

function applyServerState(serverState: {
  playerStones?: Record<string, Exclude<Stone, null>>;
  currentTurn?: Exclude<Stone, null>;
  winner?: string | null;
  moves?: Array<{ row: number; col: number; stone: Exclude<Stone, null> }>;
}) {
  resetBoard();
  const moves = serverState.moves ?? [];
  for (const move of moves) {
    if (!board.value[move.row][move.col]) {
      board.value[move.row][move.col] = move.stone;
      moveHistory.value.push(move);
    }
  }
  if (currentUser.value && serverState.playerStones) {
    myStone.value = serverState.playerStones[String(currentUser.value.userId)] ?? "spectator";
  }
  currentTurn.value = serverState.currentTurn ?? currentTurn.value;
  winner.value = serverState.winner ?? "";
  syncWindowHelpers();
}

function checkWinner(row: number, col: number, stone: Exclude<Stone, null>) {
  const directions = [[1, 0], [0, 1], [1, 1], [1, -1]];
  return directions.some(([dr, dc]) => {
    let count = 1;
    count += countDirection(row, col, dr, dc, stone);
    count += countDirection(row, col, -dr, -dc, stone);
    return count >= 5;
  });
}

function countDirection(row: number, col: number, dr: number, dc: number, stone: Exclude<Stone, null>) {
  let count = 0;
  let r = row + dr;
  let c = col + dc;
  while (r >= 0 && r < boardSize && c >= 0 && c < boardSize && board.value[r][c] === stone) {
    count += 1;
    r += dr;
    c += dc;
  }
  return count;
}

function renderBoardToCanvas(ctx: CanvasRenderingContext2D | null) {
  if (!ctx) return;
  ctx.clearRect(0, 0, canvasSize, canvasSize);
  const gradient = ctx.createLinearGradient(0, 0, canvasSize, canvasSize);
  gradient.addColorStop(0, "#f3d08b");
  gradient.addColorStop(1, "#d4a55e");
  ctx.fillStyle = gradient;
  ctx.fillRect(0, 0, canvasSize, canvasSize);
  ctx.strokeStyle = "rgba(66, 39, 8, 0.75)";
  ctx.lineWidth = 1.2;
  for (let i = 0; i < boardSize; i += 1) {
    const offset = padding + i * cell;
    ctx.beginPath();
    ctx.moveTo(padding, offset);
    ctx.lineTo(canvasSize - padding, offset);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(offset, padding);
    ctx.lineTo(offset, canvasSize - padding);
    ctx.stroke();
  }
  for (let row = 0; row < boardSize; row += 1) {
    for (let col = 0; col < boardSize; col += 1) {
      const stone = board.value[row][col];
      if (!stone) continue;
      const x = padding + col * cell;
      const y = padding + row * cell;
      ctx.beginPath();
      ctx.arc(x, y, cell * 0.38, 0, Math.PI * 2);
      const stoneGradient = ctx.createRadialGradient(x - 6, y - 6, 4, x, y, cell * 0.38);
      if (stone === "black") {
        stoneGradient.addColorStop(0, "#6e6e6e");
        stoneGradient.addColorStop(1, "#111111");
      } else {
        stoneGradient.addColorStop(0, "#ffffff");
        stoneGradient.addColorStop(1, "#d7d7d7");
      }
      ctx.fillStyle = stoneGradient;
      ctx.fill();
      ctx.strokeStyle = "rgba(0,0,0,0.18)";
      ctx.stroke();
    }
  }
  const latest = moveHistory.value.at(-1);
  if (latest) {
    ctx.fillStyle = latest.stone === "black" ? "#f6d76b" : "#8d4f0f";
    ctx.beginPath();
    ctx.arc(padding + latest.col * cell, padding + latest.row * cell, 5, 0, Math.PI * 2);
    ctx.fill();
  }
}

function handleSocketMessage(event: MessageEvent) {
  const parsed = parseServerEnvelope(event.data) as ServerWsEnvelope | null;
  if (!parsed) {
    pushFeed(String(event.data));
    return;
  }
  if (parsed.type === "PLAYER_ACTION" && parsed.payload) {
    const payload = parsePayload<MoveActionPayload>(parsed.payload);
    if (payload?.type === "move") {
      applyMove(payload.row, payload.col, payload.stone);
    }
    return;
  }
  if (parsed.type === "MATCH_START") {
    if (parsed.matchCode) {
      matchCode.value = parsed.matchCode;
    }
    if (parsed.payload) {
      const payload = parsePayload<MatchStartPayload>(parsed.payload);
      if (payload?.matchCode) {
        matchCode.value = payload.matchCode;
      }
      if (currentUser.value && payload?.playerStones) {
        myStone.value = payload.playerStones[String(currentUser.value.userId)] ?? "spectator";
      }
    }
    resetBoard();
    pushFeed("Match started.");
    return;
  }
  if (parsed.type === "GAME_STATE_SYNC" && parsed.payload) {
    if (parsed.matchCode) {
      matchCode.value = parsed.matchCode;
    }
    const payload = parsePayload<GameStateSyncPayload<{
      currentTurn?: Exclude<Stone, null>;
      winner?: string | null;
      moves?: Array<{ row: number; col: number; stone: Exclude<Stone, null> }>;
    }>>(parsed.payload);
    if (payload) {
      applyServerState({
        ...payload.state,
        playerStones: payload.playerStones
      });
    }
    return;
  }
  if (parsed.type === "MATCH_END" && parsed.payload) {
    const result = parsePayload<MatchEndPayload>(parsed.payload);
    winner.value = result?.winnerStone ?? winner.value;
    pushFeed(`Match finished. Winner: ${winner.value || "n/a"}`);
    syncWindowHelpers();
    return;
  }
  if (isRoomStateMessage(parsed)) {
    const roomState = parsed;
    activeRoomCode.value = roomState.roomCode;
    roomPlayers.value = roomState.players ?? [];
    pushFeed(`Room ${roomState.roomCode}: ${roomState.currentPlayers}/${roomState.maxPlayers} players`);
    syncWindowHelpers();
    return;
  }
  if (parsed.type === "ERROR") {
    const errorPayload = parsePayload<ErrorPayload>(parsed.payload);
    pushFeed(`Error: ${errorPayload?.message ?? "unknown"}`);
  }
}

function syncWindowHelpers() {
  (window as any).render_game_to_text = () => JSON.stringify({
    mode: winner.value ? "finished" : inMatch.value ? "playing" : "room",
    phase: phase.value,
    note: "origin is top-left, rows grow downward, cols grow rightward",
    roomCode: activeRoomCode.value || null,
    matchCode: matchCode.value || null,
    myStone: myStone.value,
    socketConnected: socketConnected.value,
    roomPlayers: roomPlayers.value,
    turn: currentTurn.value,
    winner: winner.value || null,
    moves: moveHistory.value,
    latestMove: moveHistory.value.at(-1) ?? null
  });

  (window as any).advanceTime = (ms: number) => {
    animationCounter.value += ms;
    return Promise.resolve();
  };
}

function pause(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

export { useGomokuSession };
