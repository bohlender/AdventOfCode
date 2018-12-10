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
        input.waitForAlignment.writeln;
    }
}

//============================================================================
// Puzzle 1 & 2
//============================================================================
struct Star{
    int x, y, vX, vY;
    int opCmp(in Star rhs) const {return (y==rhs.y)? x-rhs.x : y-rhs.y;}
    auto move(in int t) const {return Star(x+vX*t, y+vY*t, vX, vY);}
}

auto waitForAlignment(in Star[] stars, in int maxTime=15_000){
    int bestAligned, bestT;
    foreach(t; 0..maxTime){
        auto curStars = stars.map!(s => s.move(t));
        const vertAligned = curStars.map!(s => s.x)                            // x coords
                                    .array.sort.group                          // count each
                                    .map!(cnt => cnt[1]).filter!(cnt => cnt>1) // aligning if count > 1 
                                    .sum;
        if(bestAligned <= vertAligned){
            bestAligned = vertAligned;
            bestT = t;
        }
    }
    stars.map!(s => s.move(bestT)).array.plot;
    return bestT;
}

auto plot(in Star[] stars){
    auto sorted = stars.dup.sort.uniq!((a,b) => a.x==b.x && a.y==b.y).array;
    const xMinMax = sorted.map!(s => s.x).fold!(min, max);
    const yMinMax = sorted.map!(s => s.y).fold!(min, max);
    foreach(y; yMinMax[0]..yMinMax[1]+1){
        foreach(x; xMinMax[0]..xMinMax[1]+1){
            if(!sorted.empty && sorted.front.y == y && sorted.front.x == x){
                write("#");
                sorted.popFront;
            }else
                write(".");
        }
        writeln;
    }
}

auto parse(in string s){
    Star[] stars;
    foreach(line; s.splitLines){
        Star cur;
        line.formattedRead!"position=< %d, %d> velocity=< %d, %d>"(cur.x, cur.y, cur.vX, cur.vY);
        stars ~= cur;
    }
    return stars;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    const input = `position=< 9,  1> velocity=< 0,  2>
position=< 7,  0> velocity=<-1,  0>
position=< 3, -2> velocity=<-1,  1>
position=< 6, 10> velocity=<-2, -1>
position=< 2, -4> velocity=< 2,  2>
position=<-6, 10> velocity=< 2, -2>
position=< 1,  8> velocity=< 1, -1>
position=< 1,  7> velocity=< 1,  0>
position=<-3, 11> velocity=< 1, -2>
position=< 7,  6> velocity=<-1, -1>
position=<-2,  3> velocity=< 1,  0>
position=<-4,  3> velocity=< 2,  0>
position=<10, -3> velocity=<-1,  1>
position=< 5, 11> velocity=< 1, -2>
position=< 4,  7> velocity=< 0, -1>
position=< 8, -2> velocity=< 0,  1>
position=<15,  0> velocity=<-2,  0>
position=< 1,  6> velocity=< 1,  0>
position=< 8,  9> velocity=< 0, -1>
position=< 3,  3> velocity=<-1,  1>
position=< 0,  5> velocity=< 0, -1>
position=<-2,  2> velocity=< 2,  0>
position=< 5, -2> velocity=< 1,  2>
position=< 1,  4> velocity=< 2,  1>
position=<-2,  7> velocity=< 2, -2>
position=< 3,  6> velocity=<-1, -1>
position=< 5,  0> velocity=< 1,  0>
position=<-6,  0> velocity=< 2,  0>
position=< 5,  9> velocity=< 1, -2>
position=<14,  7> velocity=<-2,  0>
position=<-3,  6> velocity=< 2, -1>`.parse;
    expect(3, input.waitForAlignment);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}