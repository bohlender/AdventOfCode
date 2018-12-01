import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: strip, splitLines;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = input.nearestAfter(10_000); // TODO: Use analytic solution
        auto res2 = input.leftAfter(10_000); // TODO: Use analytic solution
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
import std.string: split;
import std.math: abs;
import std.algorithm: minIndex, map;

struct Vec3D{
    int x, y, z;
    auto distToOrigin() const {return x.abs + y.abs + z.abs;}
    Vec3D opBinary(string op)(Vec3D rhs) const if(op == "+") {
        return Vec3D(x+rhs.x, y+rhs.y, z+rhs.z);
    }
    int opCmp(Vec3D b) const {
        if(x == b.x){
            if(y == b.y)
                return z - b.z;
            return y - b.y;
        }
        return x - b.x;
    }
}

struct Particle{
    Vec3D p, v, a;
    Particle step(in uint t = 1) const {
        Vec3D newV = v;
        Vec3D newP = p;
        foreach(_; 0..t){
            newV = newV + a;
            newP = p + newV;
        }
        return Particle(newP, newV, a);
    }
}

Particle[] parse(in string s){
    Particle[] res;
    foreach(line; s.strip.splitLines){
        Particle p;
        line.formattedRead!"p=< %d, %d, %d>, v=< %d, %d, %d>, a=< %d, %d, %d>"(p.p.x, p.p.y, p.p.z, p.v.x, p.v.y, p.v.z, p.a.x, p.a.y, p.a.z);
        res ~= p;
    }
    return res;
}

auto nearestAfter(in Particle[] particles, in uint steps) {
    auto less = (Particle lhs, Particle rhs) => lhs.p.distToOrigin < rhs.p.distToOrigin;
    return particles.map!(p => p.step(steps)).minIndex!(less);
}

//============================================================================
// Puzzle 2
//============================================================================
import std.algorithm: group, sort, filter;
import std.array: array;

Particle[] removeColliding(in Particle[] particles) {
    auto less = (Particle lhs, Particle rhs) => lhs.p < rhs.p;
    auto eq = (Particle lhs, Particle rhs) => lhs.p == rhs.p;
    auto groups = particles.dup.sort!(less).group!(eq); // histogram
    return groups.filter!(g => g[1]==1).map!(g => g[0]).array; // keep uniques
}

auto leftAfter(in Particle[] particles, in uint steps){
    auto curParticles = particles.removeColliding;
    foreach(_; 0..steps)
        curParticles = curParticles.map!(p => p.step).array.removeColliding;
    return curParticles.length;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto contents = r"p=< 3,0,0>, v=< 2,0,0>, a=<-1,0,0>
p=< 4,0,0>, v=< 0,0,0>, a=<-2,0,0>";
    auto input = contents.parse;
    expect(0, input.nearestAfter(4));
}

unittest{
    auto contents = r"p=<-6,0,0>, v=< 3,0,0>, a=< 0,0,0>
p=<-4,0,0>, v=< 2,0,0>, a=< 0,0,0>
p=<-2,0,0>, v=< 1,0,0>, a=< 0,0,0>
p=< 3,0,0>, v=<-1,0,0>, a=< 0,0,0>";
    auto input = contents.parse;
    expect(1, input.leftAfter(3));
}