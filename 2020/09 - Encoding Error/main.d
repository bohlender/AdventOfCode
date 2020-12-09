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
        auto res1 = input.sol1(25);
        auto res2 = input.sol2(25);
        writefln!"First: %s\nSecond: %s"(res1, res2);
    }
}

auto parse(in string s) {
    return s.splitter().map!(to!long).array;
}

//==============================================================================
// Puzzle 1
//==============================================================================
alias Answers = string[];

auto sol1(Range)(Range input, uint preamble) {
    outer:
    foreach(window; input.slide(preamble+1)) {
        auto newNum = window.back;
        foreach(i; 0..preamble)
            foreach(j; i..preamble)
                if(window[i] + window[j] == newNum)
                    continue outer;
        return newNum;
    }
    assert(0);
}

//==============================================================================
// Puzzle 2
//==============================================================================
auto sol2(Range)(Range input, uint preamble) {
    auto badNum = input.sol1(preamble);
    ulong lbIdx, ubIdx, acc;
    while(acc != badNum) {
        if(acc < badNum) {
            acc += input[ubIdx];
            ubIdx++;
        }else{
            acc -= input[lbIdx];
            lbIdx++;
        }
    }
    auto minMax = input[lbIdx..ubIdx].fold!(min, max);
    return minMax[0]+minMax[1];
}

//==============================================================================
// Unittests
//==============================================================================
unittest {
    auto content =
`35
20
15
25
47
40
62
55
65
95
102
117
150
182
127
219
299
277
309
576`;
    auto input = content.parse;
    expect(127, input.sol1(5));
    expect(62, input.sol2(5));
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format!"Expected %s but got %s"(expected, actual), file, line);
}