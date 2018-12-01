import std.stdio;
import std.file;
import std.format;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = input.freq;
        auto res2 = input.firstDupeFreq;
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.string;
import std.conv;
import std.algorithm;

auto freq(int[] input){
    return input.sum;
}

auto parse(in string s){
    return s.splitLines.map!(line => line.to!int).array;
}

//============================================================================
// Puzzle 2
//============================================================================
import std.range;
import std.container;

auto firstDupeFreq(int[] input){
    int curFreq;
    auto freqSums = make!(RedBlackTree!int)(curFreq);
    
    foreach(freq; cycle(input)){
        curFreq += freq;
        if (curFreq !in freqSums)
            freqSums.insert(curFreq);
        else
            return curFreq;
    }
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto contents = `+1
-2
+3
+1`;
    auto input = contents.parse;
    expect([1, -2, 3, 1], input);
    expect(3, input.freq);
}

unittest{
    auto input = [+3, +3, +4, -2, -4];
    expect(10, input.firstDupeFreq);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}