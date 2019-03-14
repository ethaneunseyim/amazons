package amazons;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.Map;

import static amazons.Piece.*;
import static amazons.Move.mv;


/** The state of an Amazons Game.
 *  @author Ethan Yim
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 10;
    /** The value of the inner four squares. */
    static final int FIRST_INNER_SCORE = 100000;
    /** The value of the second inner squares. */
    static final int SECOND_INNER_SCORE = 10000;

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        this._turn = model._turn;
        this._winner = model._winner;
        this._move = new Stack<Move>();
        this._move.addAll(model._move);
        this._piece = new Piece[model._piece.length][];
        for (int i = 0; i < model._piece.length; i++) {
            this._piece[i] = model._piece[i].clone();
        }
        this.whiteLoc = model.whiteLoc.clone();
        this.blackLoc = model.blackLoc.clone();
    }

    /** Clears the board to the initial position. */
    void init() {
        whiteLoc = new Square[4];
        whiteLoc[0] = Square.sq("a4");
        whiteLoc[1] = Square.sq("d1");
        whiteLoc[2] = Square.sq("g1");
        whiteLoc[3] = Square.sq("j4");
        blackLoc = new Square[4];
        blackLoc[0] = Square.sq("a7");
        blackLoc[1] = Square.sq("d10");
        blackLoc[2] = Square.sq("g10");
        blackLoc[3] = Square.sq("j7");
        _move = new Stack<Move>();
        _piece = new Piece[Board.SIZE][Board.SIZE];
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                put(EMPTY, col, row);
            }
        }
        put(WHITE, 0, 3);
        put(WHITE, 3, 0);
        put(WHITE, 6, 0);
        put(WHITE, 9, 3);
        put(BLACK, 0, 6);
        put(BLACK, 3, 9);
        put(BLACK, 6, 9);
        put(BLACK, 9, 6);
        _turn = WHITE;
        _winner = EMPTY;
    }

    /** Return the Piece whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the number of moves (that have not been undone) for this
     *  board. */
    int numMoves() {
        return _move.size();
    }

    /** Return the winner in the current position, or null if the game is
     *  not yet finished. */
    Piece winner() {
        return _winner;
    }

    /** Sets the winner. */
    void setWinner() {
        if (_turn == WHITE) {
            _winner = BLACK;
        } else {
            _winner = WHITE;
        }
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return _piece[row][col];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        put(p, s.col(), s.row());
    }

    /** Set square (COL, ROW) to P. */
    final void put(Piece p, int col, int row) {
        _piece[row][col] = p;
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, col - 'a', row - '1');
    }

    /** Return true iff FROM - TO is an unblocked queen move on the current
     *  board, ignoring the contents of ASEMPTY, if it is encountered.
     *  For this to be true, FROM-TO must be a queen move and the
     *  squares along it, other than FROM and ASEMPTY, must be
     *  empty. ASEMPTY may be null, in which case it has no effect. */
    boolean isUnblockedMove(Square from, Square to, Square asEmpty) {
        if (from.isQueenMove(to)) {
            int direction = from.direction(to);
            int howFarApart = Math.max(Math.abs(from.col() - to.col()),
                    Math.abs(from.row() - to.row()));
            Square nextSquare;
            for (int steps = 1; steps <= howFarApart; steps++) {
                nextSquare = from.queenMove(direction, steps);
                if (!nextSquare.equals(asEmpty) && get(nextSquare) != EMPTY) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        if (get(from).equals(WHITE) || get(from).equals(BLACK)) {
            return get(from).equals(_turn);
        }
        return false;
    }

    /** Return true iff FROM-TO is a valid first part of move, ignoring
     *  spear throwing. */
    boolean isLegal(Square from, Square to) {
        if (isLegal(from)) {
            return isUnblockedMove(from, to, null);
        }
        return false;
    }

    /** Return true iff FROM-TO(SPEAR) is a legal move in the current
     *  position. */
    boolean isLegal(Square from, Square to, Square spear) {
        return isLegal(from, to) && isUnblockedMove(to, spear, from);
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to(), move.spear());
    }

    /** Move FROM-TO(SPEAR), assuming this is a legal move. */
    void makeMove(Square from, Square to, Square spear) {
        _move.push(mv(from, to, spear));
        put(_turn, to);
        put(EMPTY, from);
        put(SPEAR, spear);
        updateQueenLoc(from, to);
        if (_turn.equals(WHITE)) {
            _turn = BLACK;
        } else {
            _turn = WHITE;
        }
    }

    /** Updates the arrays that contain the location (Squares) of
     *  the queens.
     *  @param from The square that the queen was initially in.
     *  @param to The squar that the queen moved to. */
    private void updateQueenLoc(Square from, Square to) {
        if (_turn.equals(WHITE)) {
            if (whiteLoc[0].equals(from)) {
                whiteLoc[0] = to;
            } else if (whiteLoc[1].equals(from)) {
                whiteLoc[1] = to;
            } else if (whiteLoc[2].equals(from)) {
                whiteLoc[2] = to;
            } else {
                whiteLoc[3] = to;
            }
        } else {
            if (blackLoc[0].equals(from)) {
                blackLoc[0] = to;
            } else if (blackLoc[1].equals(from)) {
                blackLoc[1] = to;
            } else if (blackLoc[2].equals(from)) {
                blackLoc[2] = to;
            } else {
                blackLoc[3] = to;
            }
        }
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to(), move.spear());
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (numMoves() >= 2) {
            for (int i = 0; i < 2; i++) {
                Move m = _move.pop();
                put(EMPTY, m.spear());
                put(get(m.to()), m.from());
                put(EMPTY, m.to());
                if (_turn.equals(WHITE)) {
                    _turn = BLACK;
                } else {
                    _turn = WHITE;
                }
                updateQueenLoc(m.to(), m.from());
            }
        }
    }

    /** Returns the value of the square.
     *  @param p The piece (BLACK or WHITE). */
    int getLocValue(Piece p) {
        int value = 1;
        for (int i = 0; i < 4; i++) {
            if (p == WHITE) {
                value *= LOC_VALUE.get(whiteLoc[i]);
            } else {
                value *= LOC_VALUE.get(blackLoc[i]);
            }
        }
        return value;
    }

    /** Returns an array with the location of WHITE queens. */
    Square[] getWhiteLoc() {
        return whiteLoc;
    }

    /** Returns an array with the location of BLACK queens. */
    Square[] getBlackLoc() {
        return blackLoc;
    }


    /** Returns the number of empty squares around queens.
     * @param p Either WHITE or BLACK
     * @return The number of empty squares around sq. */
    int getEmptySurrounding(Piece p) {
        int count = 0;
        if (p == WHITE) {
            for (int i = 0; i < whiteLoc.length; i++) {
                Square queen = whiteLoc[i];
                for (int dir = 0; dir < 8; dir++) {
                    Square possible = queen.queenMove(dir, 1);
                    if (possible != null && get(possible) == EMPTY) {
                        count++;
                    }
                }
            }
        } else {
            for (int i = 0; i < blackLoc.length; i++) {
                Square queen = blackLoc[i];
                for (int dir = 0; dir < 8; dir++) {
                    Square possible = queen.queenMove(dir, 1);
                    if (possible != null && get(possible) == EMPTY) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /** Return an Iterator over the Squares that are reachable by an
     *  unblocked queen move from FROM. Does not pay attention to what
     *  piece (if any) is on FROM, nor to whether the game is finished.
     *  Treats square ASEMPTY (if non-null) as if it were EMPTY.  (This
     *  feature is useful when looking for Moves, because after moving a
     *  piece, one wants to treat the Square it came from as empty for
     *  purposes of spear throwing.) */
    Iterator<Square> reachableFrom(Square from, Square asEmpty) {
        return new ReachableFromIterator(from, asEmpty);
    }

    /** Return an Iterator over all legal moves on the current board. */
    Iterator<Move> legalMoves() {
        return new LegalMoveIterator(_turn);
    }

    /** Return an Iterator over all legal moves on the current board for
     *  SIDE (regardless of whose turn it is). */
    Iterator<Move> legalMoves(Piece side) {
        return new LegalMoveIterator(side);
    }

    /** An iterator used by reachableFrom. */
    private class ReachableFromIterator implements Iterator<Square> {

        /** Iterator of all squares reachable by queen move from FROM,
         *  treating ASEMPTY as empty. */
        ReachableFromIterator(Square from, Square asEmpty) {
            _from = from;
            _dir = -1;
            _steps = 0;
            _asEmpty = asEmpty;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _dir < 8;
        }

        @Override
        public Square next() {
            Square reachable = _from.queenMove(_dir, _steps);
            toNext();
            return reachable;
        }

        /** Advance _dir and _steps, so that the next valid Square is
         *  _steps steps in direction _dir from _from. */
        private void toNext() {
            _steps++;
            _reachable = _from.queenMove(_dir, _steps);
            while (!isUnblockedMove(_from, _reachable, _asEmpty)) {
                _dir++;
                if (_dir == 8) {
                    break;
                }
                _steps = 1;
                _reachable = _from.queenMove(_dir, _steps);
            }
        }

        /** Starting square. */
        private Square _from;
        /** Current direction. */
        private int _dir;
        /** Current distance. */
        private int _steps;
        /** Square treated as empty. */
        private Square _asEmpty;
        /** Square that will be returned by the next method. */
        private Square _reachable;
    }


    /** An iterator used by legalMoves. */
    private class LegalMoveIterator implements Iterator<Move> {

        /** Initializes an iterator that iterates through all the possible
         *  moves WHITE or BLACK can make.
         *  @param side Either WHITE or BLACJ. */
        LegalMoveIterator(Piece side) {
            _squaresWithQueen = new ArrayList<Square>();
            _startingSquares = Square.iterator();
            _fromPiece = side;
            while (_startingSquares.hasNext()) {
                Square s = _startingSquares.next();
                if (get(s.col(), s.row()).equals(_fromPiece)) {
                    _squaresWithQueen.add(s);
                }
                if (_squaresWithQueen.size() == 4) {
                    break;
                }
            }
            _spearThrows = NO_SQUARES;
            _pieceMoves = NO_SQUARES;
            _more = false;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _more;
        }

        @Override
        public Move next() {
            Move m = Move.mv(_queenFromSquare, _queenToSquare, _spearToSquare);
            toNext();
            return m;
        }

        /** Advance so that the next valid Move is
         *  _start-_nextSquare(sp), where sp is the next value of
         *  _spearThrows. */
        private void toNext() {
            if (_pieceMoves.equals(NO_SQUARES)
                    && _squaresWithQueen.size() > 0) {
                _queenFromSquare = _squaresWithQueen.remove(0);
                _pieceMoves = reachableFrom(_queenFromSquare, null);
                _more = true;
                while (!_pieceMoves.hasNext() && _more) {
                    if (_squaresWithQueen.size() == 0) {
                        _more = false;
                    } else {
                        _queenFromSquare = _squaresWithQueen.remove(0);
                        _pieceMoves = reachableFrom(_queenFromSquare, null);
                    }
                }
                if (_more) {
                    if (_pieceMoves.hasNext()) {
                        _queenToSquare = _pieceMoves.next();
                        _spearThrows =
                                reachableFrom(_queenToSquare, _queenFromSquare);
                    }
                }
            }
            while (_more) {
                if (_spearThrows.hasNext()) {
                    _spearToSquare = _spearThrows.next();
                    break;
                } else {
                    if (_pieceMoves.hasNext()) {
                        _queenToSquare = _pieceMoves.next();
                        _spearThrows =
                                reachableFrom(_queenToSquare, _queenFromSquare);
                    } else {
                        while (!_pieceMoves.hasNext() && _more) {
                            if (_squaresWithQueen.size() == 0) {
                                _more = false;
                            } else {
                                _queenFromSquare = _squaresWithQueen.remove(0);
                                _pieceMoves =
                                        reachableFrom(_queenFromSquare, null);
                            }
                        }
                        if (_more) {
                            if (_pieceMoves.hasNext()) {
                                _queenToSquare = _pieceMoves.next();
                                _spearThrows =
                                        reachableFrom(_queenToSquare,
                                                        _queenFromSquare);
                            }
                        }
                    }
                }

            }
        }

        /** Color of side whose moves we are iterating. */
        private Piece _fromPiece;
        /** Remaining starting squares to consider. */
        private Iterator<Square> _startingSquares;
        /** Current piece's new position. */
        private Iterator<Square> _pieceMoves;
        /** Remaining spear throws from _piece to consider. */
        private Iterator<Square> _spearThrows;
        /** A list containing all the squares that have a queen
         *  (with the correct color) on that location. */
        private List<Square> _squaresWithQueen;
        /** A boolean that represents if it can iterate through more
         *  moves. */
        private boolean _more;
        /** The square that the queen is moving from. */
        private Square _queenFromSquare;
        /** The square that the queen is moving to. */
        private Square _queenToSquare;
        /** The square that the spear is being thrown to. */
        private Square _spearToSquare;
    }

    @Override
    public String toString() {
        String s = "";
        for (int row = Board.SIZE - 1; row >= 0; row--) {
            s += "  ";
            for (int col = 0; col < Board.SIZE; col++) {
                s += " " + get(col, row).toString();
            }
            s += "\n";
        }
        return s;
    }

    /** An empty iterator for initialization. */
    private static final Iterator<Square> NO_SQUARES =
        Collections.emptyIterator();

    /** Piece whose turn it is (BLACK or WHITE). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;

    /** The 2D array representation of where the Pieces are on the board.
     *  The array is ordered with indicies [row][col]. */
    private Piece[][] _piece;

    /** The stack representing all the moves made in the game. */
    private Stack<Move> _move;

    /** The location of the four white queen pieces. */
    private Square[] whiteLoc;

    /** The location of the four black queen pieces. */
    private Square[] blackLoc;

    /** The value of the squares, the higher the value, the more desirable
     *  for the queen to be there. The middle 4 squares have highest values,
     *  and as it expands outwards, the value decreases. */
    private static final Map<Square, Integer> LOC_VALUE = new HashMap<>();

    static {
        LOC_VALUE.put(Square.sq("a1"), 1);
        LOC_VALUE.put(Square.sq("a2"), 1);
        LOC_VALUE.put(Square.sq("a3"), 1);
        LOC_VALUE.put(Square.sq("a4"), 1);
        LOC_VALUE.put(Square.sq("a5"), 1);
        LOC_VALUE.put(Square.sq("a6"), 1);
        LOC_VALUE.put(Square.sq("a7"), 1);
        LOC_VALUE.put(Square.sq("a8"), 1);
        LOC_VALUE.put(Square.sq("a9"), 1);
        LOC_VALUE.put(Square.sq("a10"), 1);
        LOC_VALUE.put(Square.sq("b1"), 1);
        LOC_VALUE.put(Square.sq("c1"), 1);
        LOC_VALUE.put(Square.sq("d1"), 1);
        LOC_VALUE.put(Square.sq("e1"), 1);
        LOC_VALUE.put(Square.sq("f1"), 1);
        LOC_VALUE.put(Square.sq("g1"), 1);
        LOC_VALUE.put(Square.sq("h1"), 1);
        LOC_VALUE.put(Square.sq("i1"), 1);
        LOC_VALUE.put(Square.sq("j1"), 1);
        LOC_VALUE.put(Square.sq("j2"), 1);
        LOC_VALUE.put(Square.sq("j3"), 1);
        LOC_VALUE.put(Square.sq("j4"), 1);
        LOC_VALUE.put(Square.sq("j5"), 1);
        LOC_VALUE.put(Square.sq("j6"), 1);
        LOC_VALUE.put(Square.sq("j7"), 1);
        LOC_VALUE.put(Square.sq("j8"), 1);
        LOC_VALUE.put(Square.sq("j9"), 1);
        LOC_VALUE.put(Square.sq("j10"), 1);
        LOC_VALUE.put(Square.sq("b10"), 1);
        LOC_VALUE.put(Square.sq("c10"), 1);
        LOC_VALUE.put(Square.sq("d10"), 1);
        LOC_VALUE.put(Square.sq("e10"), 1);
        LOC_VALUE.put(Square.sq("f10"), 1);
        LOC_VALUE.put(Square.sq("g10"), 1);
        LOC_VALUE.put(Square.sq("h10"), 1);
        LOC_VALUE.put(Square.sq("i10"), 1);

        LOC_VALUE.put(Square.sq("b2"), 10);
        LOC_VALUE.put(Square.sq("b3"), 10);
        LOC_VALUE.put(Square.sq("b4"), 10);
        LOC_VALUE.put(Square.sq("b5"), 10);
        LOC_VALUE.put(Square.sq("b6"), 10);
        LOC_VALUE.put(Square.sq("b7"), 10);
        LOC_VALUE.put(Square.sq("b8"), 10);
        LOC_VALUE.put(Square.sq("b9"), 10);
        LOC_VALUE.put(Square.sq("c2"), 10);
        LOC_VALUE.put(Square.sq("d2"), 10);
        LOC_VALUE.put(Square.sq("e2"), 10);
        LOC_VALUE.put(Square.sq("f2"), 10);
        LOC_VALUE.put(Square.sq("g2"), 10);
        LOC_VALUE.put(Square.sq("h2"), 10);
        LOC_VALUE.put(Square.sq("i2"), 10);
        LOC_VALUE.put(Square.sq("i3"), 10);
        LOC_VALUE.put(Square.sq("i4"), 10);
        LOC_VALUE.put(Square.sq("i5"), 10);
        LOC_VALUE.put(Square.sq("i6"), 10);
        LOC_VALUE.put(Square.sq("i7"), 10);
        LOC_VALUE.put(Square.sq("i8"), 10);
        LOC_VALUE.put(Square.sq("i9"), 10);
        LOC_VALUE.put(Square.sq("c9"), 10);
        LOC_VALUE.put(Square.sq("d9"), 10);
        LOC_VALUE.put(Square.sq("e9"), 10);
        LOC_VALUE.put(Square.sq("f9"), 10);
        LOC_VALUE.put(Square.sq("g9"), 10);
        LOC_VALUE.put(Square.sq("h9"), 10);

        LOC_VALUE.put(Square.sq("c3"), 1000);
        LOC_VALUE.put(Square.sq("c4"), 1000);
        LOC_VALUE.put(Square.sq("c5"), 1000);
        LOC_VALUE.put(Square.sq("c6"), 1000);
        LOC_VALUE.put(Square.sq("c7"), 1000);
        LOC_VALUE.put(Square.sq("c8"), 1000);
        LOC_VALUE.put(Square.sq("d3"), 1000);
        LOC_VALUE.put(Square.sq("e3"), 1000);
        LOC_VALUE.put(Square.sq("f3"), 1000);
        LOC_VALUE.put(Square.sq("g3"), 1000);
        LOC_VALUE.put(Square.sq("h3"), 1000);
        LOC_VALUE.put(Square.sq("h4"), 1000);
        LOC_VALUE.put(Square.sq("h5"), 1000);
        LOC_VALUE.put(Square.sq("h6"), 1000);
        LOC_VALUE.put(Square.sq("h7"), 1000);
        LOC_VALUE.put(Square.sq("h8"), 1000);
        LOC_VALUE.put(Square.sq("d8"), 1000);
        LOC_VALUE.put(Square.sq("e8"), 1000);
        LOC_VALUE.put(Square.sq("f8"), 1000);
        LOC_VALUE.put(Square.sq("g8"), 1000);

        LOC_VALUE.put(Square.sq("d4"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("d5"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("d6"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("d7"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("e4"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("f4"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("g4"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("g5"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("g6"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("g7"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("e7"), SECOND_INNER_SCORE);
        LOC_VALUE.put(Square.sq("f7"), SECOND_INNER_SCORE);

        LOC_VALUE.put(Square.sq("e5"), FIRST_INNER_SCORE);
        LOC_VALUE.put(Square.sq("e6"), FIRST_INNER_SCORE);
        LOC_VALUE.put(Square.sq("f5"), FIRST_INNER_SCORE);
        LOC_VALUE.put(Square.sq("f6"), FIRST_INNER_SCORE);
    }

}
