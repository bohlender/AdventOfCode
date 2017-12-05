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
        auto res1 = stepsUnitOOB(input);
        //auto res2 = calcUntilOver(input);
        //writefln("First: %s\nSecond: %s", res1, res2);
        writeln(res1);
    }
}

void expect(T1, T2)(T1 expected, T2 actual) if(is(typeof(expected == actual) == bool)) {
    assert(expected == actual, format("Expected %s but got %s", expected, actual));
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

    void step(){
        auto oldPc = pc;
        pc = jumps[pc]+pc;
        jumps[oldPc] += 1;
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

uint stepsUnitOOB(in int[] jumps){
    auto init = new State(0, jumps);

    uint c;
    for(c=0; init.pcInBounds; ++c)
        init.step;
    return c;
}

unittest{
    expect(5, stepsUnitOOB([0,3,0,1,-3]));
}