import std.file: readText;
import std.stdio;
import std.string;
import std.algorithm;
import std.range;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = args[1].readText.parse;
        auto res1 = input.sol1;
        auto res2 = input.sol2();
        writefln!"First: %s\nSecond: %s"(res1, res2);
    }
}

auto parse(in string s) {
    auto res = appender!(Answers[]);
    foreach(line; s.splitter("\n\n"))
        res ~= line.split;
    return res[];
}

//==============================================================================
// Puzzle 1
//==============================================================================
alias Answers = string[];

auto sol1(Range)(Range input) {
    return input.map!(answers =>
        answers.join.array.sort.uniq.count
    ).sum;
}

//==============================================================================
// Puzzle 2
//==============================================================================
auto sol2(Range)(Range input) {
    return input.map!(answers =>
        answers.join.array.sort.group
        .count!(e => e[1] == answers.length)
    ).sum;
}

//==============================================================================
// Unittests
//==============================================================================
unittest {
    auto content =
`abc

a
b
c

ab
ac

a
a
a
a

b`;
    auto input = content.parse;
    expect(11, input.sol1);
    expect(6, input.sol2);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format!"Expected %s but got %s"(expected, actual), file, line);
}