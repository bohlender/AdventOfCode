import std.file: readText;
import std.conv;
import std.stdio;
import std.format;
import std.string;
import std.algorithm;
import std.range;
import std.typecons: Tuple;

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

alias Entry = Tuple!(int, "val1", int, "val2", char, "c", string, "pass");

auto parse(in string s) {
    auto res = appender!(Entry[]);
    foreach(line; s.lineSplitter().filter!(l => !l.empty())) {
        Entry e;
        line.formattedRead!"%d-%d %c: %s"(e.val1, e.val2, e.c, e.pass);
        res ~= e;
    }
    return res[];
}

//==============================================================================
// Puzzle 1
//==============================================================================
auto sol1(Range)(Range input) {
    auto validCount = 0;
    foreach(entry; input) {
        auto charCount = entry.pass.count(entry.c);
        if(entry.val1 <= charCount && charCount <= entry.val2)
            validCount++;
    }
    return validCount;
}

//==============================================================================
// Puzzle 2
//==============================================================================
auto sol2(Range)(Range input) {
    auto validCount = 0;
    foreach(entry; input) {
        auto relevant = [entry.pass[entry.val1-1], entry.pass[entry.val2-1]];
        if(relevant.count(entry.c) == 1)
            validCount++;
    }
    return validCount;
}

//==============================================================================
// Unittests
//==============================================================================
unittest{
    auto content = `
1-3 a: abcde
1-3 b: cdefg
2-9 c: ccccccccc`;
    auto input = content.parse();

    expect(2, input.sol1());
    expect(1, input.sol2());
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}