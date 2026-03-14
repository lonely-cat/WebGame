package com.webgame.modules.match;

import com.webgame.modules.match.MatchModels.ActionValidateResult;
import com.webgame.modules.match.MatchModels.GameState;
import com.webgame.modules.match.MatchModels.GameStateView;
import com.webgame.modules.match.MatchModels.MatchInitContext;
import com.webgame.modules.match.MatchModels.MatchResult;
import com.webgame.modules.match.MatchModels.PlayerActionCommand;
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
        @Override
        public String getGameCode() {
            return "chinese-chess";
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

        @Override
        public String getGameCode() {
            return "draw-guess";
        }

        @Override
        public GameState initState(MatchInitContext context) {
            Map<String, Object> data = new HashMap<>();
            String word = WORDS.get(Math.floorMod(context.roomId().intValue(), WORDS.size()));
            data.put("roundNo", 1);
            data.put("phase", "drawing");
            data.put("secretWord", word);
            data.put("promptMask", "_".repeat(word.length()));
            data.put("strokes", new ArrayList<Map<String, Object>>());
            data.put("guesses", new ArrayList<Map<String, Object>>());
            data.put("winnerUserId", null);
            data.put("currentTurn", "drawing");
            data.put("drawerUserId", null);
            return new GameState(getGameCode(), data);
        }

        @Override
        public ActionValidateResult validateAction(PlayerActionCommand action, GameState state) {
            String role = String.valueOf(action.payload().getOrDefault("role", ""));
            if ("draw_stroke".equals(action.actionType())) {
                if (!"drawer".equals(role)) {
                    return new ActionValidateResult(false, "only the drawer can draw");
                }
                return new ActionValidateResult(true, "accepted");
            }
            if ("submit_guess".equals(action.actionType())) {
                if ("drawer".equals(role)) {
                    return new ActionValidateResult(false, "drawer cannot submit guesses");
                }
                String guess = String.valueOf(action.payload().getOrDefault("guess", "")).trim();
                if (guess.isEmpty()) {
                    return new ActionValidateResult(false, "guess cannot be empty");
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
                    state.data().put("phase", "finished");
                    state.data().put("winnerUserId", action.userId());
                    state.data().put("currentTurn", "finished");
                }
            }
            return state;
        }

        @Override
        public boolean isGameOver(GameState state) {
            return state.data().get("winnerUserId") != null;
        }

        @Override
        public MatchResult buildMatchResult(GameState state) {
            Long winnerUserId = state.data().get("winnerUserId") instanceof Number number ? number.longValue() : null;
            return new MatchResult(getGameCode(), "", winnerUserId, Map.of(
                    "winnerUserId", winnerUserId,
                    "secretWord", state.data().get("secretWord"),
                    "guesses", state.data().get("guesses")
            ));
        }

        @Override
        public GameStateView buildStateView(GameState state, Long viewerUserId) {
            Map<String, Object> visible = new HashMap<>(state.data());
            Long drawerUserId = state.data().get("drawerUserId") instanceof Number number ? number.longValue() : null;
            boolean isDrawer = viewerUserId != null && viewerUserId.equals(drawerUserId);
            if (!isDrawer) {
                visible.remove("secretWord");
            }
            return new GameStateView(getGameCode(), visible);
        }
    }
}
