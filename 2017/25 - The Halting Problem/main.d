import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = input.checksum;
        //auto res2 = input.maxLengthAndStrength.str;
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
import std.typecons: Tuple;
import std.string: split, splitLines, strip;
import std.range: chunks;
import std.conv: to;
import std.algorithm: count;

alias Rule = Tuple!(bool, "writeVal", bool, "moveRight", char, "nextState");
alias Pair = Tuple!(char, "rule", bool, "val");
alias Problem = Tuple!(char, "initRule", size_t, "numSteps", Rule[Pair], "rules");
class TM{
    this(in char initState){curState = initState;}
    
    bool curVal() const @property {return tape.get(pos, false);}
    auto checksum() const @property {return tape.byValue.count(true);}

    void step(in Rule[Pair] rules){
        auto rule = rules[Pair(curState, curVal)];
        writeVal(rule.writeVal);
        pos += rule.moveRight ? 1 : -1;
        curState = rule.nextState;
    }
    
    void writeVal(in bool val){
        if(val){
            tape[pos] = true;
        }else{
            tape.remove(pos);
        }
    }
private:
    bool[int] tape;
    int pos;
    char curState;
}
auto parse(in string s){
    auto blocks = s.split("\n\n");

    // Problem description block
    Problem p;
    blocks[0].formattedRead!"Begin in state %c.\n"(p.initRule);
    blocks[0].formattedRead!"Perform a diagnostic checksum after %d steps."(p.numSteps);

    // Blocks of rules
    foreach(i, block; blocks[1..$]){
        char ruleName;
        block.formattedRead!"In state %c:"(ruleName);
        foreach(ruleLines; block.strip.splitLines.chunks(4)){
            Rule rule;
            int curVal;
            ruleLines[0].formattedRead!" If the current value is %d:"(curVal);

            int writeVal;
            ruleLines[1].formattedRead!" - Write the value %d."(writeVal);
            rule.writeVal = writeVal.to!bool;

            string dir;
            ruleLines[2].formattedRead!" - Move one slot to the %s."(dir);
            rule.moveRight = dir == "right";

            ruleLines[3].formattedRead!" - Continue with state %c."(rule.nextState);

            p.rules[Pair(ruleName, curVal.to!bool)] = rule;
        }
    }
    return p;
} 

auto checksum(in Problem p){
    auto tm = new TM(p.initRule);
    foreach(_; 0..p.numSteps){
        tm.step(p.rules);
    }
    return tm.checksum;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto contents = r"Begin in state A.
Perform a diagnostic checksum after 6 steps.

In state A:
  If the current value is 0:
    - Write the value 1.
    - Move one slot to the right.
    - Continue with state B.
  If the current value is 1:
    - Write the value 0.
    - Move one slot to the left.
    - Continue with state B.

In state B:
  If the current value is 0:
    - Write the value 1.
    - Move one slot to the left.
    - Continue with state A.
  If the current value is 1:
    - Write the value 1.
    - Move one slot to the right.
    - Continue with state A.";
    auto input = contents.parse;
    expect(3, input.checksum);
}