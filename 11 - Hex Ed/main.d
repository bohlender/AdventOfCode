import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: split, strip, representation;
import std.algorithm: map;
import std.conv: to;
import std.array: array;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.strip.split(',').map!parseDir;
        auto res1 = input.distance;
        //auto res2 = hash(contents);
        //writefln("First: %s\nSecond: %s", res1, res2);
        writeln(res1);
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
import std.algorithm: max;
import std.math: abs;

alias AxialCoord = Tuple!(int, "x", int, "xy");
enum Dir {N,NE,SE,S,SW,NW}

// Cannot use formattedRead. Bug in std.format
// https://issues.dlang.org/show_bug.cgi?id=18051
static Dir parseDir(in string s){
    if(s == "n") return Dir.N;
    if(s == "ne") return Dir.NE;
    if(s == "se") return Dir.SE;
    if(s == "s") return Dir.S;
    if(s == "sw") return Dir.SW;
    if(s == "nw") return Dir.NW;
    assert(false);
}

static AxialCoord step(in AxialCoord from, in Dir dir){
    final switch(dir) {
        case Dir.N:  return AxialCoord(from.x,   from.xy+1);
        case Dir.S:  return AxialCoord(from.x,   from.xy-1);
        case Dir.NE: return AxialCoord(from.x+1, from.xy+1);
        case Dir.SW: return AxialCoord(from.x-1, from.xy-1);
        case Dir.SE: return AxialCoord(from.x+1, from.xy);
        case Dir.NW: return AxialCoord(from.x-1, from.xy);
    }
}

alias HexCoord = Tuple!(int, "x", int, "y", int, "z");
static HexCoord fromAxial(in AxialCoord pos){return HexCoord(pos.x, -pos.x+pos.xy, pos.xy);}

static uint distance(Range)(Range dirs){
    auto curPos = AxialCoord(0,0);
    foreach(dir; dirs)
        curPos = curPos.step(dir);
    
    auto hexPos = fromAxial(curPos);
    return max(hexPos.x.abs, hexPos.y.abs, hexPos.z.abs);
}

//============================================================================
// Unittests
//============================================================================
unittest{
    expect(3, [Dir.NE,Dir.NE,Dir.NE].distance);
    expect(0, [Dir.NE,Dir.NE,Dir.SW,Dir.SW].distance);
    expect(2, [Dir.NE,Dir.NE,Dir.S,Dir.S].distance);
    expect(3, [Dir.SE,Dir.SW,Dir.SE,Dir.SW,Dir.SW].distance);
}