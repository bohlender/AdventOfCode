import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: split, splitLines;
import std.algorithm: startsWith, find;
import std.conv: to;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        auto input = contents.splitLines;
        auto res1 = getBottomProg(input);
        //auto res2 = cyclesInLoop(input);
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
struct Node{
    const string src;
    const uint weight;
    const string[] dsts;

    this(in string src, in uint weight, in string[] dsts){
        this.src = src;
        this.weight = weight;
        this.dsts = dsts;
    }

    this(in string s){
        string src;
        uint weight;
        string[] dsts;

        auto cp = s.dup;
        cp.formattedRead("%s (%d)", src, weight);
        if(cp.startsWith(" ->"))
            cp.formattedRead(" -> %(%(%c%), %)", dsts);
        this(src, weight, dsts);
    }
}

unittest{
    expect("fwft (72)".to!Node, Node("fwft", 72, []));
    expect("fwft (72) -> ktlj, cntj, xhth".to!Node, Node("fwft", 72, ["ktlj","cntj","xhth"]));
}

string getBottomProg(in string[] lines) {
    // Collect dependencies
    string[string] heldBy;
    foreach(line; lines){
        const n = line.to!Node;
        foreach(dst; n.dsts)
            heldBy[dst] = n.src;
    }

    // Find element not held by any other
    // Naive: return heldBy.byValue.find!(s => s !in heldBy).front
    string cur;
    for(cur = heldBy.byKey.front; cur in heldBy; cur = heldBy[cur]){}
    return cur;
}

unittest{
    auto input = r"pbga (66)
xhth (57)
ebii (61)
havc (66)
ktlj (57)
fwft (72) -> ktlj, cntj, xhth
qoyq (66)
padx (45) -> pbga, havc, qoyq
tknk (41) -> ugml, padx, fwft
jptl (61)
ugml (68) -> gyxo, ebii, jptl
gyxo (61)
cntj (57)";

    expect("tknk", getBottomProg(input.splitLines));
}