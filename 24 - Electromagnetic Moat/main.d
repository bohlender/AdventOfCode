import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = input.maxStrength;
        //auto res2 = input.valOfH;
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
import std.string: strip, splitLines;
import std.algorithm: filter, map, max, remove;
import std.typecons: Tuple;

alias Edge = Tuple!(int, "v1", int, "v2", int, "w");

auto parse(in string s){
    Edge[] res;
    foreach(line; s.strip.splitLines){
        int v1, v2;
        line.formattedRead!"%d/%d"(v1, v2);
        res ~= Edge(v1, v2, v1+v2);
    }
    return res;
}

auto maxStrength(R)(const R edges, int from=0, int curStrength=0){
    auto outgoing = edges.filter!(e => e.v1==from || e.v2==from);
    if(outgoing.empty)
        return curStrength;
    int curMax;
    foreach(o; outgoing){
        auto vDst = o.v1==from ? o.v2 : o.v1;
        auto newEdges = edges.dup.remove!(e => e==o);
        auto strength = newEdges.maxStrength(vDst, curStrength + o.w);
        curMax = max(curMax, strength);
    }
    return curMax;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto contents = r"0/2
2/2
2/3
3/4
3/5
0/1
10/1
9/10";
    auto input = contents.parse;
    expect(31, input.maxStrength);
}