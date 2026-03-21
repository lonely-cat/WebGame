<template>
  <MultiplayerTwoColumn variant="room">
    <template #sidebar>
      <article class="panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Account</p>
            <h2>Sign in for the room</h2>
          </div>
          <span class="badge" :class="token ? 'online' : 'offline'">
            {{ token ? "signed in" : "guest" }}
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
            {{ loading.login ? "Signing In..." : "Login" }}
          </button>
          <button id="quick-start-btn" class="ghost" @click="quickStart" :disabled="loading.login || loading.roomCreate">
            Quick Start
          </button>
        </div>
        <button class="ghost wide" @click="registerTestUser" :disabled="loading.register">
          {{ loading.register ? "Creating..." : "Create Test User" }}
        </button>
        <p class="hint">{{ authHint }}</p>
      </article>

      <article class="panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Room Setup</p>
            <h2>Prepare the Xiangqi table</h2>
          </div>
          <span class="badge" :class="socketConnected ? 'online' : 'offline'">
            {{ socketConnected ? "socket ready" : "socket offline" }}
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
          <button
            v-if="inMatch"
            id="resume-match-btn"
            class="ghost"
            @click="goToMatch"
          >
            Resume Match
          </button>
        </div>

        <div class="roster">
          <div class="seat" :class="seatTone(0)">
            <p class="seat-label">Red Seat</p>
            <strong>{{ playerNameBySeat(0, "Seat 1") }}</strong>
            <span>{{ seatStatus(0) }}</span>
          </div>
          <div class="seat" :class="seatTone(1)">
            <p class="seat-label">Black Seat</p>
            <strong>{{ playerNameBySeat(1, "Seat 2") }}</strong>
            <span>{{ seatStatus(1) }}</span>
          </div>
        </div>
      </article>
    </template>

    <template #main>
      <article class="panel">
        <div class="panel-heading tight">
          <div>
            <p class="kicker">Prototype Scope</p>
            <h2>Room flow and live board are ready</h2>
          </div>
        </div>
        <p class="copy">
          Chinese Chess is now on the same multiplayer rails as Gomoku: login, room staging,
          ready checks, match entry, the initial board, and first-pass legal move validation are all live.
          Next we can keep tightening Xiangqi-specific rules like check, flying generals, and endgame flow.
        </p>
        <dl class="stats">
          <div><dt>User</dt><dd>{{ currentUser?.username ?? "-" }}</dd></div>
          <div><dt>Coins</dt><dd>{{ asset.coin }}</dd></div>
          <div><dt>Score</dt><dd>{{ asset.score }}</dd></div>
          <div><dt>Status</dt><dd>{{ inMatch ? "match ready" : "waiting room" }}</dd></div>
        </dl>
      </article>

      <article class="panel">
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
    </template>
  </MultiplayerTwoColumn>
</template>

<script setup lang="ts">
import { watch } from "vue";
import { useRouter } from "vue-router";
import MultiplayerTwoColumn from "../../components/multiplayer/MultiplayerTwoColumn.vue";
import { useChineseChessSession } from "./useChineseChessSession";

const router = useRouter();
const {
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
  inMatch,
  preferRoomView,
  canJoinRoom,
  canStartMatch,
  login,
  registerTestUser,
  quickStart,
  createRoom,
  connectSocket,
  joinRoomViaSocket,
  sendReady,
  startMatch,
  resumeMatchView,
  playerNameBySeat,
  seatStatus,
  seatTone
} = useChineseChessSession();

watch(inMatch, (value) => {
  if (value && !preferRoomView.value) {
    router.push("/games/chinese-chess/match");
  }
}, { immediate: true });

function goToMatch() {
  resumeMatchView();
  router.push("/games/chinese-chess/match");
}
</script>

<style scoped>
.panel {
  border: 1px solid rgba(72, 38, 10, 0.18);
  border-radius: 24px;
  background: rgba(255, 247, 229, 0.76);
  box-shadow: 0 18px 40px rgba(88, 50, 16, 0.18);
  backdrop-filter: blur(8px);
  padding: 20px;
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

.kicker,
.seat-label {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.badge.online {
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

.hint,
.copy {
  line-height: 1.6;
}

.roster {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.seat {
  padding: 12px;
  border-radius: 16px;
  background: rgba(255, 253, 245, 0.78);
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

.stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 14px 0 0;
}

.stats div {
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

.feed {
  margin: 0;
  padding-left: 18px;
  line-height: 1.65;
}
</style>
