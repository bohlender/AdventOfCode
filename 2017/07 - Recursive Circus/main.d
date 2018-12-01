import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: split, splitLines;
import std.algorithm: startsWith, map, sum, group, sort, find;
import std.typecons: Tuple;
import std.array: array;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto content = readText(args[1]);
        auto nodes = content.parse;
        auto res1 = findRoot(nodes);
        auto res2 = correctWeight(res1, nodes);
        writefln("First: %s\nSecond: %s", res1, res2);
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

    // Find node without parent
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
static uint treeWeight(in string root, in Node[string] nodes){
    const n = nodes[root];
    const childrenWeight = n.dsts.map!(s => treeWeight(nodes[s].src, nodes)).sum;
    return n.weight + childrenWeight;
}

unittest{
    auto content = r"pbga (66)
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

    auto nodes = content.parse;
    expect(251, treeWeight("ugml", nodes));
    expect(243, treeWeight("padx", nodes));
    expect(243, treeWeight("fwft", nodes));
}

// TODO: Avoid recomputation by filling uint[string] treeWeights once
static uint correctWeight(in string root, in Node[string] nodes){
    const n = nodes[root];
    auto weightHistogram = n.dsts.map!(s => treeWeight(s,nodes)).group.array;
    const expectedWeight = weightHistogram.sort!((a,b) => a[1]>b[1]).front[0];
    auto problematicSubtree = n.dsts.find!((s,w) => treeWeight(s, nodes) != w)(expectedWeight)[0];
    
    return correctWeight(problematicSubtree, expectedWeight, nodes);
}

static uint correctWeight(in string root, in uint expectedWeight, in Node[string] nodes){
    const n = nodes[root];
    auto weightHistogram = n.dsts.map!(s => treeWeight(s,nodes)).group.array;
    const expectedChildWeight = weightHistogram.sort!((a,b) => a[1]>b[1]).front[0];

    // If children have equal weight but tree weight does not match, then root is the culprit
    if(weightHistogram.length == 1 && treeWeight(root, nodes) != expectedWeight)
        return expectedWeight - n.dsts.map!(s => treeWeight(s,nodes)).sum;
    // Otherwise, continue investigation in deviating subtree
    auto problematicSubtree = n.dsts.find!((s,w) => treeWeight(s, nodes) != w)(expectedWeight)[0];
    return correctWeight(problematicSubtree, expectedChildWeight, nodes);
}

unittest{
        auto content = r"pbga (66)
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

    auto nodes = content.parse;
    auto root = findRoot(nodes);
    expect(60, correctWeight(root, nodes));
}