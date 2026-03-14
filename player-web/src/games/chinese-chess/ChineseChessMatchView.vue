<template>
  <MultiplayerTwoColumn variant="match">
    <template #sidebar>
      <article class="panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Live Match</p>
            <h2>Xiangqi match shell</h2>
          </div>
          <button class="ghost" @click="goToRoom">Back To Room</button>
        </div>
        <p class="copy">
          This is the dedicated match view. The multiplayer shell, match metadata, and room feed
          are already live; the next iteration will render the board and piece movement.
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
      <article class="panel board-panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Board Placeholder</p>
            <h2>Chinese Chess board UI is next</h2>
          </div>
          <span class="badge">{{ inMatch ? "match live" : "waiting" }}</span>
        </div>
        <div class="board-placeholder">
          <div class="river">楚河 汉界</div>
          <p>
            The platform-level room flow is already running for Chinese Chess.
            Next we will drop in the real board, pieces, legal move validation, and checkmate state.
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
} = useMultiplayerRoomSession("chinese-chess", {
  testUserPrefix: "xiangqi"
});

function goToRoom() {
  router.push("/games/chinese-chess/room");
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

.badge {
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

.board-placeholder {
  min-height: 520px;
  border-radius: 24px;
  border: 1px solid rgba(73, 41, 14, 0.12);
  background:
    linear-gradient(180deg, rgba(255, 249, 234, 0.96), rgba(240, 220, 185, 0.9)),
    repeating-linear-gradient(
      90deg,
      transparent 0,
      transparent 84px,
      rgba(83, 48, 15, 0.14) 84px,
      rgba(83, 48, 15, 0.14) 86px
    ),
    repeating-linear-gradient(
      180deg,
      transparent 0,
      transparent 52px,
      rgba(83, 48, 15, 0.14) 52px,
      rgba(83, 48, 15, 0.14) 54px
    );
  display: grid;
  place-items: center;
  text-align: center;
  padding: 32px;
}

.river {
  font-size: 38px;
  letter-spacing: 0.3em;
  margin-bottom: 18px;
}

.feed {
  margin: 0;
  padding-left: 18px;
  line-height: 1.65;
}
</style>
