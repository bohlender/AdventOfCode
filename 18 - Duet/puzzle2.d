import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res = input.numProg1Send;
        writeln(res);
    }
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}

//============================================================================
// Puzzle 2
//============================================================================
import std.string: strip, splitLines, isNumeric, startsWith;
import std.conv: to;

class State{
    State other;
    long[string] mem;
    long[] msgQueue;
    uint pc;

    long eval(in string s) const{
        return s.isNumeric ? s.to!long : mem.get(s,0);
    }
    override string toString() const {
        return format("PC: %d, msgQueue: %s\nMem: %s", pc, msgQueue, mem);
    }
}

abstract class Instruction{
    string X;
    bool exec(State s) const;
}
class Snd : Instruction{
    override bool exec(State s) const{
        s.other.msgQueue ~= s.eval(X);
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
        if(s.msgQueue.length>0){
            ++s.pc;
            s.mem[X] = s.msgQueue[0];
            s.msgQueue = s.msgQueue[1..$];
            return true;
        }
        return false;
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

auto numProg1Send(in Instruction[] instructions){
    auto s0 = new State();
    auto s1 = new State();
    s0.mem["p"] = 0;
    s0.other = s1;
    s1.mem["p"] = 1;
    s1.other = s0;
    
    uint cnt;
    do{
        if(s1.pc<instructions.length && cast(Snd)instructions[s1.pc])
            ++cnt;
    }while((s1.pc<instructions.length && instructions[s1.pc].exec(s1))
        || (s0.pc<instructions.length && instructions[s0.pc].exec(s0)));

    return cnt;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto contents = r"snd 1
snd 2
snd p
rcv a
rcv b
rcv c
rcv d";
    auto input = contents.parse;
    expect(3, input.numProg1Send());
}