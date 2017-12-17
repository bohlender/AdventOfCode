import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: strip, split;
import std.conv: to;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.to!uint;
        auto res1 = input.valAfter(2017);
        auto res2 = input.valAfter0(50_000_000);
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
import std.typecons: Tuple;

//alias State = Tuple!(uint[], "buf", uint, "pos");
struct State{
    uint[] buf;
    size_t pos;
}

static State step(in uint stepSize, in uint iterations){
    auto s = State([0],0);
    foreach(i; 0..iterations){
        const dstPos = (s.pos+stepSize) % s.buf.length;
        s.buf = s.buf[0..dstPos+1] ~ (i+1) ~ s.buf[dstPos+1..$];
        assert(dstPos+1<s.buf.length);
        s.pos = dstPos+1;
    }
    return s;
}

static uint valAfter(in uint stepSize, in uint iterations){
    auto s = stepSize.step(iterations);
    auto nextPos = (s.pos+1) % s.buf.length;
    return s.buf[nextPos];
}

//============================================================================
// Puzzle 2
//============================================================================
static uint valAfter0(in uint stepSize, in uint iterations){
    uint pos = 0;
    uint valAt0 = 0;
    foreach(i; 0..iterations){
        pos = (pos+stepSize) % (i+1) + 1;
        if(pos == 1)
            valAt0 = i+1;
    }
    return valAt0;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    expect(638, 3.valAfter(2017));
}