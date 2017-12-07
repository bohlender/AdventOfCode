import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: split, splitLines;
import std.algorithm: startsWith, find;
import std.conv: to;
import std.typecons: Tuple;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        auto input = parse(contents);
        auto res1 = findRoot(input);
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
alias Node = Tuple!(string, "src", uint, "weight", string[], "dsts");

static Node[string] parse(in string s){
    Node[string] res;
    foreach(line; s.splitLines){
        Node n;
        line.formattedRead("%s (%d)", n.src, n.weight);
        if(line.startsWith(" ->"))
            line.formattedRead(" -> %(%(%c%), %)", n.dsts);
        res[n.src] = n;
    }
    return res;
}

unittest{
    expect(Node("fwft", 72, []),                     parse("fwft (72)")["fwft"]);
    expect(Node("fwft", 72, ["ktlj","cntj","xhth"]), parse("fwft (72) -> ktlj, cntj, xhth")["fwft"]);
}

static string findRoot(in Node[string] nodes) {
    // Collect dependencies
    string[string] parentOf;
    foreach(parent; nodes.byValue)
        foreach(child; parent.dsts)
            parentOf[child] = parent.src;

    // Find element not held by any other
    // Naive: return parentOf.byValue.find!(s => s !in parentOf).front;
    string cur;
    for(cur = parentOf.byKey.front; cur in parentOf; cur = parentOf[cur]){}
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

    expect("tknk", input.parse.findRoot);
}

//============================================================================
// Puzzle 2
//============================================================================