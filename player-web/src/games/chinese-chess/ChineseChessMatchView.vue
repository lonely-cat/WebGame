<template>
  <MultiplayerTwoColumn variant="match">
    <template #sidebar>
      <article class="panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Live Match</p>
            <h2>Xiangqi board</h2>
          </div>
          <button class="ghost" @click="goToRoom">Back To Room</button>
        </div>
        <p class="copy">
          The board is now live: pick one of your pieces, choose a target square, and let the server
          validate the move before both browsers update.
        </p>
        <dl class="stats">
          <div><dt>Role</dt><dd>{{ mySide }}</dd></div>
          <div><dt>Turn</dt><dd>{{ currentTurn }}</dd></div>
          <div><dt>Status</dt><dd>{{ statusLabel }}</dd></div>
          <div><dt>Winner</dt><dd>{{ winner || "-" }}</dd></div>
          <div><dt>End</dt><dd>{{ endReason || "-" }}</dd></div>
        </dl>
      </article>

      <article class="panel">
        <div class="panel-heading tight">
          <div>
            <p class="kicker">Selection</p>
            <h2>Move guidance</h2>
          </div>
        </div>
        <p class="selection-copy">{{ selectionLabel }}</p>
        <p v-if="statusTone === 'check'" class="check-banner">Alert: {{ statusLabel }}</p>
        <p class="river-copy">楚河汉界</p>
      </article>

      <article class="panel">
        <div class="panel-heading tight">
          <div>
            <p class="kicker">Move Feed</p>
            <h2>Recent actions</h2>
          </div>
        </div>
        <ul class="feed">
          <li v-for="entry in moveHistory.slice().reverse().slice(0, 8)" :key="`${entry.fromRow}-${entry.fromCol}-${entry.toRow}-${entry.toCol}-${entry.piece}`">
            {{ describeMove(entry) }}
          </li>
        </ul>
      </article>
    </template>

    <template #main>
      <article class="panel board-panel">
        <div class="panel-heading">
          <div>
            <p class="kicker">Board Stage</p>
            <h2>Authoritative live board</h2>
          </div>
          <span class="badge">{{ roomPlayers.length }} players</span>
        </div>

        <div class="board-shell">
          <div class="river-band">楚河 汉界</div>
          <div class="board-grid">
            <button
              v-for="cell in boardCells"
              :key="`${cell.row}-${cell.col}`"
              :data-row="cell.row"
              :data-col="cell.col"
              :id="cell.row === 6 && cell.col === 0 ? 'chess-origin-cell' : undefined"
              :class="cellClasses(cell.row, cell.col)"
              @click="clickCell(cell.row, cell.col)"
            >
              <span v-if="board[cell.row][cell.col]" class="piece-token">
                {{ pieceLabel(board[cell.row][cell.col]) }}
              </span>
            </button>
          </div>
        </div>
      </article>
    </template>
  </MultiplayerTwoColumn>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import MultiplayerTwoColumn from "../../components/multiplayer/MultiplayerTwoColumn.vue";
import { useChineseChessSession } from "./useChineseChessSession";

const router = useRouter();
const {
  boardRows,
  boardCols,
  board,
  moveHistory,
  currentTurn,
  winner,
  endReason,
  mySide,
  roomPlayers,
  statusLabel,
  statusTone,
  selectionLabel,
  clickCell,
  cellClasses,
  pieceLabel
} = useChineseChessSession();

const boardCells = computed(() =>
  Array.from({ length: boardRows * boardCols }, (_, index) => ({
    row: Math.floor(index / boardCols),
    col: index % boardCols
  }))
);

function goToRoom() {
  router.push("/games/chinese-chess/room");
}

function describeMove(entry: {
  fromRow: number;
  fromCol: number;
  toRow: number;
  toCol: number;
  piece: string;
  captured?: string;
}) {
  const action = entry.captured ? `captured ${entry.captured}` : "moved";
  return `${entry.piece} ${action} to (${entry.toRow}, ${entry.toCol})`;
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

.copy,
.selection-copy {
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
  border-radius: 14px;
  border: 1px solid rgba(67, 30, 0, 0.15);
  font: inherit;
}

.ghost {
  padding: 11px 16px;
  color: #4a2509;
  background: rgba(255, 249, 236, 0.9);
  cursor: pointer;
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

.river-copy {
  margin: 16px 0 0;
  font-size: 24px;
  letter-spacing: 0.24em;
  text-align: center;
  color: #7a4718;
}

.check-banner {
  margin: 14px 0 0;
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(255, 230, 226, 0.95);
  border: 1px solid rgba(176, 58, 38, 0.28);
  color: #9e2c1c;
  font-weight: 600;
}

.board-panel {
  overflow: hidden;
}

.board-shell {
  position: relative;
  padding: 22px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(255, 205, 132, 0.28), transparent 28%),
    linear-gradient(180deg, rgba(255, 249, 234, 0.97), rgba(238, 214, 171, 0.92));
  border: 1px solid rgba(73, 41, 14, 0.12);
}

.river-band {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
  font-size: 28px;
  letter-spacing: 0.24em;
  color: rgba(112, 64, 24, 0.72);
}

.board-grid {
  display: grid;
  grid-template-columns: repeat(9, minmax(0, 1fr));
  gap: 6px;
}

.cell {
  aspect-ratio: 1;
  min-height: 58px;
  background:
    linear-gradient(180deg, rgba(255, 253, 247, 0.78), rgba(243, 224, 192, 0.94));
  border: 1px solid rgba(95, 58, 20, 0.22);
  border-radius: 18px;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: transform 120ms ease, box-shadow 120ms ease, border-color 120ms ease;
}

.cell:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(84, 48, 12, 0.12);
}

.cell.selected {
  border-color: rgba(188, 114, 28, 0.9);
  box-shadow: 0 0 0 3px rgba(228, 165, 80, 0.24);
}

.piece-token {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-size: 28px;
  font-weight: 700;
  background: rgba(255, 250, 240, 0.92);
  border: 2px solid rgba(105, 63, 24, 0.2);
}

.cell.red .piece-token {
  color: #b43420;
}

.cell.black .piece-token {
  color: #1f1f1f;
}

.feed {
  margin: 0;
  padding-left: 18px;
  line-height: 1.65;
}
</style>

