<template>
  <MultiplayerTwoColumn variant="match">
    <template #sidebar>
      <article class="panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Live Match</p>
            <h2>{{ boardHeading }}</h2>
          </div>
          <div class="top-actions">
            <button class="ghost" @click="goToRoom">Back To Room</button>
            <button class="ghost" @click="goToLobby">Back To Lobby</button>
          </div>
        </div>
        <p class="copy">{{ boardCopy }}</p>
        <dl class="stats">
          <div><dt>Black</dt><dd>{{ blackPlayerName }}</dd></div>
          <div><dt>White</dt><dd>{{ whitePlayerName }}</dd></div>
          <div><dt>Turn</dt><dd>{{ currentTurnLabel }}</dd></div>
          <div><dt>Winner</dt><dd>{{ winner || "-" }}</dd></div>
        </dl>
      </article>

      <article class="panel">
        <div class="panel-heading tight">
          <div>
            <p class="kicker">Room Feed</p>
            <h2>Live event stream</h2>
          </div>
        </div>
        <ul class="feed">
          <li v-for="entry in roomFeed" :key="entry.id">{{ entry.text }}</li>
        </ul>
      </article>
    </template>

    <template #main>
      <article class="panel board-panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Board</p>
            <h2>{{ inMatch ? "Server-authoritative board" : "Waiting for match start" }}</h2>
          </div>
          <span class="board-badge">{{ boardStatus }}</span>
        </div>

        <div class="board-frame" :class="{ waiting: !inMatch }">
          <canvas ref="canvasRef" width="720" height="720" @click="onCanvasClick" />
          <div v-if="!inMatch" class="board-overlay">
            <strong>Room is staging</strong>
            <span>The board becomes interactive once the room sends a match start event.</span>
          </div>
        </div>

        <div class="legend">
          <span>Black: {{ blackPlayerName }}</span>
          <span>White: {{ whitePlayerName }}</span>
          <span>Turn: {{ currentTurnLabel }}</span>
          <span v-if="winner">Winner: {{ winner }}</span>
        </div>
      </article>
    </template>
  </MultiplayerTwoColumn>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import MultiplayerTwoColumn from "../../components/multiplayer/MultiplayerTwoColumn.vue";
import { useGomokuSession } from "./useGomokuSession";

const router = useRouter();
const canvasRef = ref<HTMLCanvasElement | null>(null);
const ctxRef = ref<CanvasRenderingContext2D | null>(null);

const {
  roomFeed,
  inMatch,
  winner,
  boardHeading,
  boardStatus,
  boardCopy,
  currentTurnLabel,
  blackPlayerName,
  whitePlayerName,
  moveHistory,
  board,
  renderBoardToCanvas,
  handleBoardClick
} = useGomokuSession();

function draw() {
  renderBoardToCanvas(ctxRef.value);
}

function onCanvasClick(event: MouseEvent) {
  handleBoardClick(event, canvasRef.value);
  draw();
}

function goToRoom() {
  router.push("/games/gomoku/room");
}

function goToLobby() {
  router.push("/lobby");
}

onMounted(() => {
  if (!canvasRef.value) return;
  ctxRef.value = canvasRef.value.getContext("2d");
  draw();
});

watch([moveHistory, board, inMatch, winner], () => {
  draw();
}, { deep: true, immediate: true });
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

.board-panel {
  overflow: hidden;
}

.panel-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.top-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.panel-heading.tight {
  margin-bottom: 10px;
}

.kicker {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.copy {
  line-height: 1.6;
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

button {
  padding: 11px 16px;
  border-radius: 14px;
  border: 1px solid rgba(67, 30, 0, 0.15);
  color: #4a2509;
  background: rgba(255, 249, 236, 0.9);
  cursor: pointer;
  font: inherit;
}

.board-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #fff5df;
  background: linear-gradient(135deg, #3f1f09, #87501f);
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
</style>
