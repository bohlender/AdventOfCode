import std.file;
import std.stdio;
import std.format;
import std.string;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const input = readText(args[1]).parse;
        auto res1 = input.maxFiniteArea;
        auto res2 = input.areaSumDistBelow(10000);
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.string;
import std.typecons;
import std.algorithm;
import std.range;
import std.math;

alias Vec2 = Tuple!(int, "x", int, "y");

auto manhattanDist = (Vec2 from, Vec2 to) => abs(to.x-from.x) + abs(to.y-from.y);

auto maxFiniteArea(in Vec2[] input){
    auto width = input.map!(v => v.x).maxElement+1;
    auto height = input.map!(v => v.y).maxElement+1;
    auto gridFlat = new long[width*height];
    auto grid = gridFlat.chunks(width);

    // Fill with index of nearest location
    foreach(y; 0..height){
        foreach(x; 0..width){
            const cur = Vec2(x,y);
            // TODO: Use topN
            const minIndex = input.minIndex!((v1, v2) => cur.manhattanDist(v1) < cur.manhattanDist(v2));
            const minDist = cur.manhattanDist(input[minIndex]);
            if (!input[minIndex+1..$].canFind!(v => cur.manhattanDist(v) == minDist)){
                grid[y][x] = minIndex;
            }else{
                grid[y][x] = -1;
            }
        }
    }

    // Determine indices of infinite areas
    auto infIdxs = grid[0]                              // top border
                 ~ grid[$-1]                            // bottom border
                 ~ grid.map!(line => line[0]).array     // left border
                 ~ grid.map!(line => line[$-1]).array;  // right border

    return gridFlat.sort.filter!(i => !infIdxs.canFind(i)) // ignore indices of infinite areas
                   .group.map!(cnt => cnt[1])              // determine finite areas
                   .maxElement;
}

auto parse(in string s){
    Vec2[] res;
    foreach(line; s.splitLines){
        Vec2 v;
        line.formattedRead!"%d, %d"(v.x, v.y);
        res ~= v;
    }
    return res;
}
//============================================================================
// Puzzle 2
//============================================================================
// TODO: Implement more scalable approach, e.g.
//       determine mean pos and flood fill from there
auto areaSumDistBelow(in Vec2[] input, in int bound){
    auto width = input.map!(v => v.x).maxElement+1;
    auto height = input.map!(v => v.y).maxElement+1;
    auto sumDist = (in Vec2 v) => input.fold!((acc, t) => acc + v.manhattanDist(t))(0);

    int count;
    foreach(y; 0..height)
        foreach(x; 0..width)
            if(sumDist(Vec2(x,y)) < bound)
                count++;
    return count;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    const input = `1, 1
1, 6
8, 3
3, 4
5, 5
8, 9`.parse;
    expect(17, input.maxFiniteArea);
    expect(16, input.areaSumDistBelow(32));
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}