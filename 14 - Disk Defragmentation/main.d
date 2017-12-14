import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: splitLines;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.strip;
        auto res1 = input.bitsInGrid;
        //auto res2 = input.idealDelay;
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
import core.bitop: popcnt;

static uint bitsInGrid(in string key){
    uint count;
    foreach(i; 0..128){
        auto hash = knotHash(key ~ "-" ~ i.to!string);
        foreach(b; hash)
            count += b.popcnt;
    }
    return count;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    expect(8108, bitsInGrid("flqrgnkx"));
}

//============================================================================
// Knot Hash (day 10)
//============================================================================
import std.string: split, strip, representation;
import std.algorithm: map;
import std.conv: to;
import std.array: array;

import std.algorithm: swap, reduce;
import std.range: iota, chunks;
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

static ubyte[] knotHash(in string input){
    ubyte[] suffix = [17, 31, 73, 47, 23];
    auto lengths = input.strip.representation ~ suffix;
    
    uint curPos, skipSize;
    auto numbers = iota(256u).array;
    foreach(_; 0..64)
        numbers = numbers.knot(lengths, curPos, skipSize);

    auto denseHash = numbers.chunks(16).map!(reduce!((a,b) => a^b));
    return denseHash.array.to!(ubyte[]);
}