import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.math: sqrt, ceil, abs;
import std.conv: to;
import std.algorithm: reduce, min, map;
import std.array: array;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        uint input;
        contents.formattedRead("%d",input);
        auto res1 = distance(input);
        auto res2 = calcUntilOver(input);
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

void expect(T1, T2)(T1 expected, T2 actual) if(is(typeof(expected == actual) == bool)) {
    assert(expected == actual, format("Expected %s but got %s", expected, actual));
}

//============================================================================
// Puzzle 1
//============================================================================
// Notes:
// - innermost ring is ring 1
// - highest number on ring n is  (2 n    -1)^^2
// - smallest number on ring n is (2(n-1) -1)^^2 + 1
// - root of hightest number on ring is odd

/// Computes the ring number n is on
uint ring(uint n){
    auto root = n.to!real.sqrt.ceil.to!uint;
    // Root must be odd
    if(root%2 == 0)
        ++root;
    return (root+1)/2;
}

unittest{
    foreach(i; 2..10)
        expect(2, ring(i));
    foreach(i; 10..26)
        expect(3, ring(i));
    foreach(i; 26..50)
        expect(4, ring(i));
}

// Computes numbers in cardinal directions of ring
uint[] cardinalDirNums(uint ring){
    const borderLength = ring*2-1;
    const eastNum = (2*(ring-1)-1)^^2 + borderLength/2;

    uint[] res;
    foreach(i; 0..4)
        res ~= eastNum + i*(borderLength-1);
    return res;
}

unittest{
    expect([2,4,6,8], cardinalDirNums(2));
    expect([11,15,19,23], cardinalDirNums(3));
}

/// Computes manhattan distance from number n to 1 (on ring 1)
uint distance(uint n){
    const r = ring(n);
    const distToNESW = r.cardinalDirNums.map!(num => abs(n-num)).reduce!min;
    const distFromNESW = r-1;

    return distToNESW + distFromNESW;
}

unittest{
    expect(0, distance(1));
    expect(3, distance(12));
    expect(2, distance(23));
    expect(31, distance(1024));
}

//============================================================================
// Puzzle 2
//============================================================================
// Notes:
// - Horizontal & vertical distances from puzzle 1 could be used as coordinates
import std.typecons;
import std.algorithm: max, minIndex;
import std.range: repeat, chain, take, generate;

struct Vec2D{
    int x;
    int y;

    this(int x, int y){
        this.x = x;
        this.y = y;
    }

    Vec2D opBinary(string op)(Vec2D rhs) if(op == "+") {
        return Vec2D(this.x + rhs.x, this.y + rhs.y);
    }

    Vec2D[] neighbours() const {
        Vec2D[] res;
        for(int x=-1; x<=1; ++x){
            for(int y=-1; y<=1; ++y){
                if(this.x != x || this.y != y)
                    res ~= Vec2D(this.x + x, this.y + y);
            }
        }
        return res;
    }
}

auto infiniteMoves(uint startRing = 2){
    uint curRing = startRing;
    auto r = movesForRing(curRing);
    
    return (){
        if(r.empty)
            r = movesForRing(++curRing);

        auto res = r.front;
        r.popFront;
        return res;
    };
}

auto movesForRing(uint ring){
    assert(ring>1);

    auto r = Vec2D(1,0).repeat;
    auto u = Vec2D(0,1).repeat;
    auto l = Vec2D(-1,0).repeat;
    auto d = Vec2D(0,-1).repeat;

    const borderLength = ring*2-1;
    return chain(r.take(1), u.take(borderLength-2), l.take(borderLength-1), d.take(borderLength-1), r.take(borderLength-1));
}

auto calcUntilOver(uint limit){
    uint[Vec2D] mem;
    mem[Vec2D(0,0)] = 1;

    auto cur = Vec2D(0,0);
    auto moves = generate(infiniteMoves());
    for(auto move = moves.front; mem[cur] <= limit; moves.popFront, move = moves.front){
        cur = cur + move;
        uint sum;
        foreach(n; cur.neighbours){
            const p = n in mem;
            if(p) sum += *p;
        }
        mem[cur] = sum;
    }
    return mem[cur];
}

unittest{
    expect(25, calcUntilOver(23));
}