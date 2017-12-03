import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.math: sqrt, ceil, abs;
import std.conv: to;
import std.algorithm: reduce, min, map;
import std.array: array;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        uint input;
        contents.formattedRead("%d",input);
        auto res1 = distance(input);
        //auto res2 = checksum2(input);
        //writefln("First: %s\nSecond: %s", res1, res2);
        writeln(res1);
    }
}

void expect(T1, T2)(T1 expected, T2 actual) if(is(typeof(expected == actual) == bool)) {
    assert(expected == actual, format("Expected %s but got %s", expected, actual));
}

//============================================================================
// Puzzle 1
//============================================================================
// Notes:
// - innermost ring is ring 1
// - highest number on ring n is  (2 n    -1)^^2
// - smallest number on ring n is (2(n-1) -1)^^2 + 1
// - root of hightest number on ring is odd

/// Computes the ring number n is on
uint ring(uint n){
    auto root = n.to!real.sqrt.ceil.to!uint;
    // Root must be odd
    if(root%2 == 0)
        ++root;
    return (root+1)/2;
}

unittest{
    foreach(i; 2..10)
        expect(2, ring(i));
    foreach(i; 10..26)
        expect(3, ring(i));
    foreach(i; 26..50)
        expect(4, ring(i));
}

// Computes numbers in cardinal directions of ring
uint[] cardinalDirNums(uint ring){
    if(ring == 1)
        return [1];

    const borderLength = ring*2-1;
    const eastNum = (2*(ring-1)-1)^^2 + borderLength/2;

    uint[] res;
    foreach(i; 0..4)
        res ~= eastNum + i*(borderLength-1);
    return res;
}

unittest{
    expect([1], cardinalDirNums(1));
    expect([2,4,6,8], cardinalDirNums(2));
    expect([11,15,19,23], cardinalDirNums(3));
}

/// Computes manhattan distance from number n to 1 (on ring 1)
uint distance(uint n){
    assert(n>0);

    const r = ring(n);
    const distToNESW = r.cardinalDirNums.map!(num => abs(n-num)).reduce!min;
    const distFromNESW = r-1;

    return distToNESW + distFromNESW;
}

unittest{
    expect(0, distance(1));
    expect(3, distance(12));
    expect(2, distance(23));
    expect(31, distance(1024));
}