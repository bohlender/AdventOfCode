import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: split, strip;
import std.algorithm: map;
import std.conv: to;
import std.array: array;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        auto input = contents.strip.split(',').map!(to!uint).array;
        auto res1 = (a => a[0]*a[1])(iota(256u).array.knot(input));
        //auto res2 = garbage(input);
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
import std.algorithm:swap;
import std.range: iota;

static void reverse(uint[] list, in uint pos, in uint length){
    foreach(offset; 0..length/2){
        auto src = (pos + offset) % list.length;
        auto dst = (pos + length - 1 - offset) % list.length;
        swap(list[src], list[dst]);
    }
}

static uint[] knot(in uint[] list, in uint[] lengths){
    auto curList = list.dup;
    uint curPos, skipSize;
    foreach(length; lengths){
        curList.reverse(curPos, length);
        curPos = (curPos + length + skipSize) % list.length;
        ++skipSize;
    }
    return curList;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    uint[] ex = [0,1,2,3,4];
    ex.reverse(0,3); expect([2,1,0,3,4], ex);
    ex.reverse(3,4); expect([4,3,0,1,2], ex);

    expect([3,4,2,1,0], [0,1,2,3,4].knot([3,4,1,5]));
}