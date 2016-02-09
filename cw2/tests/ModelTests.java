import draughts.*;

import java.awt.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ModelTests {

    private class TestPlayer implements Player {

        @Override
        public Move notify(Set<Move> moves) {
            if (moves.iterator().hasNext()) return moves.iterator().next();
            return null;
        }

    }

    public class TestModel extends DraughtsModel {

      public TestModel(String gameName, Player player, Colour currentPlayer, Set<Piece> pieces) {
          super(gameName, player, currentPlayer, pieces);
      }

      public TestModel(String gameName, Player player) {
          super(gameName, player);
      }

      public boolean removePieceInModel(Point position, Point destination) {
          return removePiece(position, destination);
      }

      public void turnInModel() {
          turn();
      }

    }

    @Test
    public void testGameNameIsCorrect() throws Exception {
        DraughtsModel model = new DraughtsModel("Test", null);

        assertEquals("The game name should be the same as the one passed in", "Test", model.getGameName());
    }

    @Test
    public void testCurrentPlayerIsRedAtStartOfGame() throws Exception {
        DraughtsModel model = new DraughtsModel("Game", null);

        assertEquals("The red player should be the current player at the beginning of the game.", Colour.Red, model.getCurrentPlayer());
    }

    @Test
    public void testCurrentPlayerUpdatesCorrectly() throws Exception {
        TestModel model = new TestModel("Test", new TestPlayer());

        assertEquals("The current player should be red initially", Colour.Red, model.getCurrentPlayer());

        model.turnInModel();
        assertEquals("The current player should be white after one turn", Colour.White, model.getCurrentPlayer());

        model.turnInModel();
        assertEquals("The current player should be red after two turns", Colour.Red, model.getCurrentPlayer());
    }

    @Test
    public void testCorrectPieceIsReturned() throws Exception {
        Set<Piece> pieces = new HashSet<Piece>();

        Piece[] testPieces = new Piece[] {
                new Piece(Colour.Red, 3, 5),
                new Piece(Colour.Red, 1, 3),
                new Piece(Colour.Red, 2, 2),
        };

        for (Piece piece : testPieces) {
            pieces.add(piece);
        }

        DraughtsModel model = new DraughtsModel("Test", null, Colour.Red, pieces);

        assertEquals("Expected piece on (3, 5)", testPieces[0], model.getPiece(3, 5));
        assertEquals("Expected piece on (1, 3)", testPieces[1], model.getPiece(1, 3));
        assertEquals("Expected piece on (2, 2)", testPieces[2], model.getPiece(2, 2));
    }

}
