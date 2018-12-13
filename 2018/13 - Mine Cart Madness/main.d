import std.file: readText;
import std.conv;
import std.stdio;
import std.format;
import std.string;
import std.algorithm;
import std.range;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = args[1].readText.parse;
        auto res1 = input.firstCrash;
        auto res2 = input.lastCart;
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.typecons;

enum Dir{N=1, E=2, S=4, W=8};
alias Cell = BitFlags!Dir;
enum Choice{Left, Straight, Right};

Dir rotR(in Dir dir){return dir==Dir.W ? Dir.N : dir<<1;}
auto rot(in Dir dir, in Choice choice){
    final switch(choice){
        case Choice.Left: return dir.rotR.rotR.rotR;
        case Choice.Straight: return dir;
        case Choice.Right: return dir.rotR;
    }
}

struct Cart{
    int x, y;
    Dir dir;
    Choice nextChoice;
    int opCmp(Cart rhs) const {return (y==rhs.y) ? x-rhs.x : y-rhs.y;}
}

struct State{
    Cart[] carts;
    const ulong width, height;
    const Cell[] cells;

    auto step(){
        ulong[] crashedIdx;
        carts = carts.sort.array;
        foreach(i, ref cart; carts){
            // Do not move crashed
            if(crashedIdx.canFind(i))
                continue;

            // Move cart
            final switch(cart.dir){
                case Dir.N: cart.y--; break;
                case Dir.E: cart.x++; break;
                case Dir.S: cart.y++; break;
                case Dir.W: cart.x--; break;
            }
            const crashed = crashedIdx.map!(idx => carts[idx]).array;
            const crashedInto = carts.find!(c => tuple(c.x, c.y) == tuple(cart.x, cart.y) && c != cart && !crashed.canFind(c));
            if(!crashedInto.empty)
                crashedIdx ~= [i, carts.length-crashedInto.length];

            // Update direction
            auto c = cells.chunks(width)[cart.y][cart.x];
            if(c == Cell(Dir.W | Dir.N))
                cart.dir = cart.dir & Dir.E ? Dir.N : Dir.W;
            else if(c == Cell(Dir.E | Dir.S))
                cart.dir = cart.dir & Dir.W ? Dir.S : Dir.E;
            else if(c == Cell(Dir.S | Dir.W))
                cart.dir = cart.dir & Dir.E ? Dir.S : Dir.W;
            else if(c == Cell(Dir.N | Dir.E))
                cart.dir = cart.dir & Dir.W ? Dir.N : Dir.E;
            else if(c == Cell(Dir.N | Dir.E | Dir.S | Dir.W)){
                cart.dir = cart.dir.rot(cart.nextChoice);
                cart.nextChoice = ((cart.nextChoice+1)%(Choice.max+1)).to!Choice;
            }
        }
        // Remove crashed carts
        auto crashed = crashedIdx.map!(idx => carts[idx]).array;
        foreach(cart; crashed)
            carts = carts.remove!(c => c==cart);
        return crashed;
    }
}

auto firstCrash(in State input){
    auto cur = State(input.carts.dup, input.width, input.height, input.cells);
    Cart[] crashed;
    do{
        crashed = cur.step;
    }while(crashed.empty);
    return tuple(crashed.front.x, crashed.front.y);
}

auto cell2char(in Cell c){
    if(c == Cell(Dir.N | Dir.S)) return '|';
    if(c == Cell(Dir.E | Dir.W)) return '-';
    if(c == Cell(Dir.W | Dir.N)) return '/';
    if(c == Cell(Dir.E | Dir.S)) return '/';
    if(c == Cell(Dir.S | Dir.W)) return '\\';
    if(c == Cell(Dir.N | Dir.E)) return '\\';
    if(c == Cell(Dir.N | Dir.E | Dir.S | Dir.W)) return '+';
    return ' ';
}

auto char2cell(in char c, in bool hasWest=false){
    switch(c){
        case '|','v','^': return Cell(Dir.N | Dir.S);
        case '-','<','>': return Cell(Dir.E | Dir.W);
        case '/':  return hasWest ? Cell(Dir.W | Dir.N)
                                  : Cell(Dir.E | Dir.S);
        case '\\': return hasWest ? Cell(Dir.S | Dir.W)
                                  : Cell(Dir.N | Dir.E);
        case '+': return Cell(Dir.N | Dir.E | Dir.S | Dir.W);
        default: return Cell();
    }
}

auto parse(in string s){
    Cart[] carts;
    Cell[] cells;
    auto lines = s.splitLines;
    foreach(int y, line; lines){
        foreach(int x, c; line){
            const hasWest = x!=0 && cells[$-1].E;
            cells ~= c.char2cell(hasWest);
            switch(c){
                case '^': carts ~= Cart(x,y,Dir.N); break;
                case '>': carts ~= Cart(x,y,Dir.E); break;
                case 'v': carts ~= Cart(x,y,Dir.S); break;
                case '<': carts ~= Cart(x,y,Dir.W); break;
                default: break;
            }
        }
    }
    return State(carts, lines.front.length, lines.length, cells);
}

//============================================================================
// Puzzle 2
//============================================================================
auto lastCart(in State input){
    auto cur = State(input.carts.dup, input.width, input.height, input.cells);
    Cart[] crashed;
    while(cur.carts.length != 1){
        crashed = cur.step;
        if(!crashed.empty)
            foreach(cart; crashed)
                cur.carts = cur.carts.remove!(c => c==cart);
    }
    return tuple(cur.carts.front.x, cur.carts.front.y);
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto input = `/->-\        
|   |  /----\
| /-+--+-\  |
| | |  | v  |
\-+-/  \-+--/
  \------/   `.parse;
    expect(tuple(7,3), input.firstCrash);
}

unittest{
    auto input = `/>-<\  
|   |  
| /<+-\
| | | v
\>+</ |
  |   ^
  \<->/`.parse;
  expect(tuple(6,4), input.lastCart);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}