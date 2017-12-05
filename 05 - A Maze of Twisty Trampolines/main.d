import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: splitLines;
import std.algorithm: map;
import std.conv: to;
import std.array: array;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        auto input = contents.splitLines.map!(to!int).array;
        auto res1 = stepsUntilOOB(input, offsetUpdate1);
        auto res2 = stepsUntilOOB(input, offsetUpdate2);
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
class State{
    this(in int pc, in int[] jumps){
        this.pc = pc;
        this.jumps = jumps.dup;
    }

    bool pcInBounds() const @property {
        return pc >= 0 && pc < jumps.length;
    }

    void step(int function(in int oldOffset) offsetUpdate = offsetUpdate1){
        auto oldPc = pc;
        pc = jumps[pc]+pc;
        jumps[oldPc] = offsetUpdate(jumps[oldPc]);
    }
protected:
    int pc;
    int[] jumps;
}

unittest{
    auto init = new State(0,[0,3,0,1,-3]);

    init.step;
    expect(0, init.pc);
    expect([1,3,0,1,-3], init.jumps);

    init.step;
    expect(1, init.pc);
    expect([2,3,0,1,-3], init.jumps);

    init.step;
    expect(4, init.pc);
    expect([2,4,0,1,-3], init.jumps);

    init.step;
    expect(1, init.pc);
    expect([2,4,0,1,-2], init.jumps);

    init.step;
    expect(5, init.pc);
    expect([2,5,0,1,-2], init.jumps);
}

uint stepsUntilOOB(in int[] jumps, int function(in int oldOffset) offsetUpdate){
    auto init = new State(0, jumps);

    uint c;
    for(c=0; init.pcInBounds; ++c)
        init.step(offsetUpdate);
    return c;
}

static auto offsetUpdate1 = (const(int) offset) => offset+1;

unittest{
    expect(5, stepsUntilOOB([0,3,0,1,-3], offset => offset+1));
}

//============================================================================
// Puzzle 2
//============================================================================
static auto offsetUpdate2 = (const(int) offset) => offset>=3 ? offset-1 : offset+1;

unittest{
    auto init = new State(0,[0,3,0,1,-3]);
    foreach(i; 0..10)
        init.step(offsetUpdate2);
    expect(false, init.pcInBounds);
    expect([2,3,2,3,-1], init.jumps);

    expect(10, stepsUntilOOB([0,3,0,1,-3], offsetUpdate2));
}