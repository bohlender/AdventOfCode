import std.file: readText;
import std.conv;
import std.stdio;
import std.format;
import std.string;
import std.algorithm;
import std.range;
import std.typecons: Tuple;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = args[1].readText().parse();
        auto res1 = input.sol1();
        auto res2 = input.sol2();
        writefln!"First: %s\nSecond: %s"(res1, res2);
    }
}

alias Vec2 = Tuple!(ulong, "x", ulong, "y");

struct Map {
    ulong width, height;
    bool[] localTrees;

    auto isTree(Vec2 v) const {
        assert(v.y<height);
        return localTrees[v.y*width + v.x%width];
    }
}

auto parse(in string s) {
    auto lines = s.lineSplitter().filter!(l => !l.empty());
    auto width = lines.front().length;
    auto height = 0;
    auto localTrees = appender!(bool[]);
    foreach(line; lines) {
        localTrees ~= line.map!(c => c=='#');
        height++;
    }
    return Map(width, height, localTrees[]);
}

//==============================================================================
// Puzzle 1
//==============================================================================
auto sol1(in Map map) {
    return treeCount(map, Vec2(3,1));
}

auto treeCount(in Map map, in Vec2 slope) {
    auto curPos = Vec2(0,0);
    ulong count = 0;
    while(curPos.y + slope.y < map.height) {
        curPos = Vec2(curPos.x + slope.x, curPos.y + slope.y);
        if(map.isTree(curPos))
            count++;
    }
    return count;
}

//==============================================================================
// Puzzle 2
//==============================================================================
auto slopes = [Vec2(1,1), Vec2(3,1), Vec2(5,1), Vec2(7,1), Vec2(1,2)];
auto sol2(in Map map) {
    return slopes.map!(s => treeCount(map, s))
                 .fold!((a,b) => a*b);
}

//==============================================================================
// Unittests
//==============================================================================
unittest{
    auto content = `
..##.......
#...#...#..
.#....#..#.
..#.#...#.#
.#...##..#.
..#.##.....
.#.#.#....#
.#........#
#.##...#...
#...##....#
.#..#...#.#`;
    auto input = content.parse();

    expect(7, input.sol1());
    expect(336, input.sol2());
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format!"Expected %s but got %s"(expected, actual), file, line);
}