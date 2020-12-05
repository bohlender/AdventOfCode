import std.file: readText;
import std.conv;
import std.stdio;
import std.format;
import std.string;
import std.algorithm;
import std.range;
import std.bitmanip;

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
    auto res = appender!(Pass[]);
    foreach(line; s.lineSplitter()) {
        uint seatId;
        foreach(c; line) {
            seatId <<= 1;
            seatId |= c=='B' || c=='R';
        }
        res ~= Pass(seatId);
    }
    return res[];
}

//==============================================================================
// Puzzle 1
//==============================================================================
struct Pass {
    uint seatId;
    alias seatId this;
    auto col() { return seatId >> 3; }
    auto row() { return seatId & 0b111; }
}

auto sol1(Range)(Range input) {
    return input.maxElement;
}

//==============================================================================
// Puzzle 2
//==============================================================================
auto sol2(Range)(Range input) {
    foreach(window; input.sort.slide(2))
        if(window[1]-window[0] == 2)
            return window[0]+1;
    assert(0);
}

//==============================================================================
// Unittests
//==============================================================================
unittest {
    auto content =
`FBFBBFFRLR
BFFFBBFRRR
FFFBBBFRRR
BBFFBBFRLL`;
    auto input = content.parse();
    expect(820, input.sol1());
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}