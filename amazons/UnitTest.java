package amazons;

import org.junit.Test;

import static amazons.Piece.*;
import static org.junit.Assert.*;
import ucb.junit.textui;
import java.util.Iterator;

/** The suite of all JUnit tests for the amazons package.
 *  @author Ethan Yim
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** Tests basic correctness of put and get on the initialized board. */
    @Test
    public void testBasicPutGet() {
        Board b = new Board();
        b.put(BLACK, Square.sq(3, 5));
        assertEquals(b.get(3, 5), BLACK);
        b.put(WHITE, Square.sq(9, 9));
        assertEquals(b.get(9, 9), WHITE);
        b.put(EMPTY, Square.sq(3, 5));
        assertEquals(b.get(3, 5), EMPTY);
    }

    /** Tests proper identification of legal/illegal queen moves. */
    @Test
    public void testIsQueenMove() {
        assertFalse(Square.sq(1, 5).isQueenMove(Square.sq(1, 5)));
        assertFalse(Square.sq(1, 5).isQueenMove(Square.sq(2, 7)));
        assertFalse(Square.sq(0, 0).isQueenMove(Square.sq(5, 1)));
        assertTrue(Square.sq(1, 1).isQueenMove(Square.sq(9, 9)));
        assertTrue(Square.sq(2, 7).isQueenMove(Square.sq(8, 7)));
        assertTrue(Square.sq(3, 0).isQueenMove(Square.sq(3, 4)));
        assertTrue(Square.sq(7, 9).isQueenMove(Square.sq(0, 2)));
    }

    /** Tests toString for initial board state and a smiling board state. :) */
    @Test
    public void testToString() {
        Board b = new Board();
        assertEquals(INIT_BOARD_STATE, b.toString());
        makeSmile(b);
        assertEquals(SMILE, b.toString());
    }

    @Test
    public void testIsUnblockedMove() {
        Board b = new Board();
        makeSmile(b);
        assertFalse(b.isUnblockedMove(Square.sq(6, 2), Square.sq(0, 8), null));
        assertTrue(b.isUnblockedMove(Square.sq(2, 3), Square.sq(5, 6), null));
        assertFalse(b.isUnblockedMove(Square.sq(2, 3), Square.sq(6, 6), null));
        assertFalse(b.isUnblockedMove(Square.sq(2, 3), Square.sq(6, 7), null));
        assertTrue(b.isUnblockedMove(Square.sq(5, 5), Square.sq(7, 3),
                Square.sq(7, 3)));
        assertTrue(b.isUnblockedMove(Square.sq(5, 5), Square.sq(8, 2),
                Square.sq(7, 3)));
        assertTrue(b.isUnblockedMove(Square.sq(7, 3), Square.sq(7, 0), null));
        assertFalse(b.isUnblockedMove(Square.sq(7, 3), Square.sq(7, 6), null));
        assertFalse(b.isUnblockedMove(Square.sq(7, 3), Square.sq(7, 7), null));
        assertTrue(b.isUnblockedMove(Square.sq(7, 3), Square.sq(7, 5), null));
        assertTrue(b.isUnblockedMove(Square.sq(5, 9), Square.sq(5, 6), null));
        assertFalse(b.isUnblockedMove(Square.sq(5, 9), Square.sq(5, 0), null));
        assertFalse(b.isUnblockedMove(Square.sq(2, 3), Square.sq(4, 1), null));
    }

    @Test
    public void testIsLegal() {
        Board b = new Board();
        makeSmile(b);
        assertFalse(b.isLegal(Square.sq(3, 3)));
        assertFalse(b.isLegal(Square.sq(1, 7)));
        assertTrue(b.isLegal(Square.sq(2, 3)));
        assertTrue(b.isLegal(Square.sq(3, 2)));
        assertTrue(b.isLegal(Square.sq(4, 2)));
        assertTrue(b.isLegal(Square.sq(5, 2)));
        assertTrue(b.isLegal(Square.sq(6, 2)));
        assertTrue(b.isLegal(Square.sq(6, 2), Square.sq(6, 0)));
        assertTrue(b.isLegal(Square.sq(6, 2), Square.sq(6, 1)));
        assertFalse(b.isLegal(Square.sq(6, 2), Square.sq(6, 6)));
        assertFalse(b.isLegal(Square.sq(6, 2), Square.sq(8, 1)));
        assertFalse(b.isLegal(Square.sq(7, 3), Square.sq(7, 5),
                Square.sq(3, 1)));
        assertFalse(b.isLegal(Square.sq(7, 3), Square.sq(7, 5),
                Square.sq(4, 2)));
        assertTrue(b.isLegal(Square.sq(7, 3), Square.sq(7, 5),
                Square.sq(5, 3)));
    }

    @Test
    public void testMakeMove() {
        Board b = new Board();
        b.makeMove(Square.sq(3, 0), Square.sq(3, 2), Square.sq(0, 5));
        assertEquals(AFTER_ONE_MOVE, b.toString());
        assertEquals(BLACK, b.turn());
        Move m = Move.mv(Square.sq(6, 9), Square.sq(6, 1), Square.sq(6, 9));
        b.makeMove(m);
        assertEquals(AFTER_TWO_MOVE, b.toString());
        assertEquals(WHITE, b.turn());
    }

    @Test
    public void testReachableFromIteratorForQueen() {
        Board b = new Board();
        Iterator<Square> r = b.reachableFrom(Square.sq(3, 0), null);
        assertTrue(r.hasNext());
        assertEquals(Square.sq(3, 1), r.next());
        assertEquals(Square.sq("d3"), r.next());
        assertEquals(Square.sq("d4"), r.next());
        assertEquals(Square.sq("d5"), r.next());
        assertEquals(Square.sq("d6"), r.next());
        assertEquals(Square.sq("d7"), r.next());
        assertEquals(Square.sq("d8"), r.next());
        assertEquals(Square.sq("d9"), r.next());
        assertEquals(Square.sq("e2"), r.next());
        assertEquals(Square.sq("f3"), r.next());
        assertEquals(Square.sq("g4"), r.next());
        assertEquals(Square.sq("h5"), r.next());
        assertEquals(Square.sq("i6"), r.next());
        assertEquals(Square.sq("e1"), r.next());
        assertEquals(Square.sq("f1"), r.next());
        assertEquals(Square.sq("c1"), r.next());
        assertEquals(Square.sq("b1"), r.next());
        assertEquals(Square.sq("a1"), r.next());
        assertEquals(Square.sq("c2"), r.next());
        assertEquals(Square.sq("b3"), r.next());
        assertFalse(r.hasNext());

        makeSmile(b);
        Iterator<Square> r2 = b.reachableFrom(Square.sq(3, 2), null);
        assertTrue(r2.hasNext());
        assertEquals(Square.sq("d4"), r2.next());
        assertEquals(Square.sq("d5"), r2.next());
        assertEquals(Square.sq("d6"), r2.next());
        assertEquals(Square.sq("e4"), r2.next());
        assertEquals(Square.sq("f5"), r2.next());
        assertEquals(Square.sq("g6"), r2.next());
        assertEquals(Square.sq("e2"), r2.next());
        assertEquals(Square.sq("f1"), r2.next());
        assertEquals(Square.sq("d2"), r2.next());
        assertEquals(Square.sq("d1"), r2.next());
        assertEquals(Square.sq("c2"), r2.next());
        assertEquals(Square.sq("b1"), r2.next());
        assertEquals(Square.sq("c3"), r2.next());
        assertEquals(Square.sq("b3"), r2.next());
        assertEquals(Square.sq("a3"), r2.next());
        assertFalse(r2.hasNext());
    }

    @Test
    public void testReachableFromIteratorForArrow() {
        Board b = new Board();
        makeSmile(b);
        Iterator<Square> r = b.reachableFrom(Square.sq("d6"), Square.sq("d3"));
        assertTrue(r.hasNext());
        assertEquals(Square.sq("e7"), r.next());
        assertEquals(Square.sq("f8"), r.next());
        assertEquals(Square.sq("e6"), r.next());
        assertEquals(Square.sq("f6"), r.next());
        assertEquals(Square.sq("g6"), r.next());
        assertEquals(Square.sq("h6"), r.next());
        assertEquals(Square.sq("i6"), r.next());
        assertEquals(Square.sq("j6"), r.next());
        assertEquals(Square.sq("e5"), r.next());
        assertEquals(Square.sq("f4"), r.next());
        assertEquals(Square.sq("d5"), r.next());
        assertEquals(Square.sq("d4"), r.next());
        assertEquals(Square.sq("d3"), r.next());
        assertEquals(Square.sq("d2"), r.next());
        assertEquals(Square.sq("d1"), r.next());
        assertEquals(Square.sq("c5"), r.next());
        assertEquals(Square.sq("b4"), r.next());
        assertEquals(Square.sq("a3"), r.next());
        assertEquals(Square.sq("c6"), r.next());
        assertEquals(Square.sq("b6"), r.next());
        assertEquals(Square.sq("a6"), r.next());
        assertFalse(r.hasNext());
    }

    @Test
    public void testLegalMoveIterator() {
        Board b = new Board();
        Iterator<Move> r = b.legalMoves(Piece.WHITE);
        assertTrue(r.hasNext());
        int count = 0;
        while (r.hasNext()) {
            count++;
            r.next();
        }
        assertEquals(2176, count);
        assertFalse(r.hasNext());
    }

    private void makeSmile(Board b) {
        b.put(EMPTY, Square.sq(0, 3));
        b.put(EMPTY, Square.sq(0, 6));
        b.put(EMPTY, Square.sq(9, 3));
        b.put(EMPTY, Square.sq(9, 6));
        b.put(EMPTY, Square.sq(3, 0));
        b.put(EMPTY, Square.sq(3, 9));
        b.put(EMPTY, Square.sq(6, 0));
        b.put(EMPTY, Square.sq(6, 9));
        for (int col = 1; col < 4; col += 1) {
            for (int row = 6; row < 9; row += 1) {
                b.put(SPEAR, Square.sq(col, row));
            }
        }
        b.put(EMPTY, Square.sq(2, 7));
        for (int col = 6; col < 9; col += 1) {
            for (int row = 6; row < 9; row += 1) {
                b.put(SPEAR, Square.sq(col, row));
            }
        }
        b.put(EMPTY, Square.sq(7, 7));
        for (int lip = 3; lip < 7; lip += 1) {
            b.put(WHITE, Square.sq(lip, 2));
        }
        b.put(WHITE, Square.sq(2, 3));
        b.put(WHITE, Square.sq(7, 3));
    }

    static final String INIT_BOARD_STATE =
              "   - - - B - - B - - -\n"
           +  "   - - - - - - - - - -\n"
           +  "   - - - - - - - - - -\n"
           +  "   B - - - - - - - - B\n"
           +  "   - - - - - - - - - -\n"
           +  "   - - - - - - - - - -\n"
           +  "   W - - - - - - - - W\n"
           +  "   - - - - - - - - - -\n"
           +  "   - - - - - - - - - -\n"
           +  "   - - - W - - W - - -\n";

    static final String SMILE =
             "   - - - - - - - - - -\n"
           + "   - S S S - - S S S -\n"
           + "   - S - S - - S - S -\n"
           + "   - S S S - - S S S -\n"
           + "   - - - - - - - - - -\n"
           + "   - - - - - - - - - -\n"
           + "   - - W - - - - W - -\n"
           + "   - - - W W W W - - -\n"
           + "   - - - - - - - - - -\n"
           + "   - - - - - - - - - -\n";

    static final String AFTER_ONE_MOVE =
             "   - - - B - - B - - -\n"
           + "   - - - - - - - - - -\n"
           + "   - - - - - - - - - -\n"
           + "   B - - - - - - - - B\n"
           + "   S - - - - - - - - -\n"
           + "   - - - - - - - - - -\n"
           + "   W - - - - - - - - W\n"
           + "   - - - W - - - - - -\n"
           + "   - - - - - - - - - -\n"
           + "   - - - - - - W - - -\n";

    static final String AFTER_TWO_MOVE =
            "   - - - B - - S - - -\n"
           + "   - - - - - - - - - -\n"
           + "   - - - - - - - - - -\n"
           + "   B - - - - - - - - B\n"
           + "   S - - - - - - - - -\n"
           + "   - - - - - - - - - -\n"
           + "   W - - - - - - - - W\n"
           + "   - - - W - - - - - -\n"
           + "   - - - - - - B - - -\n"
           + "   - - - - - - W - - -\n";

}
