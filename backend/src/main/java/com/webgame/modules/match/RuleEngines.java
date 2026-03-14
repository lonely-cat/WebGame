package com.webgame.modules.match;

import com.webgame.modules.match.MatchModels.ActionValidateResult;
import com.webgame.modules.match.MatchModels.GameState;
import com.webgame.modules.match.MatchModels.GameStateView;
import com.webgame.modules.match.MatchModels.MatchInitContext;
import com.webgame.modules.match.MatchModels.MatchResult;
import com.webgame.modules.match.MatchModels.PlayerActionCommand;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

public final class RuleEngines {

    private RuleEngines() {
    }

    private abstract static class AbstractRuleEngine implements GameRuleEngine {
        @Override
        public GameState initState(MatchInitContext context) {
            return new GameState(getGameCode(), new HashMap<>());
        }

        @Override
        public ActionValidateResult validateAction(PlayerActionCommand action, GameState state) {
            return new ActionValidateResult(true, "accepted");
        }

        @Override
        public GameState applyAction(PlayerActionCommand action, GameState state) {
            return state;
        }

        @Override
        public boolean isGameOver(GameState state) {
            return false;
        }

        @Override
        public MatchResult buildMatchResult(GameState state) {
            return new MatchResult(getGameCode(), "", null, state.data());
        }

        @Override
        public GameStateView buildStateView(GameState state, Long viewerUserId) {
            return new GameStateView(getGameCode(), state.data());
        }
    }

    @Component
    public static class GomokuRuleEngine extends AbstractRuleEngine {
        private static final int BOARD_SIZE = 15;

        @Override
        public String getGameCode() {
            return "gomoku";
        }

        @Override
        public GameState initState(MatchInitContext context) {
            Map<String, Object> data = new HashMap<>();
            data.put("boardSize", BOARD_SIZE);
            data.put("board", createBoard());
            data.put("currentTurn", "black");
            data.put("winner", null);
            data.put("moves", new ArrayList<Map<String, Object>>());
            return new GameState(getGameCode(), data);
        }

        @Override
        public ActionValidateResult validateAction(PlayerActionCommand action, GameState state) {
            if (!"move".equals(action.actionType())) {
                return new ActionValidateResult(false, "unsupported action type");
            }
            Integer row = readInt(action.payload().get("row"));
            Integer col = readInt(action.payload().get("col"));
            String stone = String.valueOf(action.payload().get("stone"));
            if (row == null || col == null || row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
                return new ActionValidateResult(false, "move is out of board");
            }
            if (!stone.equals(state.data().get("currentTurn"))) {
                return new ActionValidateResult(false, "it is not your turn");
            }
            String[][] board = readBoard(state);
            if (board[row][col] != null) {
                return new ActionValidateResult(false, "cell is already occupied");
            }
            return new ActionValidateResult(true, "accepted");
        }

        @Override
        public GameState applyAction(PlayerActionCommand action, GameState state) {
            Integer row = readInt(action.payload().get("row"));
            Integer col = readInt(action.payload().get("col"));
            String stone = String.valueOf(action.payload().get("stone"));
            String[][] board = readBoard(state);
            board[row][col] = stone;
            state.data().put("board", board);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> moves = (List<Map<String, Object>>) state.data().get("moves");
            moves.add(new HashMap<>(Map.of(
                    "row", row,
                    "col", col,
                    "stone", stone
            )));

            if (isFiveInARow(board, row, col, stone)) {
                state.data().put("winner", stone);
            } else {
                state.data().put("currentTurn", "black".equals(stone) ? "white" : "black");
            }
            return state;
        }

        @Override
        public boolean isGameOver(GameState state) {
            return state.data().get("winner") != null;
        }

        @Override
        public MatchResult buildMatchResult(GameState state) {
            String winner = state.data().get("winner") == null ? null : String.valueOf(state.data().get("winner"));
            Map<String, Object> summary = new HashMap<>(state.data());
            return new MatchResult(getGameCode(), "", null, Map.of(
                    "winnerStone", winner,
                    "currentTurn", state.data().get("currentTurn"),
                    "moves", summary.get("moves")
            ));
        }

        @Override
        public GameStateView buildStateView(GameState state, Long viewerUserId) {
            return new GameStateView(getGameCode(), new HashMap<>(state.data()));
        }

        private static String[][] createBoard() {
            return new String[BOARD_SIZE][BOARD_SIZE];
        }

        private static String[][] readBoard(GameState state) {
            return (String[][]) state.data().get("board");
        }

