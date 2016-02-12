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
	  public void playInModel(Move move) {
		  play(move);
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

        assertEquals(
            "Expected TRUE",
            true, model.removePieceInModel(new Point (5, 5), new Point (3, 3)));
        assertEquals(
            "Expected TRUE",
            true, model.removePieceInModel(new Point (5, 5), new Point (3, 7)));
        assertEquals(
            "Expected TRUE",
            true, model.removePieceInModel(new Point (5, 5), new Point (7, 3)));
        assertEquals(
            "Expected TRUE",
            true, model.removePieceInModel(new Point (5, 5), new Point (7, 7)));

    }
	@Test
    public void testPlayIsWorking() throws Exception {
        Set<Piece> pieces = new HashSet<Piece>();

        Piece[] testPieces = new Piece[] {
                new Piece(Colour.Red, 1, 3),
                new Piece(Colour.White, 6, 6),
				new Piece(Colour.Red, 2, 6),
                new Piece(Colour.White, 3, 5),
        };

		testPieces[1].setKing(true);

        for (Piece piece : testPieces) {
            pieces.add(piece);
        }

        TestModel model = new TestModel("Test", new TestPlayer(), Colour.Red, pieces);

		//Normal piece moving forward (right)
		Move normalR = new Move(testPieces[0], 2, 4);
		model.playInModel(normalR);
        assertEquals("Expected to remove a piece from the origin - (1, 3)", null, model.getPiece(1, 3));
		assertEquals("Expected to move a piece to the destination - (2, 4)", testPieces[0], model.getPiece(2, 4));

		//Normal piece moving forward (left)
		Move normalL = new Move(testPieces[0], 3, 3);
		model.playInModel(normalL);
        assertEquals("Expected to remove a piece from the origin - (2, 4)", null, model.getPiece(2, 4));
		assertEquals("Expected to move a piece to the destination - (3, 3)", testPieces[0], model.getPiece(3, 3));

		//King piece moving forward-left
		Move kingFL = new Move(testPieces[1], 5, 5);
		model.playInModel(kingFL);
		assertEquals("Expected to remove a king piece from the origin - (6, 6)", null, model.getPiece(6, 6));
		assertEquals("Expected to move a king piece to the destination - (5, 5)", testPieces[1], model.getPiece(5, 5));

		//King piece moving backward-left
		Move kingBL = new Move(testPieces[1], 6, 4);
		model.playInModel(kingBL);
		assertEquals("Expected to remove a king piece from the origin - (5, 5)", null, model.getPiece(5, 5));
		assertEquals("Expected to move a king piece to the destination - (6, 4)", testPieces[1], model.getPiece(6, 4));

		//King piece moving backward-right
		Move kingBR = new Move(testPieces[1], 7, 5);
		model.playInModel(kingBR);
		assertEquals("Expected to remove a king piece from the origin - (6, 4)", null, model.getPiece(6, 4));
		assertEquals("Expected to move a king piece to the destination - (7, 5)", testPieces[1], model.getPiece(7, 5));

		//King piece moving forward-right
		Move kingFR = new Move(testPieces[1], 6, 6);
		model.playInModel(kingFR);
		assertEquals("Expected to remove a king piece from the origin - (7, 5)", null, model.getPiece(7, 5));
		assertEquals("Expected to move a king piece to the destination - (6, 6)", testPieces[1], model.getPiece(6, 6));

		//Test if a jump is made over a piece that it is removed from the board
		Move jump = new Move(testPieces[3], 1, 7);
		model.playInModel(jump);
		assertEquals("Expected to remove the attacking piece from the origin - (3, 5)", null, model.getPiece(3, 5));
		assertEquals("Expected to move the attacking piece to the destination - (1, 7)", testPieces[3], model.getPiece(1, 7));
		assertEquals("Expected to remove the attacked piece - (2, 6)", null, model.getPiece(2, 6));
    }

}
