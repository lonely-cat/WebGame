<template>
  <div class="gomoku-page">
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">Board Game Room Flow</p>
        <h1>Gomoku now runs as a proper room-to-match experience.</h1>
        <p class="subtitle">
          We keep login, room staging, ready checks, and the live board in one route for now,
          but the interface is split into clear phases so the same pattern can be reused for
          Chinese Chess and Draw and Guess.
        </p>
      </div>
      <div class="phase-card">
        <p class="phase-label">Current Phase</p>
        <strong>{{ phaseTitle }}</strong>
        <p>{{ phaseDescription }}</p>
        <div class="phase-pills">
          <span :class="phaseClass('auth')">1. Login</span>
          <span :class="phaseClass('room')">2. Room</span>
          <span :class="phaseClass('match')">3. Match</span>
        </div>
      </div>
    </section>

    <section class="workspace">
      <aside class="sidebar">
        <article class="panel auth-panel">
          <div class="panel-heading">
            <div>
              <p class="kicker">Account</p>
              <h2>Sign in for the room</h2>
            </div>
            <span class="badge" :class="token ? 'online' : 'offline'">
              {{ token ? 'signed in' : 'guest' }}
            </span>
          </div>

          <label>
            Username
            <input id="login-username" v-model="loginForm.username" />
          </label>
          <label>
            Password
            <input id="login-password" v-model="loginForm.password" type="password" />
          </label>
          <div class="row">
            <button id="login-btn" @click="login" :disabled="loading.login">
              {{ loading.login ? 'Signing In...' : 'Login' }}
            </button>
            <button id="quick-start-btn" class="ghost" @click="quickStart" :disabled="loading.login || loading.roomCreate">
              Quick Start
            </button>
          </div>
          <button class="ghost wide" @click="registerTestUser" :disabled="loading.register">
            {{ loading.register ? 'Creating...' : 'Create Test User' }}
          </button>
          <p class="hint">{{ authHint }}</p>
        </article>

        <article class="panel room-panel">
          <div class="panel-heading">
            <div>
              <p class="kicker">Room Setup</p>
              <h2>{{ inMatch ? 'Match is live' : 'Prepare the table' }}</h2>
            </div>
            <span class="badge" :class="socketConnected ? 'online' : 'offline'">
              {{ socketConnected ? 'socket ready' : 'socket offline' }}
            </span>
          </div>

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
            <button id="start-btn" class="ghost" @click="startMatch" :disabled="!canStartMatch">Start</button>
          </div>

          <dl class="stats compact">
            <div><dt>User</dt><dd>{{ currentUser?.username ?? '-' }}</dd></div>
            <div><dt>Room</dt><dd id="active-room-code">{{ activeRoomCode || '-' }}</dd></div>
            <div><dt>Match</dt><dd id="active-match-code">{{ matchCode || '-' }}</dd></div>
            <div><dt>Stone</dt><dd id="my-stone">{{ myStoneLabel }}</dd></div>
          </dl>

          <div class="roster">
            <div class="seat" :class="seatTone(0)">
              <p class="seat-label">Black Seat</p>
              <strong>{{ blackPlayerName }}</strong>
              <span>{{ seatStatus(0) }}</span>
            </div>
            <div class="seat" :class="seatTone(1)">
              <p class="seat-label">White Seat</p>
              <strong>{{ whitePlayerName }}</strong>
              <span>{{ seatStatus(1) }}</span>
            </div>
          </div>
        </article>

        <article class="panel asset-panel">
          <div class="panel-heading tight">
            <div>
              <p class="kicker">Player Snapshot</p>
              <h2>Carry platform data into the match</h2>
            </div>
          </div>
          <dl class="stats">
            <div><dt>Coins</dt><dd>{{ asset.coin }}</dd></div>
            <div><dt>Score</dt><dd>{{ asset.score }}</dd></div>
            <div><dt>Turn</dt><dd>{{ currentTurnLabel }}</dd></div>
            <div><dt>Winner</dt><dd>{{ winner || '-' }}</dd></div>
          </dl>
        </article>
      </aside>

      <main class="main-stage">
        <article class="board-panel panel">
          <div class="panel-heading board-heading">
            <div>
              <p class="kicker">Live Table</p>
              <h2>{{ boardHeading }}</h2>
            </div>
            <span class="board-badge">{{ boardStatus }}</span>
          </div>

          <p class="board-copy">{{ boardCopy }}</p>

          <div class="board-frame" :class="{ waiting: !inMatch }">
            <canvas ref="canvasRef" width="720" height="720" @click="handleCanvasClick" />
            <div v-if="!inMatch" class="board-overlay">
              <strong>Room is staging</strong>
              <span>Login, connect the socket, join the same room, and get both players ready.</span>
            </div>
          </div>

          <div class="legend">
            <span>Black: {{ blackPlayerName }}</span>
            <span>White: {{ whitePlayerName }}</span>
            <span>Turn: {{ currentTurnLabel }}</span>
            <span v-if="winner">Winner: {{ winner }}</span>
          </div>
        </article>

        <article class="panel feed-panel">
          <div class="panel-heading tight">
            <div>
              <p class="kicker">Room Feed</p>
              <h2>Everything the room just did</h2>
            </div>
          </div>
          <ul class="feed">
            <li v-for="entry in roomFeed" :key="entry.id">{{ entry.text }}</li>
          </ul>
        </article>
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

