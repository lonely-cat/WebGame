import { computed, ref } from "vue";
import { useMultiplayerRoomSession } from "../../composables/useMultiplayerRoomSession";
import {
  parsePayload,
  type ClientWsMessage,
  type GameStateSyncPayload,
  type MatchEndPayload,
  type MatchStartPayload,
  type ServerWsEnvelope,
  wsMessageTypes
} from "../../websocket/gameProtocol";

type Side = "red" | "black";
type PieceCode =
  | "rook"
  | "horse"
  | "elephant"
  | "advisor"
  | "general"
  | "cannon"
  | "pawn";
type Piece = { side: Side; kind: PieceCode } | null;
type MoveRecord = {
  fromRow: number;
  fromCol: number;
  toRow: number;
  toCol: number;
  piece: string;
  captured?: string;
};

const boardRows = 10;
const boardCols = 9;

const board = ref<Piece[][]>(createEmptyBoard());
const selectedCell = ref<{ row: number; col: number } | null>(null);
const moveHistory = ref<MoveRecord[]>([]);
const currentTurn = ref<Side>("red");
const winner = ref<string>("");

const roomSession = useMultiplayerRoomSession("chinese-chess", {
  testUserPrefix: "xiangqi",
  customMessageHandler: handleChineseChessMessage
});

const mySide = computed(() => roomSession.roleLabel.value === "red" || roomSession.roleLabel.value === "black"
  ? roomSession.roleLabel.value as Side
  : "spectator");
const canInteract = computed(() =>
  roomSession.inMatch.value &&
  roomSession.socketConnected.value &&
  !winner.value &&
  mySide.value !== "spectator"
);
const statusLabel = computed(() => winner.value ? `${winner.value} wins` : `${currentTurn.value} to move`);
const selectionLabel = computed(() => {
  if (!selectedCell.value) {
    return "Select one of your pieces to move.";
  }
  const piece = board.value[selectedCell.value.row][selectedCell.value.col];
  if (!piece) {
    return "Select one of your pieces to move.";
  }
  return `${piece.side} ${piece.kind} selected`;
});
const boardPieces = computed(() =>
  board.value.flatMap((row, rowIndex) =>
    row.flatMap((piece, colIndex) => piece ? [{ row: rowIndex, col: colIndex, ...piece }] : [])
  )
);

function useChineseChessSession() {
  syncWindowHelpers();
  return {
    boardRows,
    boardCols,
    board,
    selectedCell,
    moveHistory,
    currentTurn,
    winner,
    mySide,
    canInteract,
    statusLabel,
    selectionLabel,
    boardPieces,
    clickCell,
    cellClasses,
    pieceLabel,
    resetBoard,
    ...roomSession
  };
}

function createEmptyBoard(): Piece[][] {
  return Array.from({ length: boardRows }, () => Array.from({ length: boardCols }, () => null));
}

function parsePiece(value: string | null | undefined): Piece {
  if (!value || !value.includes("-")) {
    return null;
  }
  const [side, kind] = value.split("-");
  return { side: side as Side, kind: kind as PieceCode };
}

function applyServerState(state: {
  board?: Array<Array<string | null>>;
  currentTurn?: Side;
  winner?: string | null;
  moves?: MoveRecord[];
}) {
  board.value = (state.board ?? createEmptyBoard()).map((row) => row.map((cell) => parsePiece(cell)));
  currentTurn.value = state.currentTurn ?? "red";
  winner.value = state.winner ?? "";
  moveHistory.value = state.moves ?? [];
  if (winner.value) {
    roomSession.winner.value = winner.value;
  }
  syncWindowHelpers();
}

function resetBoard() {
  board.value = createEmptyBoard();
  selectedCell.value = null;
  moveHistory.value = [];
  currentTurn.value = "red";
  winner.value = "";
  roomSession.winner.value = "";
  syncWindowHelpers();
}

