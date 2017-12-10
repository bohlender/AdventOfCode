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
        auto input = contents.strip.split(',').map!(to!uint).array;
        auto res1 = (a => a[0]*a[1])(iota(256u).array.knot(input));
        auto res2 = hash(contents);
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
import std.algorithm:swap;
import std.range: iota;
import std.traits: isUnsigned;

static void reverse(E)(E[] list, in uint pos, in uint length){
    foreach(offset; 0..length/2){
        auto src = (pos + offset) % list.length;
        auto dst = (pos + length - 1 - offset) % list.length;
        swap(list[src], list[dst]);
    }
}

static E[] knot(E,L)(in E[] list, in L[] lengths, uint curPos = 0, uint skipSize = 0){
    return knot(list, lengths, curPos, skipSize);
}
static E[] knot(E,L)(in E[] list, in L[] lengths, ref uint curPos, ref uint skipSize){
    auto curList = list.dup;
    foreach(length; lengths){
        curList.reverse(curPos, length);
        curPos = (curPos + length + skipSize) % list.length;
        ++skipSize;
    }
    return curList;
}

//============================================================================
// Puzzle 2
//============================================================================
import std.range: chunks;
import std.algorithm: reduce;
import std.digest.digest: toHexString;
import std.uni: toLower;

static string hash(in string input){
    ubyte[] suffix = [17, 31, 73, 47, 23];
    auto lengths = input.strip.representation ~ suffix;
    
    uint curPos, skipSize;
    auto numbers = iota(256u).array;
    foreach(_; 0..64)
        numbers = numbers.knot(lengths, curPos, skipSize);

    auto denseHash = numbers.chunks(16).map!(reduce!((a,b) => a^b));
    return denseHash.array.to!(ubyte[]).toHexString.toLower;
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

unittest{
    expect("a2582a3a0e66e6e86e3812dcb672a272", "".hash);
    expect("33efeb34ea91902bb2f59c9920caa6cd", "AoC 2017".hash);
    expect("3efbe78a8d82f29979031a4aa0b16a9d", "1,2,3".hash);
    expect("63960835bcdc130f0b66d7ff4f6a5a8e", "1,2,4".hash);
}