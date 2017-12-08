import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: split, splitLines, isNumeric;
import std.algorithm: map, maxElement, max;
import std.conv: to;
import std.typecons: Tuple;
import std.array: array;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        auto input = contents.splitLines.map!(parseInstr).array;
        auto res1 = maxRegAtEnd(input);
        //auto res2 = cyclesInLoop(input);
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
alias State = int[string];
alias Instr = Tuple!(string, "reg", bool, "inc", int, "val", string, "cond");

static Instr parseInstr(in string s){
    Instr res;

    auto cp = s.dup;
    string op;
    cp.formattedRead("%s %s %d if ", res.reg, op, res.val);
    res.inc = op == "inc";
    res.cond = cp.idup;
    return res;
}

static bool eval(in State s, in string condition){
    auto cp = condition.dup;
    string lhs, op, rhs;
    cp.formattedRead("%s %s %s", lhs, op, rhs);

    auto lhsVal = lhs.isNumeric ? lhs.to!int : s.get(lhs, 0);
    auto rhsVal = rhs.isNumeric ? rhs.to!int : s.get(rhs, 0);
    switch(op) {
        case ">":  return lhsVal >  rhsVal;
        case "<":  return lhsVal <  rhsVal;
        case ">=": return lhsVal >= rhsVal;
        case "<=": return lhsVal <= rhsVal;
        case "==": return lhsVal == rhsVal;
        case "!=": return lhsVal != rhsVal;
        default: assert(false);
    }
}

unittest{
    State s = ["a":1, "b":-2];
    expect(false, eval(s, "a == b"));
    expect(true, eval(s, "x > b"));
}

static void step(ref State s, in Instr instr){
    if(s.eval(instr.cond)){
        s[instr.reg] = s.get(instr.reg, 0) + (instr.inc ? instr.val : -instr.val);
    }
}

static uint maxRegAtEnd(in Instr[] prog){
    State s;
    foreach(instr; prog)
        s.step(instr);
    return max(0, s.byValue.maxElement);
}

unittest{
    auto contents = r"b inc 5 if a > 1
a inc 1 if b < 5
c dec -10 if a >= 1
c inc -20 if c == 10";
    auto input = contents.splitLines.map!(parseInstr).array;
    maxRegAtEnd(input);
}