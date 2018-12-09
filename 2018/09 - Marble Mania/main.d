import std.file;
import std.stdio;
import std.format;
import std.string;
import std.algorithm;
import std.range;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const input = readText(args[1]).parse;
        auto res1 = input.winnerScore;
        auto res2 = Input(input.numPlayers, input.lastMarble*100).winnerScore;
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1 & 2
//============================================================================
import std.typecons;
import std.conv;
import std.container;
import std.math;

alias Input = Tuple!(ulong, "numPlayers", ulong, "lastMarble");

auto winnerScore(in Input input){
    auto score = new ulong[input.numPlayers];
    auto marbles = make!(DList!ulong)(0);
    foreach(nextMarble; 1..input.lastMarble+1){
        if(nextMarble%23 != 0){ // Regular: insert after index 1
            marbles.rot(2);
            marbles.insertFront(nextMarble);
        }else{  // Irregular: remove at index -7, add to score
            marbles.rot(-7);
            score[(nextMarble-1)%$] += marbles.front + nextMarble;
            marbles.removeFront;
        }
    }
    return score.maxElement;
}

auto rot(T)(DList!T list, in int by){
    const toRight = by>=0;
    foreach(rem; 0..abs(by)){
        if(toRight){
            list.insertBack(list.moveFront);
            list.removeFront;
        }else{
            list.insertFront(list.moveBack);
            list.removeBack;
        }
    }
}

auto parse(in string s){
    auto parts = s.split;
    return Input(parts[0].to!ulong, parts[6].to!ulong);
}

//============================================================================
// Unittests
//============================================================================
unittest{
    const input = `9 players; last marble is worth 25 points`.parse;
    expect(32, input.winnerScore);
    expect(8317, Input(10,1618).winnerScore);
    expect(37305, Input(30,5807).winnerScore);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}