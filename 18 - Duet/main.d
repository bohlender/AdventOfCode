import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = input.firstRcvVal;
        //auto res2 = input.valAfter0(50_000_000);
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
import std.string: strip, splitLines, isNumeric, startsWith;
import std.conv: to;

class State{
    long[string] mem;
    long lastFreqPlayed;
    uint pc;

    long eval(in string s) const{
        return s.isNumeric ? s.to!long : mem.get(s,0);
    }
    override string toString() const {
        return format("PC: %d, LastFreq: %d\nMem: %s", pc, lastFreqPlayed, mem);
    }
}

abstract class Instruction{
    string X;
    bool exec(State s) const;
}
class Snd : Instruction{
    override bool exec(State s) const{
        s.lastFreqPlayed = s.eval(X);
        ++s.pc;
        return true;
    }
}
class Set : Instruction{
    string Y;
    override bool exec(State s) const{
        s.mem[X] = s.eval(Y);
        ++s.pc;
        return true;
    }
}
class Add : Instruction{
    string Y;
    override bool exec(State s) const{
        s.mem[X] += s.eval(Y);
        ++s.pc;
        return true;
    }
}
class Mul : Instruction{
    string Y;
    override bool exec(State s) const{
        s.mem[X] *= s.eval(Y);
        ++s.pc;
        return true;
    }
}
class Mod : Instruction{
    string Y;
    override bool exec(State s) const{
        s.mem[X] %= s.eval(Y);
        ++s.pc;
        return true;
    }
}
class Rcv : Instruction{
    override bool exec(State s) const{
        ++s.pc;
        return s.eval(X) == 0;
    }
}
class Jgz : Instruction{
    string Y;
    override bool exec(State s) const{
        if(s.eval(X) > 0)
            s.pc += s.eval(Y);
        else
            ++s.pc;
        return true;
    }
}

Instruction[] parse(in string s){
    Instruction[] res;
    foreach(line; s.strip.splitLines){
        if(line.startsWith("snd")){
            auto instr = new Snd;
            line.formattedRead!"snd %s"(instr.X);
            res ~= instr;
        }else if(line.startsWith("set")){
            auto instr = new Set;
            line.formattedRead!"set %s %s"(instr.X, instr.Y);
            res ~= instr;
        }else if(line.startsWith("add")){
            auto instr = new Add;
            line.formattedRead!"add %s %s"(instr.X, instr.Y);
            res ~= instr;
        }else if(line.startsWith("mul")){
            auto instr = new Mul;
            line.formattedRead!"mul %s %s"(instr.X, instr.Y);
            res ~= instr;
        }else if(line.startsWith("mod")){
            auto instr = new Mod;
            line.formattedRead!"mod %s %s"(instr.X, instr.Y);
            res ~= instr;
        }else if(line.startsWith("rcv")){
            auto instr = new Rcv;
            line.formattedRead!"rcv %s"(instr.X);
            res ~= instr;
        }else if(line.startsWith("jgz")){
            auto instr = new Jgz;
            line.formattedRead!"jgz %s %s"(instr.X, instr.Y);
            res ~= instr;
        }else assert(false);
    }
    return res;
}

auto firstRcvVal(in Instruction[] instructions){
    auto s = new State();
    while(instructions[s.pc].exec(s)){}
    return s.lastFreqPlayed;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto contents = r"set a 1
add a 2
mul a a
mod a 5
snd a
set a 0
rcv a
jgz a -1
set a 1
jgz a -2";
    auto input = contents.parse;
    expect(4, input.firstRcvVal);
}