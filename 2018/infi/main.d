import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.utf;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const content = readText(args[1]).toUTF16;
        auto input = content.parse;
        auto goal = Pos(content.splitLines[0].length-1, content.splitLines.length-1);

        auto res1 = shortestPath(new State(input,Pos(0,0)), goal);
        "Shortest path: %s (%d steps)\n".writefln(res1, res1.length-1);
        
        auto res2 = shortestPath(new State2(input,Pos(0,0)), goal);
        "Shortest path: %s (%d steps)".writefln(res2, res2.length-1);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.typecons;
import std.string;
import std.conv;
import std.container;
import std.math;
import std.algorithm;
import std.range: retro;
import std.array: appender;

struct Pos{
    ulong x,y; // (0,0) is upper-left
}

enum Dir {N=1, E=2, S=4, W=8};
alias Cell = BitFlags!Dir;

class State {
    const Board board;
    Pos pos;

    this(in Board board, in Pos pos){
        this.board = board;
        this.pos = pos;
    }

    const(State)[] successors() const{
        auto x = pos.x;
        auto y = pos.y;
        auto cell = board(x, y);

        State[] res;
        if(y>0 && (cell & Dir.N) && (board(x,y-1) & Dir.S))              // N
            res ~= new State(board, Pos(x,y-1));
        if(y<board.height-1 && (cell & Dir.S) && (board(x,y+1) & Dir.N)) // S
            res ~= new State(board, Pos(x,y+1));
        if(x>0 && (cell & Dir.W) && (board(x-1,y) & Dir.E))              // W
            res ~= new State(board, Pos(x-1,y));
        if(x<board.width-1 && (cell & Dir.E) && (board(x+1,y) & Dir.W))  // E
            res ~= new State(board, Pos(x+1,y));
        return res;
    }

    override bool opEquals(Object other) {
        if(auto s = cast(State)other)
            return board == s.board && pos == s.pos;
        return super.opEquals(other);
    }

    override string toString() const {
        return pos.to!string;
    }
}

auto shortestPath(in State initial, in Pos dst){ // Plain BFS
    State[const(State)] cameFrom;
    auto reconstructPath(in State cur){
        auto res = [cur];
        while (res[$-1] in cameFrom)
            res ~= cameFrom[res[$-1]];
        return res.retro;
    }

    // TODO: Use ordered set eventually (std.container.RedBlackTree is broken though)
    const(State)[] visited;
    auto worklist = [initial];

    while(!worklist.empty){
        const cur = worklist[0];
        worklist = worklist[1..$];
        visited ~= cur;
        
        if (cur.pos == dst)
            return reconstructPath(cur);

        foreach(const succ; cur.successors){
            if (visited.canFind(succ))
                continue;
            if (!worklist.canFind(succ))
                worklist ~= succ;
            cameFrom[succ] = cast(State)cur;
        }
    }
    assert(false);
}

struct Board{
    const ulong width;
    const ulong height;
    private const Cell[] cells;

    this(in ulong width, in ulong height, in Cell[] cells){
        this.width = width;
        this.height = height;
        this.cells = cells;
    }

    auto opCall(in ulong x, in ulong y) const {
        assert(0<=x && x<width && 0<=y && y<height, "(%d, %d) is out of bounds (width: %d, height: %d)".format(x, y, width, height));
        return cells[y*height + x];
    }

    auto shiftRow(in ulong y) const {
        assert(y<height);
        Cell[] shiftedCells = cells.dup;
        foreach(x; 0..width)
            shiftedCells[y*height + (x+1)%width] = cells[y*height + x];
        return Board(width, height, shiftedCells);
    }

    auto shiftColumn(in ulong x) const {
        assert(x<width);
        Cell[] shiftedCells = cells.dup;
        foreach(y; 0..height)
            shiftedCells[(y+1)%height*height + x] = cells[y*height + x];
        return Board(width, height, shiftedCells);
    }
}

Cell char2cell(in wchar c){
    switch(c){
        case '╔': return Cell(Dir.E | Dir.S);
        case '║': return Cell(Dir.N | Dir.S);
        case '╗': return Cell(Dir.S | Dir.W);
        case '╠': return Cell(Dir.N | Dir.E | Dir.S);
        case '╦': return Cell(Dir.E | Dir.S | Dir.W);
        case '╚': return Cell(Dir.N | Dir.E);
        case '╝': return Cell(Dir.N | Dir.W);
        case '╬': return Cell(Dir.N | Dir.E | Dir.S | Dir.W);
        case '╩': return Cell(Dir.N | Dir.E | Dir.W);
        case '═': return Cell(Dir.E | Dir.W);
        case '╣': return Cell(Dir.N | Dir.S | Dir.W);
        default: assert(false, "Unexpected symbol: %s".format(c));
    }
}

