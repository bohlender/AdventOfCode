import std.file: readText;
import std.conv;
import std.stdio;
import std.format;
import std.string;
import std.algorithm;
import std.range;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = args[1].readText().parse();
        auto res1 = input.sol1();
        auto res2 = input.sol2();
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

auto parse(in string s) {
    return s.splitLines().map!(l => l.to!int).array().sort();
}

//============================================================================
// Puzzle 1
//============================================================================
auto sol1(Range)(Range input) {
    foreach(i; input) {
        auto j = 2020-i;
        if(input.contains(j))
            return i*j;
    }
    assert(0);
}

//============================================================================
// Puzzle 2
//============================================================================
auto sol2(Range)(Range input) {
    foreach(i; input) {
        foreach(j; input) {
            auto k = 2020-i-j;
            if(input.contains(k))
                return i*j*k;
        }
    }
    assert(0);
}

//============================================================================
// Unittests
//============================================================================
unittest{
    expect(514579, [1721, 979, 366, 299, 675, 1456].sol1());

    expect(241861950, [1721, 979, 366, 299, 675, 1456].sol2());
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}