import { computed, ref } from "vue";
import { useMultiplayerRoomSession } from "../../composables/useMultiplayerRoomSession";
import {
  wsMessageTypes,
  parsePayload,
  type ClientWsMessage,
  type GameStateSyncPayload,
  type MoveActionPayload,
  type ServerWsEnvelope
} from "../../websocket/gameProtocol";

type Stone = "black" | "white" | null;

const boardSize = 15;
const canvasSize = 720;
const padding = 48;
const cell = (canvasSize - padding * 2) / (boardSize - 1);

const board = ref<Stone[][]>(createEmptyBoard());
const moveHistory = ref<Array<{ row: number; col: number; stone: Exclude<Stone, null> }>>([]);
const currentTurn = ref<Exclude<Stone, null>>("black");
const animationCounter = ref(0);

const roomSession = useMultiplayerRoomSession("gomoku", {
  testUserPrefix: "gomoku",
  customMessageHandler: handleGomokuMessage
});

const myStone = computed(() => {
  const role = roomSession.roleLabel.value;
  return role === "black" || role === "white" ? role : "spectator";
});
const boardHeading = computed(() => roomSession.inMatch.value ? "Authoritative live board" : "Waiting for both players");
const boardStatus = computed(() => roomSession.inMatch.value ? `${currentTurn.value} to move` : "staging room");
const boardCopy = computed(() => roomSession.inMatch.value
  ? "Moves are only painted after the backend accepts them, so both browsers stay in sync."
  : "The board becomes interactive only after both players are seated and the match starts.");
const currentTurnLabel = computed(() => roomSession.winner.value ? "finished" : currentTurn.value);
const myStoneLabel = computed(() => myStone.value);
const blackPlayerName = computed(() => roomSession.playerNameBySeat(0, "Seat 1"));
const whitePlayerName = computed(() => roomSession.playerNameBySeat(1, "Seat 2"));

function useGomokuSession() {
  syncWindowHelpers();
  return {
    boardSize,
    canvasSize,
    padding,
    cell,
    board,
    moveHistory,
    currentTurn,
    myStone,
    boardHeading,
    boardStatus,
    boardCopy,
    currentTurnLabel,
    myStoneLabel,
    blackPlayerName,
    whitePlayerName,
    handleBoardClick,
    renderBoardToCanvas,
    resetBoard,
    syncWindowHelpers,
    ...roomSession
  };
}

function createEmptyBoard(): Stone[][] {
  return Array.from({ length: boardSize }, () => Array.from({ length: boardSize }, () => null));
}

function resetBoard() {
  board.value = createEmptyBoard();
  moveHistory.value = [];
  currentTurn.value = "black";
  roomSession.winner.value = "";
  syncWindowHelpers();
}

function handleBoardClick(event: MouseEvent, target: HTMLCanvasElement | null) {
  if (!roomSession.inMatch.value || !roomSession.activeRoomCode.value || !roomSession.socketConnected.value || roomSession.winner.value || myStone.value === "spectator") {
    return;
  }
  if (myStone.value !== currentTurn.value) {
    roomSession.pushFeed(`It is ${currentTurn.value}'s turn.`);
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
  roomSession.startMatch;
  sendMove(row, col, currentTurn.value);
}

function sendMove(row: number, col: number, stone: Exclude<Stone, null>) {
  const payload: MoveActionPayload = { type: "move", row, col, stone };
  const message: ClientWsMessage<MoveActionPayload> = {
    type: wsMessageTypes.playerAction,
    gameCode: "gomoku",
    roomCode: roomSession.activeRoomCode.value,
    payload,
    timestamp: new Date().toISOString()
  };
  roomSession.sendClientMessage(message);
}

function applyMove(row: number, col: number, stone: Exclude<Stone, null>) {
  if (board.value[row][col] || roomSession.winner.value) {
    return;
  }
  board.value[row][col] = stone;
  moveHistory.value.push({ row, col, stone });
  if (checkWinner(row, col, stone)) {
    roomSession.winner.value = stone;
    roomSession.pushFeed(`${stone} wins the round.`);
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
  for (const move of serverState.moves ?? []) {
    if (!board.value[move.row][move.col]) {
      board.value[move.row][move.col] = move.stone;
      moveHistory.value.push(move);
    }
  }
  currentTurn.value = serverState.currentTurn ?? currentTurn.value;
  roomSession.winner.value = serverState.winner ?? "";
  syncWindowHelpers();
}

function handleGomokuMessage(parsed: ServerWsEnvelope, helpers: {
  currentUser: typeof roomSession.currentUser;
  activeRoomCode: typeof roomSession.activeRoomCode;
  roomPlayers: typeof roomSession.roomPlayers;
  matchCode: typeof roomSession.matchCode;
  roleLabel: typeof roomSession.roleLabel;
  winner: typeof roomSession.winner;
  currentTurnLabel: typeof roomSession.currentTurnLabel;
  pushFeed: (text: string) => void;
  syncWindowHelpers: () => void;
}) {
  if (parsed.type === wsMessageTypes.playerAction && parsed.payload) {
    const payload = parsePayload<MoveActionPayload>(parsed.payload);
    if (payload?.type === "move") {
      applyMove(payload.row, payload.col, payload.stone);
    }
    return true;
  }
  if (parsed.type === wsMessageTypes.matchStart) {
    resetBoard();
    return false;
  }
  if (parsed.type === wsMessageTypes.gameStateSync && parsed.payload) {
    if (parsed.matchCode) {
      helpers.matchCode.value = parsed.matchCode;
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
    return true;
  }
  return false;
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

function syncWindowHelpers() {
  (window as any).render_game_to_text = () => JSON.stringify({
    mode: roomSession.winner.value ? "finished" : roomSession.inMatch.value ? "playing" : "room",
    phase: roomSession.phase.value,
    note: "origin is top-left, rows grow downward, cols grow rightward",
    roomCode: roomSession.activeRoomCode.value || null,
    matchCode: roomSession.matchCode.value || null,
    myStone: myStone.value,
    socketConnected: roomSession.socketConnected.value,
    roomPlayers: roomSession.roomPlayers.value,
    turn: currentTurn.value,
    winner: roomSession.winner.value || null,
    moves: moveHistory.value,
    latestMove: moveHistory.value.at(-1) ?? null
  });

  (window as any).advanceTime = (ms: number) => {
    animationCounter.value += ms;
    return Promise.resolve();
  };
}

export { useGomokuSession };
