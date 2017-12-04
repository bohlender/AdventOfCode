import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        auto res1 = countValidPasses(contents, words => words.validPass);
        auto res2 = countValidPasses(contents, words => words.validPass2);
        writefln("First: %s\nSecond: %s", res1, res2);
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

static auto countValidPasses(in string input, bool function (string[]) pred){
    return input.splitLines.map!(line => line.split).count!(pred);
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

//============================================================================
// Puzzle 2
//============================================================================
import std.algorithm: sort;
import std.utf: byCodeUnit;
import std.conv: to;
import std.container: redBlackTree;

static string normalise(in string word){
    auto sorted = word.dup.byCodeUnit.sort;
    return sorted.to!string;
}

unittest{
    expect("abcde", normalise("ecdab"));
    expect("abcde", normalise("abcde"));
}

static bool validPass2(in string[] words){
    auto seen = redBlackTree!string;

    foreach(const ref word; words){
        const normWord = word.normalise;
        if(normWord in seen)
            return false;
        seen.insert(normWord);
    }
    return true;
}

unittest{
    assert(validPass2("abcde fghij".split));
    assert(!validPass2("abcde xyz ecdab".split));
    assert(validPass2("a ab abc abd abf abj".split));
    assert(validPass2("iiii oiii ooii oooi oooo".split));
    assert(!validPass2("oiii ioii iioi iiio".split));
}