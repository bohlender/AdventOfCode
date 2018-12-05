import std.file;
import std.stdio;
import std.format;
import std.string;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const input = readText(args[1]).strip;
        auto res1 = input.react.length;
        auto res2 = input.bestReact.length;
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.algorithm;
import std.conv;

string react(S)(in S polymer){
    string step(in S acc, in dchar cur){
        if(!acc.empty && acc[$-1] != cur && acc[$-1].toUpper == cur.toUpper)
            return acc[0..$-1];
        return acc ~ cur.to!S;
    }
    return polymer.fold!step("");
}

//============================================================================
// Puzzle 2
//============================================================================
import std.ascii: abc = lowercase;
import std.array;

string bestReact(in string polymer){
    return abc.map!(c => polymer.filter!(z => z!=c && z!=c.toUpper).to!string.react)
              .minElement!(s => s.length);
}

//============================================================================
// Unittests
//============================================================================
unittest{
    string input = "dabAcCaCBAcCcaDA";
    expect("dabCBAcaDA", input.react);
    expect("daDA", input.bestReact);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}