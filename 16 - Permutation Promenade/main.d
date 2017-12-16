import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: strip, split;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = input.dance;
        auto res2 = input.dance(1_000_000_000);
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
import std.range: iota;
import std.array: array;
import std.algorithm: swap, find;

abstract class Move{
    void apply(ref char[] state) const;
}
class Spin : Move{
    const uint count;
    this(in uint count){this.count = count;}
    override void apply(ref char[] state) const{
        state = state[$-count..$] ~ state[0..$-count];
    }
}
class Exchange : Move{
    const uint pos1, pos2;
    this(in uint pos1, in uint pos2){this.pos1 = pos1; this.pos2 = pos2;}
    override void apply(ref char[] state) const{
        swap(state[pos1], state[pos2]);
    }
}
class Partner : Move{
    const char p1, p2;
    this(in char p1, in char p2){this.p1 = p1; this.p2 = p2;}
    override void apply(ref char[] state) const{
        auto r1 = state.find(p1);
        auto r2 = state.find(p2);
        swap(r1[0], r2[0]);
    }
}

static Move[] parse(in string contents){
    Move[] res;
    foreach(s; contents.strip.split(',')){
        switch(s[0]) {
            case 's': // Spin
                uint count;
                s.formattedRead!("s%d")(count);
                res ~= new Spin(count);
                break;
            case 'x': // Exchange
                uint pos1, pos2;
                s.formattedRead!("x%d/%d")(pos1,pos2);
                res ~= new Exchange(pos1, pos2);
                break;
            case 'p': // Partner
                char p1, p2;
                s.formattedRead!("p%c/%c")(p1,p2);
                res ~= new Partner(p1,p2);
                break;
        default:
            assert(false);
        }
    }
    return res;
}

static char[] dance(in Move[] moves, in char[] initState = iota('a','q').array){
    auto s = initState.dup;
    foreach(m; moves)
        m.apply(s);
    return s;
}

//============================================================================
// Puzzle 2
//============================================================================
static char[] dance(in Move[] moves, in uint times, in char[] initState = iota('a','q').array){
    auto s = initState.dup;

    for(uint i = 0; i<times; ++i){
        s = moves.dance(s);
        // Fast-forward if cycling at some point
        if(s == initState)
            i = (times - times%(i+1)) - 1;
    }
    return s;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto moves = "s1,x3/4,pe/b".parse; 
    expect("baedc", moves.dance("abcde"));
}