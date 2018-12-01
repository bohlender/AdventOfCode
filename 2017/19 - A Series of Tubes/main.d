import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: strip, splitLines;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.splitLines;
        auto entryPos = input.findEntry;
        auto res1 = entryPos.lettersOnPath(input);
        auto res2 = entryPos.numSteps(input);
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}

//============================================================================
// Puzzle 1
//============================================================================
import std.typecons: Tuple;
import std.uni: isAlpha;
import std.algorithm: any;
import std.conv: to;

enum Dir {UP, RIGHT, DOWN, LEFT};
alias Vec2D = Tuple!(uint, "x", uint, "y");

Vec2D move(in Vec2D pos, in Dir dir){
    final switch(dir){
        case Dir.UP:    return Vec2D(pos.x, pos.y-1);
        case Dir.RIGHT: return Vec2D(pos.x+1, pos.y);
        case Dir.DOWN:  return Vec2D(pos.x, pos.y+1);
        case Dir.LEFT:  return Vec2D(pos.x-1, pos.y);
    }
}

struct State{
    Vec2D pos;
    Dir dir;

    State step(in string[] lines) const{
        auto c = lines[pos.y][pos.x];
        if("-|".any!(a => a==c) || c.isAlpha) // Keep moving in current direction
            return State(pos.move(dir), dir);
        else if (c == '+'){ // Move around corner
            final switch(dir) {
                case Dir.UP, Dir.DOWN: // Try left & right
                    return pos.move(Dir.LEFT).isOnPath(lines) ? State(pos.move(Dir.LEFT), Dir.LEFT) : State(pos.move(Dir.RIGHT), Dir.RIGHT);
                case Dir.LEFT, Dir.RIGHT: // Try up & down
                    return pos.move(Dir.UP).isOnPath(lines) ? State(pos.move(Dir.UP), Dir.UP) : State(pos.move(Dir.DOWN), Dir.DOWN);
            }
        }else assert(false);
    }
}

bool isPath(in char c) pure {return c != ' ';}

bool isOnPath(in Vec2D pos, in string[] lines) pure{
    if(pos.x < 0 || pos.x >= lines[0].length || pos.y < 0 || pos.y >= lines.length) return false;
    return lines[pos.y][pos.x].isPath;
}

Vec2D findEntry(in string[] lines) pure {
    foreach(i, c; lines[0])
        if(c.isPath)
            return Vec2D(i, 0);
    assert(false);
}

auto lettersOnPath(in Vec2D entry, in string[] lines){
    auto s = State(entry, Dir.DOWN);
    string res;

    while(s.pos.isOnPath(lines)){
        auto c = lines[s.pos.y][s.pos.x];
        if(c.isAlpha)
            res ~= c;
        s = s.step(lines);    
    }

    return res;
}

//============================================================================
// Puzzle 2
//============================================================================
auto numSteps(in Vec2D entry, in string[] lines){
    auto s = State(entry, Dir.DOWN);
    uint res;

    while(s.pos.isOnPath(lines)){
        s = s.step(lines);
        ++res;
    }
    return res;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto contents = r"     |          
     |  +--+    
     A  |  C    
 F---|----E|--+ 
     |  |  |  D 
     +B-+  +--+ 
";
    auto input = contents.splitLines;
    auto entry = input.findEntry;
    expect(Vec2D(5,0), entry);
    expect("ABCDEF", entry.lettersOnPath(input));

    expect(38, entry.numSteps(input));
}