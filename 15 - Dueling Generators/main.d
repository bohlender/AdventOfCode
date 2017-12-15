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
        //auto res2 = input.regionCount;
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
import std.range: generate, take;
import std.typecons: Tuple;

alias Pair(T) = Tuple!(T, T);
static auto parse(in string contents){
    auto lines = contents.splitLines;
    uint aPrev, bPrev;
    lines[0].formattedRead!"Generator A starts with %d"(aPrev);
    lines[1].formattedRead!"Generator B starts with %d"(bPrev);
    return Pair!uint(aPrev, bPrev);
}

static auto infGen(in uint factor, in uint prev){
    uint _prev = prev;
    return (){
        uint res = (_prev * cast(ulong)(factor)) % 2147483647;
        _prev = res;
        return res;
    };
}

static uint countMatches(in uint numPairs, in uint prevA, in uint prevB){
    auto gen1 = generate(infGen(16807, prevA));
    auto gen2 = generate(infGen(48271, prevB));

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
    auto gen1 = generate(infGen(16807, 65));
    auto gen2 = generate(infGen(48271, 8921));
    
    expect([1092455u, 1181022009, 245556042, 1744312007, 1352636452], gen1.take(5).array);
    expect([430625591u, 1233683848, 1431495498, 137874439, 285222916], gen2.take(5).array);

    expect(1, 5.countMatches(65, 8921));
}