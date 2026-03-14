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