        private static Integer readInt(Object value) {
            if (value instanceof Integer integer) {
                return integer;
            }
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value instanceof String string) {
                return Integer.parseInt(string);
            }
            return null;
        }

        private static boolean isFiveInARow(String[][] board, int row, int col, String stone) {
            int[][] directions = new int[][]{{1, 0}, {0, 1}, {1, 1}, {1, -1}};
            for (int[] direction : directions) {
                int count = 1;
                count += countDirection(board, row, col, direction[0], direction[1], stone);
                count += countDirection(board, row, col, -direction[0], -direction[1], stone);
                if (count >= 5) {
                    return true;
                }
            }
            return false;
        }

        private static int countDirection(String[][] board, int row, int col, int rowStep, int colStep, String stone) {
            int count = 0;
            int nextRow = row + rowStep;
            int nextCol = col + colStep;
            while (nextRow >= 0 && nextRow < BOARD_SIZE && nextCol >= 0 && nextCol < BOARD_SIZE) {
                if (!stone.equals(board[nextRow][nextCol])) {
                    break;
                }
                count += 1;
                nextRow += rowStep;
                nextCol += colStep;
            }
            return count;
        }
    }

    @Component
    public static class ChineseChessRuleEngine extends AbstractRuleEngine {
        private static final int ROWS = 10;
        private static final int COLS = 9;

        @Override
        public String getGameCode() {
            return "chinese-chess";
        }

        @Override
        public GameState initState(MatchInitContext context) {
            Map<String, Object> data = new HashMap<>();
            data.put("board", createInitialBoard());
            data.put("currentTurn", "red");
            data.put("winner", null);
            data.put("checkSide", null);
            data.put("moves", new ArrayList<Map<String, Object>>());
            return new GameState(getGameCode(), data);
        }

        @Override
        public ActionValidateResult validateAction(PlayerActionCommand action, GameState state) {
            if (!"move".equals(action.actionType())) {
                return new ActionValidateResult(false, "unsupported action type");
            }
            Integer fromRow = readInt(action.payload().get("fromRow"));
            Integer fromCol = readInt(action.payload().get("fromCol"));
            Integer toRow = readInt(action.payload().get("toRow"));
            Integer toCol = readInt(action.payload().get("toCol"));
            String side = String.valueOf(action.payload().getOrDefault("role", ""));
            if (!isInsideBoard(fromRow, fromCol) || !isInsideBoard(toRow, toCol)) {
                return new ActionValidateResult(false, "move is out of board");
            }
            if (fromRow.equals(toRow) && fromCol.equals(toCol)) {
                return new ActionValidateResult(false, "source and target cannot match");
            }
            if (!side.equals(state.data().get("currentTurn"))) {
                return new ActionValidateResult(false, "it is not your turn");
            }
            String[][] board = readChessBoard(state);
            String piece = board[fromRow][fromCol];
            if (piece == null) {
                return new ActionValidateResult(false, "no piece on the source square");
            }
            if (!side.equals(pieceSide(piece))) {
                return new ActionValidateResult(false, "you can only move your own pieces");
            }
            String target = board[toRow][toCol];
            if (target != null && side.equals(pieceSide(target))) {
                return new ActionValidateResult(false, "cannot capture your own piece");
            }
            if (!isLegalMove(board, piece, fromRow, fromCol, toRow, toCol)) {
                return new ActionValidateResult(false, "illegal move for that piece");
            }
            String[][] nextBoard = cloneBoard(board);
            nextBoard[toRow][toCol] = piece;
            nextBoard[fromRow][fromCol] = null;
            if (areGeneralsFacing(nextBoard)) {
                return new ActionValidateResult(false, "generals cannot face each other");
            }
            if (isSideInCheck(nextBoard, side)) {
                return new ActionValidateResult(false, "move leaves your general in check");
            }
            return new ActionValidateResult(true, "accepted");
        }

        @Override
        public GameState applyAction(PlayerActionCommand action, GameState state) {
            Integer fromRow = readInt(action.payload().get("fromRow"));
            Integer fromCol = readInt(action.payload().get("fromCol"));
            Integer toRow = readInt(action.payload().get("toRow"));
            Integer toCol = readInt(action.payload().get("toCol"));
            String[][] board = readChessBoard(state);
            String piece = board[fromRow][fromCol];
            String captured = board[toRow][toCol];

            board[toRow][toCol] = piece;
            board[fromRow][fromCol] = null;
            state.data().put("board", board);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> moves = (List<Map<String, Object>>) state.data().get("moves");
            Map<String, Object> move = new HashMap<>();
            move.put("fromRow", fromRow);
            move.put("fromCol", fromCol);
            move.put("toRow", toRow);
            move.put("toCol", toCol);
            move.put("piece", piece);
            if (captured != null) {
                move.put("captured", captured);
            }
            moves.add(move);

            if ("general".equals(pieceKind(captured))) {
                state.data().put("winner", pieceSide(piece));
                state.data().put("checkSide", null);
            } else {
                String nextTurn = "red".equals(pieceSide(piece)) ? "black" : "red";
                state.data().put("currentTurn", nextTurn);
                state.data().put("checkSide", isSideInCheck(board, nextTurn) ? nextTurn : null);
            }
            return state;
        }

        @Override
        public boolean isGameOver(GameState state) {
            return state.data().get("winner") != null;
        }

        @Override
        public MatchResult buildMatchResult(GameState state) {
            return new MatchResult(getGameCode(), "", null, Map.of(
                    "winnerStone", state.data().get("winner"),
                    "currentTurn", state.data().get("currentTurn"),
                    "moves", state.data().get("moves")
            ));
        }

        @Override
        public GameStateView buildStateView(GameState state, Long viewerUserId) {
            return new GameStateView(getGameCode(), new HashMap<>(state.data()));
        }

        private static String[][] createInitialBoard() {
            String[][] board = new String[ROWS][COLS];
            placeBackRank(board, 0, "black");
            placeCannons(board, 2, "black");
            placePawns(board, 3, "black");
            placeBackRank(board, 9, "red");
            placeCannons(board, 7, "red");
            placePawns(board, 6, "red");
            return board;
        }

        private static void placeBackRank(String[][] board, int row, String side) {
            board[row][0] = side + "-rook";
            board[row][1] = side + "-horse";
            board[row][2] = side + "-elephant";
            board[row][3] = side + "-advisor";
            board[row][4] = side + "-general";
            board[row][5] = side + "-advisor";
            board[row][6] = side + "-elephant";
            board[row][7] = side + "-horse";
            board[row][8] = side + "-rook";
        }

        private static void placeCannons(String[][] board, int row, String side) {
            board[row][1] = side + "-cannon";
            board[row][7] = side + "-cannon";
        }

        private static void placePawns(String[][] board, int row, String side) {
            for (int col = 0; col < COLS; col += 2) {
                board[row][col] = side + "-pawn";
            }
        }

        private static boolean isInsideBoard(Integer row, Integer col) {
            return row != null && col != null && row >= 0 && row < ROWS && col >= 0 && col < COLS;
        }

        private static String[][] readChessBoard(GameState state) {
            return (String[][]) state.data().get("board");
        }

        private static String[][] cloneBoard(String[][] board) {
            String[][] cloned = new String[ROWS][COLS];
            for (int row = 0; row < ROWS; row += 1) {
                System.arraycopy(board[row], 0, cloned[row], 0, COLS);
            }
            return cloned;
        }

        private static Integer readInt(Object value) {
            if (value instanceof Integer integer) {
                return integer;
            }
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value instanceof String string) {
                return Integer.parseInt(string);
            }
            return null;
        }

        private static String pieceSide(String piece) {
            if (piece == null || !piece.contains("-")) {
                return "";
            }
            return piece.substring(0, piece.indexOf('-'));
        }

        private static String pieceKind(String piece) {
            if (piece == null || !piece.contains("-")) {
                return "";
            }
            return piece.substring(piece.indexOf('-') + 1);
        }

        private static boolean areGeneralsFacing(String[][] board) {
            int[] red = findGeneral(board, "red");
            int[] black = findGeneral(board, "black");
            if (red == null || black == null || red[1] != black[1]) {
                return false;
            }
            return countBlockers(board, red[0], red[1], black[0], black[1]) == 0;
        }

        private static int[] findGeneral(String[][] board, String side) {
            String target = side + "-general";
            for (int row = 0; row < ROWS; row += 1) {
                for (int col = 0; col < COLS; col += 1) {
                    if (target.equals(board[row][col])) {
                        return new int[]{row, col};
                    }
                }
            }
            return null;
        }

        private static boolean isSideInCheck(String[][] board, String side) {
            int[] general = findGeneral(board, side);
            if (general == null) {
                return false;
            }
            String enemy = "red".equals(side) ? "black" : "red";
            for (int row = 0; row < ROWS; row += 1) {
                for (int col = 0; col < COLS; col += 1) {
                    String piece = board[row][col];
                    if (piece == null || !enemy.equals(pieceSide(piece))) {
                        continue;
                    }
                    if (isLegalMove(board, piece, row, col, general[0], general[1])) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static boolean isLegalMove(String[][] board, String piece, int fromRow, int fromCol, int toRow, int toCol) {
            String side = pieceSide(piece);
            String kind = pieceKind(piece);
            int rowDelta = toRow - fromRow;
            int colDelta = toCol - fromCol;
            int absRow = Math.abs(rowDelta);
            int absCol = Math.abs(colDelta);
            return switch (kind) {
                case "rook" -> (rowDelta == 0 || colDelta == 0) && countBlockers(board, fromRow, fromCol, toRow, toCol) == 0;
                case "cannon" -> isCannonMove(board, toRow, toCol, fromRow, fromCol);
                case "horse" -> isHorseMove(board, fromRow, fromCol, toRow, toCol, absRow, absCol);
                case "elephant" -> isElephantMove(board, side, fromRow, fromCol, toRow, toCol, absRow, absCol);
                case "advisor" -> absRow == 1 && absCol == 1 && isInsidePalace(side, toRow, toCol);
                case "general" -> isGeneralMove(board, side, fromRow, fromCol, toRow, toCol, absRow, absCol);
                case "pawn" -> isPawnMove(side, fromRow, fromCol, toRow, toCol, rowDelta, colDelta);
                default -> false;
            };
        }

        private static boolean isCannonMove(String[][] board, int toRow, int toCol, int fromRow, int fromCol) {
            if (toRow != fromRow && toCol != fromCol) {
                return false;
            }
            int blockers = countBlockers(board, fromRow, fromCol, toRow, toCol);
            boolean isCapture = board[toRow][toCol] != null;
            return isCapture ? blockers == 1 : blockers == 0;
        }

        private static boolean isHorseMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol,
                                           int absRow, int absCol) {
            if (!((absRow == 2 && absCol == 1) || (absRow == 1 && absCol == 2))) {
                return false;
            }
            int legRow = fromRow + (absRow == 2 ? Integer.signum(toRow - fromRow) : 0);
            int legCol = fromCol + (absCol == 2 ? Integer.signum(toCol - fromCol) : 0);
            return board[legRow][legCol] == null;
        }

        private static boolean isElephantMove(String[][] board, String side, int fromRow, int fromCol, int toRow,
                                              int toCol, int absRow, int absCol) {
            if (absRow != 2 || absCol != 2) {
                return false;
            }
            if ("red".equals(side) && toRow < 5) {
                return false;
            }
            if ("black".equals(side) && toRow > 4) {
                return false;
            }
            return board[(fromRow + toRow) / 2][(fromCol + toCol) / 2] == null;
        }

        private static boolean isGeneralMove(String[][] board, String side, int fromRow, int fromCol, int toRow,
                                             int toCol, int absRow, int absCol) {
            if (fromCol == toCol && "general".equals(pieceKind(board[toRow][toCol]))) {
                return countBlockers(board, fromRow, fromCol, toRow, toCol) == 0;
            }
            return absRow + absCol == 1 && isInsidePalace(side, toRow, toCol);
        }

        private static boolean isPawnMove(String side, int fromRow, int fromCol, int toRow, int toCol,
                                          int rowDelta, int colDelta) {
            if (Math.abs(rowDelta) + Math.abs(colDelta) != 1) {
                return false;
            }
            if ("red".equals(side)) {
                if (rowDelta > 0) {
                    return false;
                }
                if (fromRow <= 4) {
                    return rowDelta == 0 || rowDelta == -1;
                }
                return rowDelta == -1;
            }
            if (rowDelta < 0) {
                return false;
            }
            if (fromRow >= 5) {
                return rowDelta == 0 || rowDelta == 1;
            }
            return rowDelta == 1;
        }

        private static boolean isInsidePalace(String side, int row, int col) {
            if (col < 3 || col > 5) {
                return false;
            }
            return "red".equals(side) ? row >= 7 && row <= 9 : row >= 0 && row <= 2;
        }

        private static int countBlockers(String[][] board, int fromRow, int fromCol, int toRow, int toCol) {
            int rowStep = Integer.compare(toRow, fromRow);
            int colStep = Integer.compare(toCol, fromCol);
            int row = fromRow + rowStep;
            int col = fromCol + colStep;
            int blockers = 0;
            while (row != toRow || col != toCol) {
                if (board[row][col] != null) {
                    blockers += 1;
                }
                row += rowStep;
                col += colStep;
            }
            return blockers;
        }
    }

    @Component
    public static class BlackjackRuleEngine extends AbstractRuleEngine {
        @Override
        public String getGameCode() {
            return "blackjack";
        }
    }

    @Component
    public static class DoodleRuleEngine extends AbstractRuleEngine {
        @Override
        public String getGameCode() {
            return "doodle";
        }
    }

    @Component
    public static class DrawGuessRuleEngine extends AbstractRuleEngine {
        private static final List<String> WORDS = List.of("cat", "house", "tree", "rocket", "apple");
        private static final int MAX_ROUNDS = 3;
        private static final int ROUND_DURATION_SECONDS = 45;

        @Override
        public String getGameCode() {
            return "draw-guess";
        }

        @Override
        public GameState initState(MatchInitContext context) {
            Map<String, Object> data = new HashMap<>();
            int roundNo = 1;
            String word = pickWord(context.roomId().intValue(), roundNo);
            data.put("roundNo", roundNo);
            data.put("maxRounds", MAX_ROUNDS);
            data.put("phase", "drawing");
            data.put("secretWord", word);
            data.put("promptMask", "_".repeat(word.length()));
            data.put("strokes", new ArrayList<Map<String, Object>>());
            data.put("guesses", new ArrayList<Map<String, Object>>());
            data.put("winnerUserId", null);
            data.put("currentTurn", "drawing");
            data.put("drawerUserId", null);
            data.put("scores", new HashMap<Long, Integer>());
            data.put("roundEndsAt", Instant.now().plusSeconds(ROUND_DURATION_SECONDS).toString());
            data.put("roundEndReason", null);
            return new GameState(getGameCode(), data);
        }

        @Override
        public ActionValidateResult validateAction(PlayerActionCommand action, GameState state) {
            String role = String.valueOf(action.payload().getOrDefault("role", ""));
            String phase = String.valueOf(state.data().getOrDefault("phase", "drawing"));
            if ("draw_stroke".equals(action.actionType())) {
                if (!"drawing".equals(phase)) {
                    return new ActionValidateResult(false, "round is not active");
                }
                if (!"drawer".equals(role)) {
                    return new ActionValidateResult(false, "only the drawer can draw");
                }
                return new ActionValidateResult(true, "accepted");
            }
            if ("submit_guess".equals(action.actionType())) {
                if (!"drawing".equals(phase)) {
                    return new ActionValidateResult(false, "round is not active");
                }
                if ("drawer".equals(role)) {
                    return new ActionValidateResult(false, "drawer cannot submit guesses");
                }
                String guess = String.valueOf(action.payload().getOrDefault("guess", "")).trim();
                if (guess.isEmpty()) {
                    return new ActionValidateResult(false, "guess cannot be empty");
                }
                return new ActionValidateResult(true, "accepted");
            }
            if ("next_round".equals(action.actionType())) {
                if (!"round_finished".equals(phase)) {
                    return new ActionValidateResult(false, "round is not ready to advance");
                }
                return new ActionValidateResult(true, "accepted");
            }
            return new ActionValidateResult(false, "unsupported action type");
        }

        @Override
        public GameState applyAction(PlayerActionCommand action, GameState state) {
            if ("draw_stroke".equals(action.actionType())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> strokes = (List<Map<String, Object>>) state.data().get("strokes");
                strokes.add(new HashMap<>(Map.of(
                        "userId", action.userId(),
                        "stroke", action.payload().get("stroke")
                )));
                return state;
            }
            if ("submit_guess".equals(action.actionType())) {
                String guess = String.valueOf(action.payload().get("guess")).trim();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> guesses = (List<Map<String, Object>>) state.data().get("guesses");
                guesses.add(new HashMap<>(Map.of(
                        "userId", action.userId(),
                        "guess", guess
                )));
                String secretWord = String.valueOf(state.data().get("secretWord"));
                if (secretWord.equalsIgnoreCase(guess)) {
                    state.data().put("phase", "round_finished");
                    state.data().put("winnerUserId", action.userId());
                    state.data().put("currentTurn", "round_finished");
                    state.data().put("roundEndReason", "guessed");
                    awardScore(state, action.userId(), 10);
                    Long drawerUserId = state.data().get("drawerUserId") instanceof Number number ? number.longValue() : null;
                    if (drawerUserId != null) {
                        awardScore(state, drawerUserId, 4);
                    }
                }
                return state;
            }
            if ("next_round".equals(action.actionType())) {
                advanceRound(state);
            }
            return state;
        }

        @Override
        public boolean isGameOver(GameState state) {
            int roundNo = ((Number) state.data().getOrDefault("roundNo", 1)).intValue();
            int maxRounds = ((Number) state.data().getOrDefault("maxRounds", MAX_ROUNDS)).intValue();
            return "round_finished".equals(state.data().get("phase")) && roundNo >= maxRounds;
        }

        @Override
        public MatchResult buildMatchResult(GameState state) {
            Long winnerUserId = state.data().get("winnerUserId") instanceof Number number ? number.longValue() : null;
            return new MatchResult(getGameCode(), "", winnerUserId, Map.of(
                    "winnerUserId", winnerUserId,
                    "secretWord", state.data().get("secretWord"),
                    "guesses", state.data().get("guesses"),
                    "scores", state.data().get("scores"),
                    "roundNo", state.data().get("roundNo"),
                    "roundEndReason", state.data().get("roundEndReason")
            ));
        }

        @Override
        public GameState reconcileState(GameState state) {
            String phase = String.valueOf(state.data().getOrDefault("phase", "drawing"));
            if (!"drawing".equals(phase)) {
                return state;
            }
            Object rawRoundEndsAt = state.data().get("roundEndsAt");
            if (!(rawRoundEndsAt instanceof String roundEndsAt)) {
                return state;
            }
            if (Instant.now().isBefore(Instant.parse(roundEndsAt))) {
                return state;
            }
            state.data().put("phase", "round_finished");
            state.data().put("currentTurn", "round_finished");
            state.data().put("winnerUserId", null);
            state.data().put("roundEndReason", "timeout");
            return state;
        }

        @Override
        public GameStateView buildStateView(GameState state, Long viewerUserId) {
            Map<String, Object> visible = new HashMap<>(state.data());
            Long drawerUserId = state.data().get("drawerUserId") instanceof Number number ? number.longValue() : null;
            boolean isDrawer = viewerUserId != null && viewerUserId.equals(drawerUserId);
            if (!isDrawer && "drawing".equals(state.data().get("phase"))) {
                visible.remove("secretWord");
            }
            return new GameStateView(getGameCode(), visible);
        }

        private static void advanceRound(GameState state) {
            int currentRound = ((Number) state.data().getOrDefault("roundNo", 1)).intValue();
            int nextRound = currentRound + 1;
            @SuppressWarnings("unchecked")
            Map<Long, String> playerRoles = (Map<Long, String>) state.data().get("playerRoles");
            if (playerRoles != null && !playerRoles.isEmpty()) {
                rotateRoles(playerRoles);
                Long drawerUserId = playerRoles.entrySet().stream()
                        .filter(entry -> "drawer".equals(entry.getValue()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                state.data().put("drawerUserId", drawerUserId);
            }
            String nextWord = pickWord(currentRound, nextRound);
            state.data().put("roundNo", nextRound);
            state.data().put("phase", "drawing");
            state.data().put("secretWord", nextWord);
            state.data().put("promptMask", "_".repeat(nextWord.length()));
            state.data().put("strokes", new ArrayList<Map<String, Object>>());
            state.data().put("guesses", new ArrayList<Map<String, Object>>());
            state.data().put("winnerUserId", null);
            state.data().put("currentTurn", "drawing");
            state.data().put("roundEndsAt", Instant.now().plusSeconds(ROUND_DURATION_SECONDS).toString());
            state.data().put("roundEndReason", null);
        }

        private static void rotateRoles(Map<Long, String> playerRoles) {
            Long currentDrawer = playerRoles.entrySet().stream()
                    .filter(entry -> "drawer".equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            Long currentGuesser = playerRoles.entrySet().stream()
                    .filter(entry -> "guesser".equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (currentDrawer != null) {
                playerRoles.put(currentDrawer, "guesser");
            }
            if (currentGuesser != null) {
                playerRoles.put(currentGuesser, "drawer");
            }
        }

        @SuppressWarnings("unchecked")
        private static void awardScore(GameState state, Long userId, int amount) {
            Map<Long, Integer> scores = (Map<Long, Integer>) state.data().get("scores");
            scores.put(userId, scores.getOrDefault(userId, 0) + amount);
        }

        private static String pickWord(int seed, int roundNo) {
            return WORDS.get(Math.floorMod(seed + roundNo - 1, WORDS.size()));
        }
    }
}
