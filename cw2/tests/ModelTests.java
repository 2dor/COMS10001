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

    @Test
    public void testRemovesPieceCorrectly() throws Exception {
        Set<Piece> pieces = new HashSet<Piece>();

        Piece[] testPieces = new Piece[] {
                new Piece(Colour.Red, 0, 0),
                new Piece(Colour.White, 1, 1),

                new Piece(Colour.Red, 5, 5),
                new Piece(Colour.White, 4, 5),
                new Piece(Colour.White, 4, 6),
                new Piece(Colour.White, 6, 4),
                new Piece(Colour.White, 6, 6),
        };

        for (Piece piece : testPieces) {
            pieces.add(piece);
        }

        TestModel model = new TestModel("Test", new TestPlayer(), Colour.Red, pieces);

        /**
         * Red moves, red is NOT a king
         */


        assertEquals(
            "Expected TRUE",
            true, model.removePieceInModel(new Point (0, 0), new Point (2, 2)));
        assertEquals(
            "Expected FALSE: red can't jump backwards from (5, 5) to (5, 7)",
            false, model.removePieceInModel(new Point (5, 5), new Point (3, 3)));
        assertEquals(
            "Expected FALSE: red can't jump backwards from (5, 5) to (3, 7)",
            false, model.removePieceInModel(new Point (5, 5), new Point (3, 7)));
        assertEquals(
            "Expected TRUE: red can jump from (5, 5) to (7, 3)",
            true, model.removePieceInModel(new Point (5, 5), new Point (7, 3)));
        assertEquals(
            "Expected TRUE: red can jump from (5, 5) to (7, 7)",
            true, model.removePieceInModel(new Point (5, 5), new Point (7, 7)));

        /**
         * Red moves, red is a king
         */

        // Not implemented yet

    }

}
