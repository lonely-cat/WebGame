<template>
  <div class="gomoku-page">
    <section class="hero">
      <div>
        <p class="eyebrow">WebGame Multiplayer Lab</p>
        <h1>Gomoku Online Prototype</h1>
        <p class="subtitle">
          先用一版可玩的五子棋联机页把房间、准备、开始和落子同步跑通。
        </p>
      </div>
      <div class="auth-card">
        <h2>Quick Login</h2>
        <label>
          Username
          <input id="login-username" v-model="loginForm.username" />
        </label>
        <label>
          Password
          <input id="login-password" v-model="loginForm.password" type="password" />
        </label>
        <button id="login-btn" @click="login" :disabled="loading.login">
          {{ loading.login ? "Signing In..." : "Login" }}
        </button>
        <button id="quick-start-btn" @click="quickStart" :disabled="loading.login || loading.roomCreate">
          Quick Start
        </button>
        <button class="ghost" @click="registerTestUser" :disabled="loading.register">
          {{ loading.register ? "Creating..." : "Create Test User" }}
        </button>
        <p class="hint">{{ authHint }}</p>
      </div>
    </section>

    <section class="workspace">
      <aside class="sidebar">
        <div class="panel">
          <h3>Room Controls</h3>
          <div class="row">
            <button @click="createRoom" :disabled="!token || loading.roomCreate">Create Room</button>
            <button class="ghost" @click="connectSocket" :disabled="!token || socketConnected">Connect WS</button>
          </div>
          <label>
            Room Code
            <input id="room-code-input" v-model="roomCodeInput" placeholder="BF79F4" />
          </label>
          <div class="row">
            <button id="join-room-btn" @click="joinRoomViaSocket" :disabled="!canJoinRoom">Join Room</button>
            <button id="ready-btn" class="ghost" @click="sendReady" :disabled="!activeRoomCode || !socketConnected">Ready</button>
            <button id="start-btn" class="ghost" @click="startMatch" :disabled="!activeRoomCode || !socketConnected">Start</button>
          </div>
          <dl class="stats">
            <div><dt>User</dt><dd>{{ currentUser?.username ?? "-" }}</dd></div>
            <div><dt>Coins</dt><dd>{{ asset.coin }}</dd></div>
            <div><dt>Score</dt><dd>{{ asset.score }}</dd></div>
            <div><dt>Room</dt><dd id="active-room-code">{{ activeRoomCode || "-" }}</dd></div>
            <div><dt>Status</dt><dd>{{ socketConnected ? "socket ready" : "offline" }}</dd></div>
            <div><dt>Turn</dt><dd>{{ currentTurnLabel }}</dd></div>
            <div><dt>Stone</dt><dd id="my-stone">{{ myStoneLabel }}</dd></div>
            <div><dt>Match</dt><dd id="active-match-code">{{ matchCode || "-" }}</dd></div>
          </dl>
        </div>

        <div class="panel">
          <h3>Room Feed</h3>
          <ul class="feed">
            <li v-for="entry in roomFeed" :key="entry.id">{{ entry.text }}</li>
          </ul>
        </div>
      </aside>

      <main class="board-shell">
        <div class="board-frame">
          <canvas
            ref="canvasRef"
            width="720"
            height="720"
            @click="handleCanvasClick"
          />
        </div>
        <div class="legend">
          <span>Black: {{ blackPlayerName }}</span>
          <span>White: {{ whitePlayerName }}</span>
          <span v-if="winner">Winner: {{ winner }}</span>
        </div>
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { authApi } from "../../api/authApi";
import { assetApi } from "../../api/assetApi";
import { roomApi } from "../../api/roomApi";
import { GameSocketClient } from "../../websocket/GameSocketClient";

type Stone = "black" | "white" | null;
type PlayerActionPayload = {
  type: "move";
  row: number;
  col: number;
  stone: Exclude<Stone, null>;
};

type WsEnvelope = {
  type: string;
  gameCode?: string;
  roomCode?: string;
  matchCode?: string;
  payload?: string;
};

const boardSize = 15;
const canvasSize = 720;
const padding = 48;
const cell = (canvasSize - padding * 2) / (boardSize - 1);
const canvasRef = ref<HTMLCanvasElement | null>(null);
const ctxRef = ref<CanvasRenderingContext2D | null>(null);

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
const roomPlayers = ref<Array<{ userId: number; readyStatus: number; seatNo: number }>>([]);
const animationCounter = ref(0);
const matchCode = ref("");
const myStone = ref<Exclude<Stone, null> | "spectator">("spectator");

