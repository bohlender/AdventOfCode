import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = input.numInfections(10_000);
        auto res2 = input.numInfections(10_000_000, false);
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
// Puzzle 1/2
//============================================================================
import std.string: strip, splitLines;

enum Status {Clean, Weakened, Infected, Flagged}

struct Vec2D{
    int x, y;
    Vec2D opBinary(string op)(Vec2D rhs) if(op=="+") {
        return Vec2D(x+rhs.x, y+rhs.y);
    }
    Vec2D opUnary(string op)() if(op=="-"){
        return Vec2D(-x,-y);
    }
    void rotR(){
        auto oldX = x;
        x = -y;
        y = oldX;
    }
    void rotL(){foreach(_; 0..3) rotR();}
}
class State{
    this(in Vec2D pos){
        this._pos = pos;
        this._dir = Vec2D(0, -1);
    }
    this(in State other){
        this._pos = other.pos;
        this._dir = other._dir;
        // TODO: this._infected = other._infected.dup;
        foreach(pos; other._infected.byKey)
            this.infect(pos);
    }
    auto pos() const @property {return _pos;}
    override string toString() const{
        return format!"Pos: %s, Dir: %s\n%s"(pos, _dir, _infected);
    }
    void infect(in Vec2D pos) {_infected[pos]=Status.Infected;}
    void clean(in Vec2D pos) {_infected.remove(pos);}
    Status getStatus(in Vec2D pos) const @property {return _infected.get(pos,Status.Clean);}
    bool isInfected(in Vec2D pos) const @property {return getStatus(pos) == Status.Infected;}

    bool burst(in bool puzzle1){
        bool didInfect = false;
        auto status = getStatus(pos);
        if(puzzle1){
            if(status == Status.Infected){
                _dir.rotR;
                clean(pos);
            }else{
                _dir.rotL;
                infect(pos);
                didInfect = true;
            }
        }else{
            final switch(status){
                case Status.Clean:
                    _dir.rotL;
                    _infected[pos] = Status.Weakened;
                    break;
                case Status.Weakened:
                    infect(pos);
                    didInfect = true;
                    break;
                case Status.Infected:
                    _infected[pos] = Status.Flagged;
                    _dir.rotR;
                    break;
                case Status.Flagged:
                    clean(pos);
                    _dir = -_dir;
                    break;
            }
        }
        _pos = _pos + _dir;
        return didInfect;
    }
private:
    Vec2D _pos;
    Vec2D _dir;
    Status[Vec2D] _infected;
}

State parse(in string s){
    auto lines = s.strip.splitLines;
    auto width = lines[0].length;
    auto height = lines.length;
    auto initPos = Vec2D(width/2, height/2);

    auto initState = new State(initPos);
    foreach(y, line; lines)
        foreach(x, c; line)
            if(c == '#')
                initState.infect(Vec2D(x,y));
    return initState;
}

auto numInfections(in State initState, size_t bursts, bool puzzle1=true){
    auto s = new State(initState);
    size_t res;
    foreach(_; 0..bursts)
        if(s.burst(puzzle1))
            ++res;
    return res;
}

//============================================================================
// Unittests
//============================================================================

unittest{
    auto dir = Vec2D(0,-1);
    dir.rotR;
    expect(Vec2D(1,0), dir);
    dir.rotR;
    expect(Vec2D(0,1), dir);
    dir.rotR;
    expect(Vec2D(-1,0), dir);

    auto contents = r"..#
#..
...";
    auto input = contents.parse;

    expect(5, input.numInfections(7));
    expect(41, input.numInfections(70));
    expect(5587, input.numInfections(10_000));

    expect(26, input.numInfections(100, false));
    expect(2511944, input.numInfections(10_000_000, false));
}