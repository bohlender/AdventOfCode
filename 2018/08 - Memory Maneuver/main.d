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
        auto res1 = input.metaSum;
        auto res2 = input.value;
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.conv;

struct Node{
    Node[] children;
    ulong[] data;

    this(ref ulong[] raw){
        const numChilds = raw.front;
        raw.popFront;
        const numData = raw.front;
        raw.popFront;
        foreach(i; 0..numChilds)
            children ~= Node(raw);
        foreach(i; 0..numData){
            data ~= raw.front;
            raw.popFront;
        }
    }
}

ulong metaSum(in Node input){
    return input.data.sum + input.children.map!(c => metaSum(c)).sum;
}

auto parse(in string s){
    auto raw = s.split.to!(ulong[]);
    return Node(raw);
}
//============================================================================
// Puzzle 2
//============================================================================
ulong value(in Node n){
    if(n.children.empty)
        return n.data.sum;
    auto childIdxs = n.data.filter!(d => 1 <= d && d <= n.children.length).map!(d => d-1);
    return childIdxs.map!(i => n.children[i].value).sum;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    const input = `2 3 0 3 10 11 12 1 1 0 1 99 2 1 1 2`.parse;
    expect(138, input.metaSum);
    expect(66, input.value);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}