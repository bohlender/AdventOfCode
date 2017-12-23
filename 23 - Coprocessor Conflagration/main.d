import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = input.mulCount;
        auto res2 = input.valOfH;
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
import std.string: strip, splitLines, isNumeric, startsWith;
import std.conv: to;

class Context{
    long[string] mem;
    size_t pc;

    long eval(in string s) const{
        return s.isNumeric ? s.to!long : mem.get(s,0);
    }
    override string toString() const {
        return format("PC: %d, Mem: %s", pc, mem);
    }
}

abstract class Instruction{
    string X;
    string Y;
    bool exec(Context s) const;
}
class Set : Instruction{
    override bool exec(Context s) const{
        s.mem[X] = s.eval(Y);
        ++s.pc;
        return true;
    }
}
class Sub : Instruction{
    override bool exec(Context s) const{
        s.mem[X] -= s.eval(Y);
        ++s.pc;
        return true;
    }
}
class Mul : Instruction{
    override bool exec(Context s) const{
        s.mem[X] *= s.eval(Y);
        ++s.pc;
        return true;
    }
}
class Jnz : Instruction{
    override bool exec(Context s) const{
        if(s.eval(X) != 0)
            s.pc += s.eval(Y);
        else
            ++s.pc;
        return true;
    }
}

Instruction[] parse(in string s){
    Instruction[] res;
    foreach(line; s.strip.splitLines){
        if(line.startsWith("set")){
            auto instr = new Set;
            line.formattedRead!"set %s %s"(instr.X, instr.Y);
            res ~= instr;
        }else if(line.startsWith("sub")){
            auto instr = new Sub;
            line.formattedRead!"sub %s %s"(instr.X, instr.Y);
            res ~= instr;
        }else if(line.startsWith("mul")){
            auto instr = new Mul;
            line.formattedRead!"mul %s %s"(instr.X, instr.Y);
            res ~= instr;
        }else if(line.startsWith("jnz")){
            auto instr = new Jnz;
            line.formattedRead!"jnz %s %s"(instr.X, instr.Y);
            res ~= instr;
        }else assert(false);
    }
    return res;
}

auto mulCount(in Instruction[] prog){
    auto ctx = new Context;
    size_t count;
    while(ctx.pc >=0 && ctx.pc<prog.length){
        if(cast(Mul)prog[ctx.pc])
            ++count;
        prog[ctx.pc].exec(ctx);
    }
    return count;
}

//============================================================================
// Puzzle 2
//============================================================================
auto valOfH(in Instruction[] prog){
    long a=1,b,c,d,e,f,g,h;

    b = 79;
    c = b;
    if(a != 0){
        b = b*100 + 100_000;
        c = b + 17_000;
    }
    
    loc8:
    f = 1;
    d = 2;
    
    for(d=2; d != b; ++d){
        e = b;
        if(b%d == 0){
            f = 0;
            break;
        }
    }
    d = b;

    if(f == 0)
        h += 1;
    
    if(b != c){
        b += 17;
        goto loc8;
    }
    return h;
}