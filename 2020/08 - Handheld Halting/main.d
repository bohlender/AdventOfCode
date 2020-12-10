import std.file: readText;
import std.stdio;
import std.string;
import std.algorithm;
import std.range;
import std.typecons;
import std.format;
import std.conv;
import std.container;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = args[1].readText.parse;
        auto res1 = input.sol1;
        auto res2 = input.sol2;
        writefln!"First: %s\nSecond: %s"(res1, res2);
    }
}

auto parse(in string s) {
    auto res = appender!(Instr[]);
    foreach(line; s.lineSplitter()) {
        string opStr;
        int arg;
        line.formattedRead!"%s %d"(opStr, arg);
        res ~= Instr(opStr.to!Op, arg);
    }
    return res[];
}

//==============================================================================
// Puzzle 1
//==============================================================================
enum Op {acc, jmp, nop}
alias Instr = Tuple!(Op, "op", int, "arg");
alias State = Tuple!(uint, "pc", int, "acc");

auto step(ref State s, in Instr instr) {
    final switch(instr.op) {
        case Op.acc:
            s.acc += instr.arg;
            s.pc++;
            break;
        case Op.jmp:
            s.pc += instr.arg;
            break;
        case Op.nop:
            s.pc++;
    }
}

auto run(in Instr[] prog) {
    State s;
    auto visited = redBlackTree!uint;
    while(s.pc !in visited && s.pc < prog.length) {
        visited.insert(s.pc);
        s.step(prog[s.pc]);
    }
    return s;
}

auto sol1(in Instr[] prog) {
    return prog.run.acc;
}

//==============================================================================
// Puzzle 2
//==============================================================================
auto flip(ref Instr instr) {
    if(instr.op == Op.nop)
        instr.op = Op.jmp;
    else if(instr.op == Op.jmp)
        instr.op = Op.nop;
}

auto sol2(Instr[] prog) {
    foreach(ref instr; prog) {
        if(instr.op.among(Op.nop, Op.jmp)) {
            instr.flip;
            auto s = prog.run;
            if(s.pc == prog.length)
                return s.acc;
            instr.flip;
        }
    }
    assert(0);
}

//==============================================================================
// Unittests
//==============================================================================
unittest {
    auto content =
`nop +0
acc +1
jmp +4
acc +3
jmp -3
acc -99
acc +1
jmp -4
acc +6`;
    auto input = content.parse;
    expect(5, input.sol1);
    expect(8, input.sol2);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format!"Expected %s but got %s"(expected, actual), file, line);
}