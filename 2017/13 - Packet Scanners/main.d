import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: splitLines;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = new State(input).tripSeverity;
        auto res2 = input.idealDelay;
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
import std.array: appender;
import std.algorithm: maxElement, each;

static uint[uint] parse(in string content){
    uint[uint] res;
    foreach(line; content.splitLines){
        uint layer, range;
        line.formattedRead!("%d: %d")(layer, range);
        res[layer] = range;
    }
    return res;
}

class State{
    uint pos;
    const(uint[uint]) ranges;
    uint[uint] scannerPos;
    bool[uint] scannerMovesDown;

    this(in uint[uint] ranges){
        // Default init
        pos = 0;
        this.ranges = ranges;
        foreach(k; ranges.byKey){
            scannerPos[k] = 0;
            scannerMovesDown[k] = true;
        }
    }

    this(in State other){
        pos = other.pos;
        ranges = other.ranges;
        scannerPos = cast(uint[uint])other.scannerPos.dup;
        scannerMovesDown = cast(bool[uint])other.scannerMovesDown.dup;
    }

    void delayBy(in uint delay){
        auto oldPos = pos;
        foreach(_; 0..delay)
            step();
        pos = oldPos;
    }
    uint maxLayer() const @property {return ranges.byKey.maxElement;}
    bool isFinal() const @property {return pos >= maxLayer;}
    uint severity() const @property {return detected ? pos*ranges[pos] : 0;}
    bool detected() const @property {return (pos in scannerPos) && (scannerPos[pos] == 0);}
    void step(){
        // Move player
        ++pos;

        // Move scanners
        foreach(k; scannerPos.byKey){
            scannerPos[k] = scannerMovesDown[k] ? scannerPos[k]+1 : scannerPos[k]-1;
            if(scannerMovesDown[k] && scannerPos[k] == ranges[k]-1)
                scannerMovesDown[k] = false;
            else if(!scannerMovesDown[k] && scannerPos[k] == 0)
                scannerMovesDown[k] = true;
        }
    }

    override string toString() const{
        auto strBldr = appender!string;
        foreach(layer; 0..maxLayer+1){
            strBldr.put("%d: ".format(layer));
            foreach(value; 0..ranges.get(layer, 0)) {
                auto entry = scannerPos[layer] == value ? "S" : " ";
                auto entryFormat = layer == pos && value == 0 ? "(%s)" : "[%s]";
                strBldr.put(entryFormat.format(entry));
            }
            strBldr.put("\n");
        }
        return strBldr.data;
    }
}

static uint tripSeverity(in State init){
    auto s = new State(init);
    uint severity;
    while(!s.isFinal){
        s.step;
        severity += s.severity;
    }
    return severity;
}

//============================================================================
// Puzzle 2
//============================================================================
static bool tripIsSafe(in State from){
    auto s = new State(from);
    if(s.detected)
        return false;

    while(!s.isFinal){
        s.step;
        if(s.detected)
            return false;
    }
    return true;
}

static uint idealDelay(in uint[uint] ranges){
    auto s = new State(ranges);

    uint cnt;
    for(cnt = 0; !s.tripIsSafe; ++cnt){
        s.delayBy(1);
        //if(cnt % 10000 == 0) writeln("Delay ",cnt);
    }
    return cnt;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto contents = r"0: 3
1: 2
4: 4
6: 4";
    auto input = contents.parse;
    auto init = new State(input);
    expect(24, init.tripSeverity);
    expect(10, input.idealDelay);
}