import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        auto res1 = countValidPasses(contents);
        //auto res2 = calcUntilOver(words);
        //writefln("First: %s\nSecond: %s", res1, res2);
        writeln(res1);    
    }
}

static void expect(T1, T2)(T1 expected, T2 actual) if(is(typeof(expected == actual) == bool)) {
    assert(expected == actual, format("Expected %s but got %s", expected, actual));
}

//============================================================================
// Puzzle 1
//============================================================================
import std.string: split, splitLines;
import std.algorithm: map, count;

static auto countValidPasses(in string input){
    return input.splitLines.map!(line => line.split).count!(words => words.validPass);
}

static bool validPass(in string[] words){
    uint[string] wordCount;

    foreach(const ref word; words){
        const p = word in wordCount;
        if(p)
            return false;
        wordCount[word] += 1;
    }
    return true;
}

unittest{
    assert(validPass("aa bb cc dd ee".split));
    assert(!validPass("aa bb cc dd aa".split));
    assert(validPass("aa bb cc dd aaa".split));
}