type RoomPlayer = { userId: number; readyStatus: number; seatNo: number };

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
const roomPlayers = ref<RoomPlayer[]>([]);
const animationCounter = ref(0);
const matchCode = ref("");
const myStone = ref<Exclude<Stone, null> | "spectator">("spectator");

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
  match: "The server now validates every move and broadcasts the authoritative state back to both clients."
}[phase.value]));
const boardHeading = computed(() => inMatch.value ? "Authoritative live board" : "Waiting for both players");
const boardStatus = computed(() => inMatch.value ? `${currentTurn.value} to move` : "staging room");
const boardCopy = computed(() => inMatch.value
  ? "Moves are only painted after the backend accepts them, so both browsers stay in sync."
  : "The board is visible early so players can see the table they are about to use, but input stays locked until the match starts.");
const blackPlayerName = computed(() => playerNameBySeat(0, "Seat 1"));
const whitePlayerName = computed(() => playerNameBySeat(1, "Seat 2"));
const currentTurnLabel = computed(() => winner.value ? "finished" : currentTurn.value);
const myStoneLabel = computed(() => myStone.value);
const canJoinRoom = computed(() => !!roomCodeInput.value && !!token.value && socketConnected.value);
const canStartMatch = computed(() => !!activeRoomCode.value && !!socketConnected.value && roomPlayers.value.length >= 2);

function authorizeWindowHelpers() {
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
    renderBoard();
    return Promise.resolve();
  };
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
  renderBoard();
  authorizeWindowHelpers();
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
      matchCode.value = "";
      myStone.value = "spectator";
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
  if (!inMatch.value || !activeRoomCode.value || !socketConnected.value || winner.value || myStone.value === "spectator") {
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
  authorizeWindowHelpers();
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
  authorizeWindowHelpers();
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
    authorizeWindowHelpers();
    return;
  }

  if ((parsed as any).roomCode && (parsed as any).players) {
    const roomState = parsed as unknown as {
      roomCode: string;
      roomStatus: number;
      currentPlayers: number;
      maxPlayers: number;
      players: RoomPlayer[];
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

function pause(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

onMounted(() => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  ctxRef.value = canvas.getContext("2d");
  renderBoard();
  authorizeWindowHelpers();

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
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.9fr);
  margin-bottom: 24px;
}

.hero-copy {
  padding: 8px 0;
}

.eyebrow,
.kicker,
.phase-label,
.seat-label {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

h1,
h2,
p,
strong {
  margin-top: 0;
}

h1 {
  margin-bottom: 12px;
  font-size: clamp(42px, 5vw, 72px);
  line-height: 0.94;
}

h2 {
  margin-bottom: 0;
  font-size: 24px;
}

.subtitle,
.board-copy,
.phase-card p,
.hint {
  line-height: 1.6;
}

.subtitle {
  max-width: 760px;
  font-size: 18px;
}

.phase-card,
.panel,
.board-frame {
  border: 1px solid rgba(72, 38, 10, 0.18);
  border-radius: 24px;
  background: rgba(255, 247, 229, 0.76);
  box-shadow: 0 18px 40px rgba(88, 50, 16, 0.18);
  backdrop-filter: blur(8px);
}

.phase-card,
.panel {
  padding: 20px;
}

.phase-card strong {
  display: block;
  font-size: 26px;
  margin-bottom: 8px;
}

.phase-pills {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.pill {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 251, 242, 0.75);
  color: rgba(74, 37, 9, 0.72);
}

.pill.active {
  background: linear-gradient(135deg, #4a240a, #9a5f27);
  color: #fff4e0;
}

.pill.complete {
  background: rgba(112, 76, 31, 0.16);
  color: #4a2509;
}

.workspace {
  grid-template-columns: 340px minmax(0, 1fr);
  align-items: start;
}

.sidebar,
.main-stage {
  display: grid;
  gap: 18px;
}

.panel-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.panel-heading.tight {
  margin-bottom: 10px;
}

.badge,
.board-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.badge.online,
.board-badge {
  color: #fff5df;
  background: linear-gradient(135deg, #3f1f09, #87501f);
}

.badge.offline {
  color: #4a2509;
  background: rgba(255, 249, 236, 0.9);
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

button.wide {
  width: 100%;
  margin-top: 12px;
}

button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 14px 0 0;
}

.stats.compact {
  margin-top: 18px;
}

.stats div,
.seat {
  padding: 12px;
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

.roster {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.seat strong,
.seat span {
  display: block;
}

.seat.ready {
  background: linear-gradient(180deg, rgba(255, 252, 245, 0.95), rgba(241, 220, 180, 0.72));
}

.seat.joined {
  background: rgba(255, 249, 239, 0.84);
}

.seat.empty {
  opacity: 0.72;
}

.main-stage {
  grid-template-rows: auto auto;
}

.board-panel {
  overflow: hidden;
}

.board-frame {
  position: relative;
  padding: 18px;
}

.board-frame.waiting {
  filter: saturate(0.9);
}

canvas {
  width: min(100%, 720px);
  height: auto;
  display: block;
  margin: 0 auto;
  border-radius: 18px;
  box-shadow: inset 0 0 0 1px rgba(73, 41, 14, 0.16);
}

.board-overlay {
  position: absolute;
  inset: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(44, 23, 8, 0.12), rgba(44, 23, 8, 0.36));
  color: #fff8ed;
  text-align: center;
  padding: 24px;
}

.board-overlay strong {
  font-size: 28px;
}

.legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  padding-top: 14px;
  font-size: 15px;
}

.feed {
  margin: 0;
  padding-left: 18px;
  line-height: 1.65;
}

@media (max-width: 1080px) {
  .workspace {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
