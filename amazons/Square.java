package amazons;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static amazons.Utils.*;

/** Represents a position on an Amazons board.  Positions are numbered
 *  from 0 (lower-left corner) to 99 (upper-right corner).  Squares
 *  are immutable and unique: there is precisely one square created for
 *  each distinct position.  Clients create squares using the factory method
 *  sq, not the constructor.  Because there is a unique Square object for each
 *  position, you can freely use the cheap == operator (rather than the
 *  .equals method) to compare Squares, and the program does not waste time
 *  creating the same square over and over again.
 *  @author Ethan Yim
 */
final class Square {

    /** The regular expression for a square designation (e.g.,
     *  a3). For convenience, it is in parentheses to make it a
     *  group.  This subpattern is intended to be incorporated into
     *  other pattern that contain square designations (such as
     *  patterns for moves). */
    static final String SQ = "([a-j](?:[1-9]|10))";

    /** Return my row position, where 0 is the bottom row. */
    int row() {
        return _row;
    }

    /** Return my column position, where 0 is the leftmost column. */
    int col() {
        return _col;
    }

    /** Return my index position (0-99).  0 represents square a1, and 99
     *  is square j10. */
    int index() {
        return _index;
    }

    /** Return true iff THIS - TO is a valid queen move. */
    boolean isQueenMove(Square to) {
        if (to != null && exists(to._col, to._row)) {
            if (to._row == this._row ^ to._col == this._col) {
                return true;
            }
            for (int direction = 1; direction <= 7; direction += 2) {
                int steps = 1;
                Square diagSquare;
                while (true) {
                    diagSquare = queenMove(direction, steps);
                    if (diagSquare == null) {
                        break;
                    }
                    if (diagSquare._col == to._col
                            && diagSquare._row == to._row) {
                        return true;
                    }
                    steps++;
                }
            }
        }
        return false;
    }


    /** Definitions of direction for queenMove.  DIR[k] = (dcol, drow)
     *  means that to going one step from (col, row) in direction k,
     *  brings us to (col + dcol, row + drow). */
    private static final int[][] DIR = {
        { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 },
        { 0, -1 }, { -1, -1 }, { -1, 0 }, { -1, 1 }
    };

    /** Return the Square that is STEPS>0 squares away from me in
     *  direction DIR, or null if there is no such square.
     *  DIR = 0 for north, 1 for northeast, 2 for east, etc., up to
     *  7 for northwest. If DIR has another value, return null. Thus,
     *  unless the result is null the resulting square is a queen
     *  move away rom me. */

    Square queenMove(int dir, int steps) {
        int newCol = _col;
        int newRow = _row;
        if (dir == 0) {
            newRow += steps;
        } else if (dir == 1) {
            newCol += steps;
            newRow += steps;
        } else if (dir == 2) {
            newCol += steps;
        } else if (dir == 3) {
            newCol += steps;
            newRow -= steps;
        } else if (dir == 4) {
            newRow -= steps;
        } else if (dir == 5) {
            newCol -= steps;
            newRow -= steps;
        } else if (dir == 6) {
            newCol -= steps;
        } else if (dir == 7) {
            newCol -= steps;
            newRow += steps;
        } else {
            return null;
        }
        if (exists(newCol, newRow)) {
            return sq(newCol, newRow);
        } else {
            return null;
        }
    }

    /** Return the direction (an int as defined in the documentation
     *  for queenMove) of the queen move THIS-TO. */
    int direction(Square to) {
        assert isQueenMove(to);
        if (this._col == to._col) {
            if (this._row < to._row) {
                return 0;
            } else {
                return 4;
            }
        } else if (this._row == to._row) {
            if (this._col < to._col) {
                return 2;
            } else {
                return 6;
            }
        } else {
            if (this._col < to._col) {
                if (this._row < to._row) {
                    return 1;
                } else {
                    return 3;
                }
            } else {
                if (this._row < to._row) {
                    return 7;
                } else {
                    return 5;
                }
            }
        }
    }

    @Override
    public String toString() {
        return _str;
    }

    /** Return true iff COL ROW is a legal square. */
    static boolean exists(int col, int row) {
        return row >= 0 && col >= 0 && row < Board.SIZE && col < Board.SIZE;
    }

    /** Return the (unique) Square denoting COL ROW. */
    static Square sq(int col, int row) {
        if (!exists(row, col)) {
            throw error("row or column out of bounds");
        }
        return sq(row * 10 + col);
    }

    /** Return the (unique) Square denoting the position with index INDEX. */
    static Square sq(int index) {
        return SQUARES[index];
    }

    /** Return the (unique) Square denoting the position COL ROW, where
     *  COL ROW is the standard text format for a square (e.g., a4). */
    static Square sq(String col, String row) {
        int c = col.charAt(0) - ASCII_LOWER_A;
        int r = Integer.parseInt(row);
        return sq(c, r);
    }

    /** Return the (unique) Square denoting the position in POSN, in the
     *  standard text format for a square (e.g. a4). POSN must be a
     *  valid square designation. */
    static Square sq(String posn) {
        assert posn.matches(SQ);
        int c = posn.charAt(0) - ASCII_LOWER_A;
        int r = Integer.parseInt(posn.substring(1)) - 1;
        return sq(c, r);
    }

    /** Return an iterator over all Squares. */
    static Iterator<Square> iterator() {
        return SQUARE_LIST.iterator();
    }

    /** Return the Square with index INDEX. */
    private Square(int index) {
        _index = index;
        _row = index / 10;
        _col = index % 10;
        String row = Integer.toString(_row + 1);
        char col = (char) (_col + ASCII_LOWER_A);
        _str = String.format(col + row);
    }

    /** The cache of all created squares, by index. */
    private static final Square[] SQUARES =
        new Square[Board.SIZE * Board.SIZE];

    /** SQUARES viewed as a List. */
    private static final List<Square> SQUARE_LIST = Arrays.asList(SQUARES);

    static {
        for (int i = Board.SIZE * Board.SIZE - 1; i >= 0; i -= 1) {
            SQUARES[i] = new Square(i);
        }
    }

    /** My index position. */
    private final int _index;

    /** My row and column (redundant, since these are determined by _index). */
    private final int _row, _col;

    /** My String denotation. */
    private final String _str;

    /** Int value used to correctly match the ascii table. */
    private static final int ASCII_LOWER_A = 97;

}
