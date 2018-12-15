import std.file: readText;
import std.conv;
import std.stdio;
import std.format;
import std.string;
import std.algorithm;
import std.range;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = args[1].readText.strip;
        auto res1 = input.to!ulong.scoresAfter;
        auto res2 = input.recipesBefore;
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
struct State{
    ulong[2] elves = [0,1];
    auto scores = "37";

    void step(){
        scores ~= (scores[elves[0]]-'0' + scores[elves[1]]-'0').to!string;
        foreach(ref idx; elves)
            idx = (idx+scores[idx]-'0'+1)%scores.length;
    }
}

auto scoresAfter(in ulong steps){
    State s;
    while(s.scores.length < steps+10)
        s.step;
    return s.scores[steps..steps+10];
}

//============================================================================
// Puzzle 2
//============================================================================
auto recipesBefore(in string input){
    State s;
    while(!s.scores.endsWith(input) && !s.scores[0..$-1].endsWith(input))
        s.step;
    return s.scores.length-s.scores.find(input).length;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    expect("5158916779", 9.scoresAfter);
    expect("0124515891", 5.scoresAfter);
    expect("9251071085", 18.scoresAfter);
    expect("5941429882", 2018.scoresAfter);

    expect(9, "51589".recipesBefore);
    expect(5, "01245".recipesBefore);
    expect(18, "92510".recipesBefore);
    expect(2018, "59414".recipesBefore);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}