function clickCell(row: number, col: number) {
  if (!canInteract.value) {
    return;
  }
  const piece = board.value[row][col];
  if (selectedCell.value) {
    const selectedPiece = board.value[selectedCell.value.row][selectedCell.value.col];
    if (piece && piece.side === mySide.value) {
      selectedCell.value = { row, col };
      syncWindowHelpers();
      return;
    }
    if (selectedPiece && currentTurn.value === mySide.value) {
      sendMove(selectedCell.value.row, selectedCell.value.col, row, col);
      selectedCell.value = null;
      syncWindowHelpers();
      return;
    }
  }
  if (piece && piece.side === mySide.value && currentTurn.value === mySide.value) {
    selectedCell.value = { row, col };
    syncWindowHelpers();
  }
}

function sendMove(fromRow: number, fromCol: number, toRow: number, toCol: number) {
  const message: ClientWsMessage<{
    type: "move";
    fromRow: number;
    fromCol: number;
    toRow: number;
    toCol: number;
  }> = {
    type: wsMessageTypes.playerAction,
    gameCode: "chinese-chess",
    roomCode: roomSession.activeRoomCode.value,
    payload: { type: "move", fromRow, fromCol, toRow, toCol },
    timestamp: new Date().toISOString()
  };
  roomSession.sendClientMessage(message);
}

function pieceLabel(piece: Piece) {
  if (!piece) {
    return "";
  }
  const labels: Record<Side, Record<PieceCode, string>> = {
    red: {
      rook: "俥",
      horse: "傌",
      elephant: "相",
      advisor: "仕",
      general: "帅",
      cannon: "炮",
      pawn: "兵"
    },
    black: {
      rook: "車",
      horse: "馬",
      elephant: "象",
      advisor: "士",
      general: "将",
      cannon: "砲",
      pawn: "卒"
    }
  };
  return labels[piece.side][piece.kind];
}

function cellClasses(row: number, col: number) {
  const piece = board.value[row][col];
  return {
    cell: true,
    selected: selectedCell.value?.row === row && selectedCell.value?.col === col,
    occupied: !!piece,
    red: piece?.side === "red",
    black: piece?.side === "black"
  };
}

function handleChineseChessMessage(parsed: ServerWsEnvelope, helpers: {
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
  if (parsed.type === wsMessageTypes.matchStart && parsed.payload) {
    resetBoard();
    const payload = parsePayload<MatchStartPayload>(parsed.payload);
    if (payload?.playerStones && helpers.currentUser.value) {
      helpers.roleLabel.value = payload.playerStones[String(helpers.currentUser.value.userId)] ?? helpers.roleLabel.value;
    }
    return false;
  }
  if (parsed.type === wsMessageTypes.gameStateSync && parsed.payload) {
    if (parsed.matchCode) {
      helpers.matchCode.value = parsed.matchCode;
    }
    const payload = parsePayload<GameStateSyncPayload<{
      board?: Array<Array<string | null>>;
      currentTurn?: Side;
      winner?: string | null;
      moves?: MoveRecord[];
    }>>(parsed.payload);
    if (payload) {
      if (payload.playerStones && helpers.currentUser.value) {
        helpers.roleLabel.value = payload.playerStones[String(helpers.currentUser.value.userId)] ?? helpers.roleLabel.value;
      }
      applyServerState(payload.state);
      helpers.currentTurnLabel.value = payload.state.currentTurn ?? "live";
    }
    return true;
  }
  if (parsed.type === wsMessageTypes.matchEnd && parsed.payload) {
    const payload = parsePayload<MatchEndPayload>(parsed.payload);
    if (payload?.winnerStone) {
      winner.value = payload.winnerStone;
      helpers.winner.value = payload.winnerStone;
      syncWindowHelpers();
    }
    return false;
  }
  return false;
}

function syncWindowHelpers() {
  (window as any).render_game_to_text = () => JSON.stringify({
    mode: winner.value ? "finished" : roomSession.inMatch.value ? "playing" : "room",
    phase: roomSession.phase.value,
    note: "origin is top-left, rows grow downward, cols grow rightward",
    roomCode: roomSession.activeRoomCode.value || null,
    matchCode: roomSession.matchCode.value || null,
    mySide: mySide.value,
    turn: currentTurn.value,
    winner: winner.value || null,
    selectedCell: selectedCell.value,
    latestMove: moveHistory.value.at(-1) ?? null,
    pieces: boardPieces.value
  });
  (window as any).advanceTime = () => Promise.resolve();
}

export { useChineseChessSession };
