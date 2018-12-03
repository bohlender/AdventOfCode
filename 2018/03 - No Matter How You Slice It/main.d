import std.stdio;
import std.file;
import std.format;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const content = readText(args[1]);
        auto input = content.parse;
        auto res1 = input.intersectingSqInches;
        auto res2 = input.nonOverlapping;
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.string;
import std.algorithm;
import std.range;

auto intersectingSqInches(in Square[] input){
    auto width = input.map!(sq => sq.right).maxElement;
    auto height = input.map!(sq => sq.bottom).maxElement;
    auto board = new int[width*height];

    foreach(sq; input)
        foreach(y; sq.top..sq.bottom)
            board[y*width + sq.left .. y*width + sq.right] += 1;
    return board.count!(x => x>1);
}

struct Square{
    int id, x, y, width, height;

    @property left() const {return x;}
    @property right() const {return x+width;}
    @property top() const {return y;}
    @property bottom() const {return y+height;}
}

auto parse(in string s){
    Square[] res;
    foreach(line; s.splitLines){
        Square sq;
        line.formattedRead!"#%d @ %d,%d: %dx%d"(sq.id, sq.x, sq.y, sq.width, sq.height);
        res ~= sq;
    }
    return res;
}

//============================================================================
// Puzzle 2
//============================================================================
import std.container;

auto nonOverlapping(in Square[] input){
    auto width = input.map!(sq => sq.right).maxElement;
    auto height = input.map!(sq => sq.bottom).maxElement;
    auto board = new int[width*height];

    auto intersectingIds = new RedBlackTree!int;
    foreach(sq; input)
        foreach(y; sq.top..sq.bottom)
            foreach(x; sq.left..sq.right){
                if(board[y*width+x] != 0){
                    intersectingIds.insert(sq.id);
                    intersectingIds.insert(board[y*width+x]);
                }
                board[y*width+x] = sq.id;
            }
    return input.find!(sq => sq.id !in intersectingIds)[0].id;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto content = `#1 @ 1,3: 4x4
#2 @ 3,1: 4x4
#3 @ 5,5: 2x2`;
    auto input = content.parse;
    expect(input[0], Square(1,1,3,4,4));
    expect(input[1], Square(2,3,1,4,4));
    expect(input[2], Square(3,5,5,2,2));

    expect(4, input.intersectingSqInches);
}

unittest{
    auto content = `#1 @ 1,3: 4x4
#2 @ 3,1: 4x4
#3 @ 5,5: 2x2`;
    auto input = content.parse;
    expect(3, input.nonOverlapping);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}