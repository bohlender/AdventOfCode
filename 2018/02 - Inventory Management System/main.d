import std.stdio;
import std.file;
import std.format;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const content = readText(args[1]);
        auto input = content.parse;
        auto res1 = input.checksum;
        auto res2 = input.commonCharsOfCorrectBoxes;
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.string;
import std.algorithm;
import std.array;

auto checksum(string[] input){
    auto histograms = input.map!(line => line.array.sort.group);
    auto numCharOccursTwice = histograms.count!(h => h.canFind!(charCount => charCount[1] == 2));
    auto numCharOccursThrice = histograms.count!(h => h.canFind!(charCount => charCount[1] == 3));
    return numCharOccursTwice * numCharOccursThrice;
}

auto parse(in string s){
    return s.splitLines;
}

//============================================================================
// Puzzle 2
//============================================================================

string commonCharsOfCorrectBoxes(string[] input){
    foreach(line1; input){
        foreach(line2; input){
            auto dist = levenshteinDistanceAndPath(line1, line2);
            if (dist[0] == 1){
                auto badCharIdx = line2.length - dist[1].find!(op => op == EditOp.substitute).length;
                return line2[0..badCharIdx]~line2[badCharIdx+1..$];
            }
        }
    }
    assert(false, "No suitable pair found");
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto content = `abcdef
bababc
abbcde
abcccd
aabcdd
abcdee
ababab`;
    auto input = content.parse;
    expect(12, input.checksum);
}

unittest{
    auto content = `abcde
fghij
klmno
pqrst
fguij
axcye
wvxyz`;
    auto input = content.parse;
    expect("fgij", input.commonCharsOfCorrectBoxes);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}