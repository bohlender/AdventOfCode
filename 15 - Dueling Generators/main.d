import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: strip, splitLines;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = 40_000_000.countMatches(input[0], input[1]);
        auto res2 = 5_000_000.countMatches!("a%4==0", "a%8==0")(input[0], input[1]);
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
// Puzzle 1/2
//============================================================================
import std.range: take, recurrence, dropOne;
import std.typecons: Tuple;
import std.algorithm: filter;

alias Pair(T) = Tuple!(T, T);
static auto parse(in string contents){
    auto lines = contents.splitLines;
    uint aPrev, bPrev;
    lines[0].formattedRead!"Generator A starts with %d"(aPrev);
    lines[1].formattedRead!"Generator B starts with %d"(bPrev);
    return Pair!uint(aPrev, bPrev);
}

static uint countMatches(alias pred1 = "true", alias pred2 = "true")(in uint numPairs, uint prevA, uint prevB){
    auto gen1 = recurrence!("(a[n-1]*16807UL) % 2147483647")(prevA).dropOne.filter!pred1;
    auto gen2 = recurrence!("(a[n-1]*48271UL) % 2147483647")(prevB).dropOne.filter!pred2;

    uint matches;
    foreach(_; 0..numPairs){
        if((0xFFFF & gen1.front) == (0xFFFF & gen2.front))
            ++matches;
        gen1.popFront;
        gen2.popFront;
    }
    return matches;
}

//============================================================================
// Unittests
//============================================================================
import std.array: array;
unittest{
    auto gen1 = recurrence!("(a[n-1]*16807UL) % 2147483647")(65).dropOne;
    auto gen2 = recurrence!("(a[n-1]*48271UL) % 2147483647")(8921).dropOne;
    
    expect([1092455u, 1181022009, 245556042, 1744312007, 1352636452], gen1.take(5).array);
    expect([430625591u, 1233683848, 1431495498, 137874439, 285222916], gen2.take(5).array);

    expect(1, 5.countMatches(65, 8921));
}

unittest{
    auto gen1 = recurrence!("(a[n-1]*16807UL) % 2147483647")(65).dropOne.filter!(v => v%4 == 0);
    auto gen2 = recurrence!("(a[n-1]*48271UL) % 2147483647")(8921).dropOne.filter!(v => v%8 == 0);
    
    expect([1352636452u, 1992081072, 530830436, 1980017072, 740335192], gen1.take(5).array);
    expect([1233683848, 862516352, 1159784568, 1616057672, 412269392], gen2.take(5).array);

    expect(1, 1056.countMatches!("a%4==0", "a%8==0")(65, 8921));
}