import std.stdio;
import std.file;
import std.format;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const content = readText(args[1]);
        auto input = content.parse;
        auto res1 = input.sol1;
        auto res2 = input.sol2;
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.string;
import std.algorithm;
import std.typecons;
import std.conv;
import std.range;

alias interval = Tuple!(int, "from", int, "till");
alias NapMap = interval[][int];

auto bestNapper(in NapMap id2naps){
    return id2naps.keys.maxElement!(id => id2naps[id].map!(i => i.till-i.from).sum);
}

// Returns most often occurring minute and count
auto bestMinute(in interval[] naps){
    return naps.map!(n => iota(n.from, n.till)) // naps to ranges of minutes
               .joiner.array                    // flatten
               .sort.group                      // count occurrences
               .maxElement!(t => t[1]);         // most often
}

auto sol1(in NapMap id2naps){
    auto napper = id2naps.bestNapper;
    return napper * id2naps[napper].bestMinute[0];
}

auto parse(in string s){
    NapMap id2naps;
    int curId, curFrom;
    foreach(line; s.splitLines.sort){
        auto split = line.split;
        if(split[2] == "Guard")
            curId = split[3][1..$].to!int;
        else if(split[2] == "falls")
            curFrom = split[1][3..5].to!int;
        else if(split[2] == "wakes"){
            auto curTill = split[1][3..5].to!int;
            id2naps[curId] ~= interval(curFrom, curTill);
        }
    }
    return id2naps;
}

//============================================================================
// Puzzle 2
//============================================================================
import std.container;

auto sol2(in NapMap id2naps){
    Tuple!(int,int)[int] id2bestMinute;
    id2naps.each!((id, naps) => id2bestMinute[id] = naps.bestMinute);

    auto bestPair = id2bestMinute.byKeyValue.maxElement!(e => e.value[1]);
    return bestPair.key*bestPair.value[0];
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto content = `[1518-11-01 00:00] Guard #10 begins shift
[1518-11-01 00:05] falls asleep
[1518-11-01 00:25] wakes up
[1518-11-01 00:30] falls asleep
[1518-11-01 00:55] wakes up
[1518-11-01 23:58] Guard #99 begins shift
[1518-11-02 00:40] falls asleep
[1518-11-02 00:50] wakes up
[1518-11-03 00:05] Guard #10 begins shift
[1518-11-03 00:24] falls asleep
[1518-11-03 00:29] wakes up
[1518-11-04 00:02] Guard #99 begins shift
[1518-11-04 00:36] falls asleep
[1518-11-04 00:46] wakes up
[1518-11-05 00:03] Guard #99 begins shift
[1518-11-05 00:45] falls asleep
[1518-11-05 00:55] wakes up`;
    // Test whether sorting suffices
    expect(content, content.splitLines.sort.join("\n"));

    // Test parser
    auto input = content.parse;
    expect(2, input.length);
    expect(input[10], [interval(5,25), interval(30,55), interval(24,29)]);

    expect(10, input.bestNapper);
    expect(24, input[input.bestNapper].bestMinute[0]);
}

unittest{
    auto content = `[1518-11-01 00:00] Guard #10 begins shift
[1518-11-01 00:05] falls asleep
[1518-11-01 00:25] wakes up
[1518-11-01 00:30] falls asleep
[1518-11-01 00:55] wakes up
[1518-11-01 23:58] Guard #99 begins shift
[1518-11-02 00:40] falls asleep
[1518-11-02 00:50] wakes up
[1518-11-03 00:05] Guard #10 begins shift
[1518-11-03 00:24] falls asleep
[1518-11-03 00:29] wakes up
[1518-11-04 00:02] Guard #99 begins shift
[1518-11-04 00:36] falls asleep
[1518-11-04 00:46] wakes up
[1518-11-05 00:03] Guard #99 begins shift
[1518-11-05 00:45] falls asleep
[1518-11-05 00:55] wakes up`;
    auto input = content.parse;
    expect(4455, input.sol2);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}