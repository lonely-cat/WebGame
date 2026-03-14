<template>
  <MultiplayerTwoColumn variant="match">
    <template #sidebar>
      <article class="panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Live Match</p>
            <h2>Draw and Guess match shell</h2>
          </div>
          <button class="ghost" @click="goToRoom">Back To Room</button>
        </div>
        <p class="copy">
          The dedicated party-game stage is now live. Next we will wire prompt assignment, timed rounds,
          live brush strokes, and guess submission.
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
            <h2>Sketch round placeholder</h2>
          </div>
          <span class="badge">{{ inMatch ? "match live" : "waiting" }}</span>
        </div>
        <div class="canvas-placeholder">
          <div class="word-chip">Prompt hidden</div>
          <div class="canvas-surface">
            <div class="scribble scribble-a"></div>
            <div class="scribble scribble-b"></div>
            <div class="scribble scribble-c"></div>
          </div>
          <p>
            This stage is ready for the real game loop. The next iteration will add drawing sync, round timers,
            guess input, and scoring.
          </p>
        </div>
      </article>
    </template>
  </MultiplayerTwoColumn>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import MultiplayerTwoColumn from "../../components/multiplayer/MultiplayerTwoColumn.vue";
import { useMultiplayerRoomSession } from "../../composables/useMultiplayerRoomSession";

const router = useRouter();
const {
  roomFeed,
  roleLabel,
  winner,
  currentTurnLabel,
  roomPlayers,
  inMatch
} = useMultiplayerRoomSession("draw-guess", {
  testUserPrefix: "drawguess"
});

function goToRoom() {
  router.push("/games/draw-guess/room");
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

.badge,
.word-chip {
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

.canvas-placeholder {
  min-height: 520px;
  display: grid;
  gap: 20px;
  align-content: start;
}

.word-chip {
  justify-self: start;
  color: #4a2509;
  background: rgba(255, 249, 236, 0.92);
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

.scribble {
  position: absolute;
  border-radius: 999px;
  opacity: 0.65;
}

.scribble-a {
  top: 58px;
  left: 62px;
  width: 220px;
  height: 16px;
  background: #d2553f;
  transform: rotate(-8deg);
}

.scribble-b {
  top: 148px;
  right: 84px;
  width: 180px;
  height: 14px;
  background: #29507a;
  transform: rotate(19deg);
}

.scribble-c {
  bottom: 88px;
  left: 140px;
  width: 260px;
  height: 18px;
  background: #2f6b44;
  transform: rotate(11deg);
}

.feed {
  margin: 0;
  padding-left: 18px;
  line-height: 1.65;
}
</style>