auto parse(in wstring content){
    const lines = content.splitLines;
    const height = lines.length;
    const width = lines[0].length;

    Cell[] cells;
    foreach(line; lines)
        foreach(c; line)
            cells ~= char2cell(c);
    return Board(width, height, cells);
}

unittest{
    const input =
`╔═╗║
╠╗╠║
╬╬╣╬
╚╩╩═`.to!wstring;
    auto board = input.parse;
    { // Check neighbours
        auto s = new State(board, Pos(1,1));
        auto succs = s.successors();
        assert(!succs.canFind!(s => s.pos == Pos(1,0)));
        assert(!succs.canFind!(s => s.pos == Pos(2,1)));
        assert(succs.canFind!(s => s.pos == Pos(1,2)));
        assert(succs.canFind!(s => s.pos == Pos(0,1)));
    }
    { // Check shortest path (7 nodes, 6 steps)
        auto initial = new State(board, Pos(0,0));
        auto p = shortestPath(initial, Pos(3,3));
        assert(p.length == 7);
    }
}

//============================================================================
// Puzzle 2
//============================================================================
class State2 : State {
    ulong depth;    

    this(in Board board, in Pos pos, in ulong depth = 0){
        super(board, pos);
        this.depth = depth;
    }

    override const(State2)[] successors() const{
        const x = pos.x;
        const y = pos.y;
        const cell = board(x, y);

        assert(board.width == board.height);
        auto shiftIdx = depth%board.width;
        auto shiftRow = depth%2==0;
        auto newBoard = shiftRow ? board.shiftRow(shiftIdx) : board.shiftColumn(shiftIdx);

        State2[] res;
        if(y>0 && (cell & Dir.N) && (board(x,y-1) & Dir.S)){                   // N
            auto xOffset = (shiftRow && shiftIdx == y-1) ? 1 : 0;
            auto yOffset = (!shiftRow && shiftIdx == x) ? 1 : 0;
            res ~= new State2(newBoard, Pos((x+xOffset)%board.width,(y-1+yOffset)%board.height), depth+1);
        }
        if(y<board.height-1 && (cell & Dir.S) && (board(x,y+1) & Dir.N)){      // S
            auto xOffset = (shiftRow && shiftIdx == y+1) ? 1 : 0;
            auto yOffset = (!shiftRow && shiftIdx == x) ? 1 : 0;
            res ~= new State2(newBoard, Pos((x+xOffset)%board.width,(y+1+yOffset)%board.height), depth+1);
        }
        if(x>0 && (cell & Dir.W) && (board(x-1,y) & Dir.E)){                   // W
            auto xOffset = (shiftRow && shiftIdx == y) ? 1 : 0;
            auto yOffset = (!shiftRow && shiftIdx == x-1) ? 1 : 0;
            res ~= new State2(newBoard, Pos((x-1+xOffset)%board.width,(y+yOffset)%board.height), depth+1);
        }
        if(x<board.width-1 && (cell & Dir.E) && (board(x+1,y) & Dir.W)){       // E
            auto xOffset = (shiftRow && shiftIdx == y) ? 1 : 0;
            auto yOffset = (!shiftRow && shiftIdx == x+1) ? 1 : 0;
            res ~= new State2(newBoard, Pos((x+1+xOffset)%board.width,(y+yOffset)%board.height), depth+1);
        }
        return res;
    }
}

unittest{
    const input =
`╔═╗║
╠╗╠║
╬╬╣╬
╚╩╩═`.to!wstring;
    auto board = input.parse;
    { // Check shift
        assert(board.shiftRow(0)(3,0) == board(2,0));
        assert(board.shiftRow(2)(3,2) == board(2,2));
        assert(board.shiftColumn(0)(0,3) == board(0,2));
        assert(board.shiftColumn(2)(2,3) == board(2,2));
    }
    { // Check shortest path (5 nodes, 4 steps)
        auto initial = new State2(board, Pos(0,0));
        auto p = shortestPath(initial, Pos(3,3));
        assert(p.length == 5);
    }
}