import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: split;
import std.algorithm: map, maxIndex;
import std.conv: to;
import std.array: array;
import std.container: redBlackTree;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        auto input = contents.split.map!(to!uint).array;
        auto res1 = cyclesUntilRecurrence(input);
        //auto res2 = stepsUntilOOB(input, offsetUpdate2);
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
uint cyclesUntilRecurrence(in uint[] banks){
    auto curBanks = banks.dup;
    auto visited = redBlackTree!(uint[]);
    
    uint cycles;
    for(cycles=0; curBanks !in visited; ++cycles){
        visited.insert(curBanks);

        // Pick bank with highest number of blocks (first, if ambiguous)
        const maxIdx = curBanks.maxIndex;

        // Pick its blocks
        auto numBlocks = curBanks[maxIdx];
        curBanks = curBanks.dup;
        curBanks[maxIdx] = 0;

        // Redistribute blocks equally, starting in next bank
        for(auto idx=maxIdx+1; numBlocks>0; ++idx, --numBlocks)
            curBanks[idx % $] += 1;
    }
    return cycles;
}

unittest{
    expect(5,cyclesUntilRecurrence([0,2,7,0]));
}