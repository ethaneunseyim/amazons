package amazons;

import java.util.Iterator;
import static amazons.Piece.*;

/** A Player that automatically generates moves.
 *  @author Ethan Yim
 */
class AI extends Player {

    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;
    /** When the number of moves is greater than this value, use minimax tree
     * of depth 1. */
    private static final int DEPTH_ONE_MOVE_NUM = 0;
    /** When the number of moves is greater than this value, use minimax tree
     * of depth 2. */
    private static final int DEPTH_TWO_MOVE_NUM = 10;
    /** When the number of moves is greater than this value, use minimax tree
     * of depth 3. */
    private static final int DEPTH_THREE_MOVE_NUM = 45;
    /** When the number of moves is greater than this value, use minimax tree
     * of depth 4. */
    private static final int DEPTH_FOUR_MOVE_NUM = 55;
    /** Multiplying this value to the score returned in staticScore method. */
    private static final int SCORE_MULTIPLIER = 100000;
    /** Up to this many moves, use a certain type of heuristic. */
    private static final int USE_UP_TO = 30;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        if (depth == 0 || board.winner() != EMPTY) {
            return staticScore(board);
        }
        int score;
        if (sense == 1) {
            score = -INFTY;
        } else {
            score = INFTY;
        }
        boolean endGame = true;
        Iterator<Move> m = board.legalMoves(myPiece());
        while (m.hasNext()) {
            Move move = m.next();
            Board newBoard = new Board(board);
            newBoard.makeMove(move);
            int value = findMove(newBoard, depth - 1,
                    false, -sense, alpha, beta);
            if (sense == 1) {
                if (value >= score) {
                    score = value;
                    if (saveMove) {
                        _lastFoundMove = move;
                        endGame = false;
                    }
                    alpha = Integer.max(alpha, value);
                }
            } else {
                if (value <= score) {
                    score = value;
                    if (saveMove) {
                        _lastFoundMove = move;
                        endGame = false;
                    }
                    beta = Integer.min(beta, value);
                }
            }
            if (_controller.board().winner() != EMPTY) {
                break;
            }
            if (alpha >= beta) {
                break;
            }
        }
        return score;
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private int maxDepth(Board board) {
        int N = board.numMoves();
        if (N > DEPTH_FOUR_MOVE_NUM) {
            return 4;
        }
        if (N > DEPTH_THREE_MOVE_NUM) {
            return 3;
        }
        if (N > DEPTH_TWO_MOVE_NUM) {
            return 2;
        } else {
            return 1;
        }
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        Piece winner = board.winner();
        if (winner == BLACK) {
            return -WINNING_VALUE;
        } else if (winner == WHITE) {
            return WINNING_VALUE;
        }
        int score = 0;
        int N = board.numMoves();
        Piece p = myPiece();
        if (N < 10) {
            score = board.getLocValue(p);
            if (myPiece() == BLACK) {
                score = -1 * score;
            }
        } else if (N < USE_UP_TO) {
            score = SCORE_MULTIPLIER * board.getEmptySurrounding(p)
                    + board.getLocValue(p);
        } else if (N < DEPTH_THREE_MOVE_NUM) {
            for (int i = 0; i < 4; i++) {
                Iterator<Square> s;
                if (myPiece() == WHITE) {
                    s = board.reachableFrom(board.getWhiteLoc()[i], null);

                } else {
                    s = board.reachableFrom(board.getBlackLoc()[i], null);
                }
                for (; s.hasNext(); score++) {
                    s.next();
                }
            }
        } else {
            Iterator<Move> m = board.legalMoves(myPiece());
            score = 0;
            for ( ; m.hasNext(); score++) {
                m.next();
            }
        }
        return score;
    }

}
