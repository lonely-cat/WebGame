<template>
  <MultiplayerTwoColumn variant="match">
    <template #sidebar>
      <article class="panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Live Match</p>
            <h2>Draw and Guess round</h2>
          </div>
          <button class="ghost" @click="goToRoom">Back To Room</button>
        </div>
        <p class="copy">
          The first playable round is here: the drawer sees the prompt, everyone else sees the mask,
          strokes sync through the room, and guesses are checked on the server.
        </p>
        <dl class="stats">
          <div><dt>Role</dt><dd>{{ roleLabel }}</dd></div>
          <div><dt>Turn</dt><dd>{{ currentTurnLabel }}</dd></div>
          <div><dt>Winner</dt><dd>{{ winner || "-" }}</dd></div>
          <div><dt>Players</dt><dd>{{ roomPlayers.length }}</dd></div>
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
      <article class="panel canvas-panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Canvas Stage</p>
            <h2>Live sketch round</h2>
          </div>
          <span class="badge">{{ inMatch ? "match live" : "waiting" }}</span>
        </div>
        <div class="canvas-placeholder">
          <div class="top-strip">
            <div class="word-chip">Prompt: {{ displayPrompt }}</div>
            <div class="drawer-chip">Drawer: {{ drawerLabel }}</div>
          </div>
          <div class="canvas-surface">
            <canvas
              ref="canvasRef"
              width="800"
              height="420"
              @mousedown="beginStroke"
              @mousemove="trackStroke"
              @mouseup="finishStroke"
              @mouseleave="finishStroke"
            />
          </div>
          <div class="guess-row">
            <input
              v-model="guessInput"
              :disabled="isDrawer || !inMatch"
              placeholder="Type a guess and press submit"
              @keydown.enter.prevent="submitGuess"
            />
            <button :disabled="isDrawer || !inMatch" @click="submitGuess">Submit Guess</button>
          </div>
          <ul class="guess-feed">
            <li v-for="entry in guesses.slice().reverse()" :key="`${entry.userId}-${entry.guess}-${entryIndex(entry)}`">
              User #{{ entry.userId }} guessed "{{ entry.guess }}"
            </li>
          </ul>
        </div>
      </article>
    </template>
  </MultiplayerTwoColumn>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import MultiplayerTwoColumn from "../../components/multiplayer/MultiplayerTwoColumn.vue";
import { useDrawGuessSession } from "./useDrawGuessSession";

const router = useRouter();
const {
  roomFeed,
  roleLabel,
  winner,
  currentTurnLabel,
  roomPlayers,
  inMatch,
  strokes,
  drawerUserId,
  displayPrompt,
  isDrawer,
  guessInput,
  guesses,
  sendStroke,
  submitGuess,
  playerNameBySeat
} = useDrawGuessSession();

const canvasRef = ref<HTMLCanvasElement | null>(null);
const ctxRef = ref<CanvasRenderingContext2D | null>(null);
const draftPoints = ref<Array<{ x: number; y: number }>>([]);
const drawing = ref(false);

const drawerLabel = computed(() => {
  const userId = drawerUserId.value;
  if (!userId) {
    return playerNameBySeat(0, "Seat 1");
  }
  return `User #${userId}`;
});

function goToRoom() {
  router.push("/games/draw-guess/room");
}

function drawScene() {
  const ctx = ctxRef.value;
  const canvas = canvasRef.value;
  if (!ctx || !canvas) return;
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.fillStyle = "#fff8ef";
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.strokeStyle = "rgba(91, 55, 18, 0.18)";
  ctx.strokeRect(0, 0, canvas.width, canvas.height);

  for (const stroke of strokes.value) {
    paintStroke(ctx, stroke);
  }
  if (draftPoints.value.length > 1) {
    paintStroke(ctx, { color: "#2f6b44", width: 6, points: draftPoints.value });
  }
}

function paintStroke(ctx: CanvasRenderingContext2D, stroke: { color: string; width: number; points: Array<{ x: number; y: number }> }) {
  if (stroke.points.length < 2) return;
  ctx.beginPath();
  ctx.lineCap = "round";
  ctx.lineJoin = "round";
  ctx.strokeStyle = stroke.color;
  ctx.lineWidth = stroke.width;
  ctx.moveTo(stroke.points[0].x, stroke.points[0].y);
  for (const point of stroke.points.slice(1)) {
    ctx.lineTo(point.x, point.y);
  }
  ctx.stroke();
}

function pointFromEvent(event: MouseEvent) {
  const canvas = canvasRef.value;
  if (!canvas) return null;
  const rect = canvas.getBoundingClientRect();
  return {
    x: ((event.clientX - rect.left) / rect.width) * canvas.width,
    y: ((event.clientY - rect.top) / rect.height) * canvas.height
  };
}

function beginStroke(event: MouseEvent) {
  if (!isDrawer.value || !inMatch.value) return;
  const point = pointFromEvent(event);
  if (!point) return;
  drawing.value = true;
  draftPoints.value = [point];
  drawScene();
}

function trackStroke(event: MouseEvent) {
  if (!drawing.value) return;
  const point = pointFromEvent(event);
  if (!point) return;
  draftPoints.value = [...draftPoints.value, point];
  drawScene();
}

function finishStroke() {
  if (!drawing.value) return;
  drawing.value = false;
  if (draftPoints.value.length > 1) {
    sendStroke({
      color: "#2f6b44",
      width: 6,
      points: draftPoints.value
    });
  }
  draftPoints.value = [];
  drawScene();
}

function entryIndex(entry: { userId: number; guess: string }) {
  return guesses.value.indexOf(entry);
}

onMounted(() => {
  if (!canvasRef.value) return;
  ctxRef.value = canvasRef.value.getContext("2d");
  drawScene();
});

watch(strokes, () => {
  drawScene();
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

button,
.guess-row input {
  padding: 11px 16px;
  border-radius: 14px;
  border: 1px solid rgba(67, 30, 0, 0.15);
  font: inherit;
}

button {
  color: #4a2509;
  background: rgba(255, 249, 236, 0.9);
  cursor: pointer;
}

.badge,
.word-chip,
.drawer-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.badge {
  color: #fff5df;
  background: linear-gradient(135deg, #3f1f09, #87501f);
}

.word-chip,
.drawer-chip {
  color: #4a2509;
  background: rgba(255, 249, 236, 0.92);
}

.canvas-placeholder {
  min-height: 520px;
  display: grid;
  gap: 20px;
  align-content: start;
}

.top-strip {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.canvas-surface {
  min-height: 360px;
  position: relative;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 252, 245, 0.94), rgba(245, 228, 197, 0.9)),
    radial-gradient(circle at top left, rgba(255, 191, 122, 0.22), transparent 28%);
  overflow: hidden;
  border: 1px solid rgba(73, 41, 14, 0.12);
}

.canvas-surface canvas {
  width: 100%;
  height: auto;
  display: block;
  cursor: crosshair;
}

.guess-row {
  display: flex;
  gap: 12px;
}

.guess-row input {
  flex: 1;
  background: rgba(255, 252, 245, 0.88);
}

.guess-feed,
.feed {
  margin: 0;
  padding-left: 18px;
  line-height: 1.65;
}
</style>
