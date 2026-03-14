package com.webgame.modules.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.webgame.modules.match.MatchModels.ActionValidateResult;
import com.webgame.modules.match.MatchModels.GameState;
import com.webgame.modules.match.MatchModels.PlayerActionCommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ChineseChessRuleEngineTest {

    private final RuleEngines.ChineseChessRuleEngine engine = new RuleEngines.ChineseChessRuleEngine();

    @Test
    void shouldResolveCheckmateWhenBlackHasNoReply() {
        String[][] board = emptyBoard();
        board[0][4] = "black-general";
        board[1][3] = "red-rook";
        board[1][5] = "red-rook";
        board[2][4] = "red-rook";
        board[9][4] = "red-general";

        GameState state = createState(board, "red");
        PlayerActionCommand action = move(1L, 2, 4, 1, 4);

        ActionValidateResult validation = engine.validateAction(action, state);
        assertTrue(validation.valid(), validation.message());

        engine.applyAction(action, state);

        assertEquals("red", state.data().get("winner"));
        assertEquals("black", state.data().get("checkSide"));
        assertEquals("checkmate", state.data().get("endReason"));
        assertTrue(engine.isGameOver(state));
    }

    @Test
    void shouldResolveStalemateWhenBlackIsBoxedWithoutCheck() {
        String[][] board = emptyBoard();
        board[0][4] = "black-general";
        board[1][3] = "red-rook";
        board[1][4] = "red-advisor";
        board[1][6] = "red-rook";
        board[2][4] = "red-rook";
        board[9][4] = "red-general";

        GameState state = createState(board, "red");
        PlayerActionCommand action = move(1L, 1, 6, 1, 5);

        ActionValidateResult validation = engine.validateAction(action, state);
        assertTrue(validation.valid(), validation.message());

        engine.applyAction(action, state);

        assertEquals("red", state.data().get("winner"));
        assertEquals(null, state.data().get("checkSide"));
        assertEquals("stalemate", state.data().get("endReason"));
        assertTrue(engine.isGameOver(state));
    }

    private static GameState createState(String[][] board, String currentTurn) {
        Map<String, Object> data = new HashMap<>();
        data.put("board", board);
        data.put("currentTurn", currentTurn);
        data.put("winner", null);
        data.put("checkSide", null);
        data.put("endReason", null);
        data.put("moves", new ArrayList<Map<String, Object>>());
        return new GameState("chinese-chess", data);
    }

    private static PlayerActionCommand move(Long userId, int fromRow, int fromCol, int toRow, int toCol) {
        return new PlayerActionCommand(
                "chinese-chess",
                "match-test",
                userId,
                "move",
                Map.of(
                        "role", "red",
                        "fromRow", fromRow,
                        "fromCol", fromCol,
                        "toRow", toRow,
                        "toCol", toCol
                )
        );
    }

    private static String[][] emptyBoard() {
        return new String[10][9];
    }
}