const blackPlayerName = computed(() => roomPlayers.value[0]?.userId ? `User #${roomPlayers.value[0].userId}` : "Seat 1");
const whitePlayerName = computed(() => roomPlayers.value[1]?.userId ? `User #${roomPlayers.value[1].userId}` : "Seat 2");
const currentTurnLabel = computed(() => winner.value ? "finished" : currentTurn.value);
const myStoneLabel = computed(() => myStone.value);
const canJoinRoom = computed(() => !!roomCodeInput.value && !!token.value && socketConnected.value);

function createEmptyBoard(): Stone[][] {
  return Array.from({ length: boardSize }, () => Array.from({ length: boardSize }, () => null));
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
  renderBoard();
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
  if (socketConnected.value) {
    return Promise.resolve();
  }
  return new Promise<void>((resolve) => {
    const done = () => resolve();
    socketClient.onOpen(done);
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

function handleCanvasClick(event: MouseEvent) {
  if (!activeRoomCode.value || !socketConnected.value || winner.value || myStone.value === "spectator") {
    return;
  }
  if (myStone.value !== currentTurn.value) {
    pushFeed(`It is ${currentTurn.value}'s turn.`);
    return;
  }
  const target = canvasRef.value;
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
  const payload: PlayerActionPayload = { type: "move", row, col, stone };
  socketClient.send({
    type: "PLAYER_ACTION",
    gameCode: "gomoku",
    roomCode: activeRoomCode.value,
    payload: JSON.stringify(payload),
    timestamp: new Date().toISOString()
  });
}

function applyMove(row: number, col: number, stone: Exclude<Stone, null>) {
  if (board.value[row][col] || winner.value) {
    return;
  }
  board.value[row][col] = stone;
  moveHistory.value.push({ row, col, stone });
  if (checkWinner(row, col, stone)) {
    winner.value = stone;
    pushFeed(`${stone} wins the round.`);
  } else {
    currentTurn.value = stone === "black" ? "white" : "black";
  }
  renderBoard();
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
  renderBoard();
  syncWindowHelpers();
}

function checkWinner(row: number, col: number, stone: Exclude<Stone, null>) {
  const directions = [
    [1, 0],
    [0, 1],
    [1, 1],
    [1, -1]
  ];
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

function renderBoard() {
  const ctx = ctxRef.value;
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
  let parsed: WsEnvelope | null = null;
  try {
    parsed = JSON.parse(event.data) as WsEnvelope;
  } catch {
    pushFeed(String(event.data));
    return;
  }

  if (parsed.type === "PLAYER_ACTION" && parsed.payload) {
    const payload = JSON.parse(parsed.payload) as PlayerActionPayload;
    if (payload.type === "move") {
      applyMove(payload.row, payload.col, payload.stone);
    }
    return;
  }

  if (parsed.type === "MATCH_START") {
    if (parsed.matchCode) {
      matchCode.value = parsed.matchCode;
    }
    if (parsed.payload) {
      const payload = JSON.parse(parsed.payload) as {
        playerStones?: Record<string, Exclude<Stone, null>>;
        matchCode?: string;
      };
      if (payload.matchCode) {
        matchCode.value = payload.matchCode;
      }
      if (currentUser.value && payload.playerStones) {
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
    const payload = JSON.parse(parsed.payload) as {
      state: {
        currentTurn?: Exclude<Stone, null>;
        winner?: string | null;
        moves?: Array<{ row: number; col: number; stone: Exclude<Stone, null> }>;
      };
      playerStones?: Record<string, Exclude<Stone, null>>;
    };
    applyServerState({
      ...payload.state,
      playerStones: payload.playerStones
    });
    return;
  }

  if (parsed.type === "MATCH_END" && parsed.payload) {
    const result = JSON.parse(parsed.payload) as { winnerStone?: string | null };
    winner.value = result.winnerStone ?? winner.value;
    pushFeed(`Match finished. Winner: ${winner.value || "n/a"}`);
    renderBoard();
    syncWindowHelpers();
    return;
  }

  if ((parsed as any).roomCode && (parsed as any).players) {
    const roomState = parsed as unknown as {
      roomCode: string;
      roomStatus: number;
      currentPlayers: number;
      maxPlayers: number;
      players: Array<{ userId: number; readyStatus: number; seatNo: number }>;
    };
    roomPlayers.value = roomState.players ?? [];
    pushFeed(`Room ${roomState.roomCode}: ${roomState.currentPlayers}/${roomState.maxPlayers} players`);
    return;
  }

  if (parsed.type === "ERROR") {
    pushFeed(`Error: ${parsed.payload ?? "unknown"}`);
    return;
  }

  if (parsed.type === "HEARTBEAT") {
    return;
  }

  pushFeed(`WS ${parsed.type}`);
}

function syncWindowHelpers() {
  (window as any).render_game_to_text = () => JSON.stringify({
    mode: winner.value ? "finished" : "playing",
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
    renderBoard();
    return Promise.resolve();
  };
}

function pause(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

onMounted(() => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  ctxRef.value = canvas.getContext("2d");
  renderBoard();
  syncWindowHelpers();

  socketClient.onOpen(() => {
    socketConnected.value = true;
    pushFeed("WebSocket connected.");
  });
  socketClient.onClose(() => {
    socketConnected.value = false;
    pushFeed("WebSocket disconnected.");
  });
  socketClient.onMessage(handleSocketMessage);
});

onBeforeUnmount(() => {
  socketClient.disconnect();
  delete (window as any).render_game_to_text;
  delete (window as any).advanceTime;
});
</script>

<style scoped>
.gomoku-page {
  min-height: 100vh;
  padding: 28px;
  color: #2a1908;
  background:
    radial-gradient(circle at top left, rgba(255, 231, 176, 0.9), transparent 32%),
    radial-gradient(circle at top right, rgba(225, 160, 86, 0.42), transparent 28%),
    linear-gradient(135deg, #f8edd0 0%, #deb87a 52%, #c88c53 100%);
  font-family: Georgia, "Times New Roman", serif;
}

.hero,
.workspace {
  display: grid;
  gap: 24px;
}

.hero {
  grid-template-columns: minmax(0, 1.6fr) minmax(300px, 0.9fr);
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 13px;
  letter-spacing: 0.28em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: clamp(42px, 5vw, 72px);
  line-height: 0.92;
}

.subtitle {
  max-width: 720px;
  font-size: 18px;
  line-height: 1.6;
}

.workspace {
  grid-template-columns: 320px minmax(0, 1fr);
  align-items: start;
}

.panel,
.auth-card,
.board-frame {
  border: 1px solid rgba(72, 38, 10, 0.18);
  border-radius: 24px;
  background: rgba(255, 247, 229, 0.76);
  box-shadow: 0 18px 40px rgba(88, 50, 16, 0.18);
  backdrop-filter: blur(8px);
}

.auth-card,
.panel {
  padding: 18px;
}

.panel + .panel {
  margin-top: 18px;
}

.row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 12px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
  font-size: 14px;
}

input,
button {
  border-radius: 14px;
  border: 1px solid rgba(67, 30, 0, 0.15);
  font: inherit;
}

input {
  padding: 11px 12px;
  background: rgba(255, 252, 245, 0.88);
}

button {
  padding: 11px 16px;
  color: #fff6e2;
  background: linear-gradient(135deg, #3f1f09, #87501f);
  cursor: pointer;
}

button.ghost {
  color: #4a2509;
  background: rgba(255, 249, 236, 0.9);
}

button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.hint {
  margin: 14px 0 0;
  font-size: 13px;
  line-height: 1.5;
}

.stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 14px 0 0;
}

.stats div {
  padding: 10px 12px;
  border-radius: 16px;
  background: rgba(255, 253, 245, 0.78);
}

dt {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

dd {
  margin: 4px 0 0;
  font-size: 18px;
}

.feed {
  margin: 12px 0 0;
  padding-left: 18px;
  line-height: 1.55;
}

.board-shell {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.board-frame {
  padding: 18px;
}

canvas {
  width: min(100%, 720px);
  height: auto;
  display: block;
  margin: 0 auto;
  border-radius: 18px;
  box-shadow: inset 0 0 0 1px rgba(73, 41, 14, 0.16);
}

.legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  padding: 0 10px;
  font-size: 15px;
}

@media (max-width: 980px) {
  .hero,
  .workspace {
    grid-template-columns: 1fr;
  }
}
</style>
