import std.file: readText;
import std.stdio;
import std.string;
import std.algorithm;
import std.range;
import std.conv;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = args[1].readText.parse;
        auto res1 = input.sol1;
        auto res2 = input.sol2;
        writefln!"First: %s\nSecond: %s"(res1, res2);
    }
}

auto parse(in string s) {
    auto sorted = s.splitter().map!(to!ulong).array.sort;
    return chain([0], sorted, [sorted.back+3]).array;
}

//==============================================================================
// Puzzle 1
//==============================================================================
auto sol1(Range)(Range input) {
    uint num1jolt, num3jolt;
    foreach(window; input.slide(2)) {
        if(window[1]-window[0] == 1) { num1jolt++; }
        if(window[1]-window[0] == 3) { num3jolt++; }
    }
    return num1jolt*num3jolt;
}

//==============================================================================
// Puzzle 2
//==============================================================================
auto sol2(T)(T[] input) {
    T[ulong] cache;

    auto arrangementsFrom(ulong fromIdx) {
        auto p = fromIdx in cache;
        if(p !is null)
            return *p;

        if(fromIdx == input.length-1)
            return 1;

        auto succIdxs = iota(fromIdx+1, fromIdx+4)
            .filter!(i => i<input.length && input[i]-input[fromIdx] <= 3);
        auto res = succIdxs.map!(i => arrangementsFrom(i)).sum;
        return cache[fromIdx] = res;
    }

    return arrangementsFrom(0);
}

//==============================================================================
// Unittests
//==============================================================================
unittest {
    auto content =
`16
10
15
5
1
11
7
19
6
12
4`;
    auto input = content.parse;
    expect(7*5, input.sol1);
    expect(8, input.sol2);
}

unittest {
    auto content =
`28
33
18
42
31
14
46
20
48
47
24
23
49
45
19
38
39
11
1
32
25
35
8
17
7
9
4
2
34
10
3`;
    auto input = content.parse;
    expect(22*10, input.sol1);
    expect(19208, input.sol2);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format!"Expected %s but got %s"(expected, actual), file, line);
}