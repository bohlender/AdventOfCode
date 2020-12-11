import std.file: readText;
import std.stdio;
import std.string;
import std.algorithm;
import std.range;
import std.conv;

void main(string[] args) {
    if(args.length != 2) {
        writeln("Invalid number of parameters. Expecting one input file.");
    } else {
        auto input = args[1].readText.parse;
        auto res1 = input.sol1;
        auto res2 = input.sol2;
        writefln!"First: %s\nSecond: %s"(res1, res2);
    }
}

auto parse(in string s) {
    auto lines = s.splitLines;
    auto tiles = lines.joiner.map!charToTile;
    return Board(tiles.array, lines.front.length, lines.length);
}

//==============================================================================
// Puzzle 1
//==============================================================================
enum Tile {floor, empty, occupied}

auto charToTile(Char)(in Char c) {
    final switch(c) {
        case '.': return Tile.floor;
        case 'L': return Tile.empty;
        case '#': return Tile.occupied;
    }
}

struct Vec2 {
    ulong x, y;
    auto opBinary(string op)(const Vec2 rhs) const if(op=="+") {
        return Vec2(x + rhs.x, y + rhs.y);
    }
}

struct Board {
    Tile[] tiles;
    size_t width, height;

    auto at(Vec2 v) const { return tiles[idx(v)]; }
    auto idx(Vec2 v) const { return v.y*width + v.x; }

    auto neighbours(Vec2 v, ulong dist = 1) const {
        Tile[] res;

        void addTileInDir(Vec2 dv) {
            for(auto cur=v+dv, i=1; 0<=cur.x && cur.x<width && 0<=cur.y && cur.y<height; cur = cur + dv, i++) {
                if(at(cur) != Tile.floor && i<=dist) {
                    res ~= at(cur);
                    break;
                }
            }
        }

        [Vec2(-1,0), Vec2(-1, -1), Vec2(0, -1), Vec2(1, -1), Vec2(1, 0), Vec2(1, 1), Vec2(0,1), Vec2(-1,1)].each!addTileInDir;
        return res;
    }

    auto next(bool part1 = true) const {
        auto nextTiles = tiles.dup;
        foreach(y; iota(height)) {
            foreach(x; iota(width)) {
                auto v = Vec2(x,y);
                if(part1) {
                    if(at(v) == Tile.empty && neighbours(v).all!(t => t!=Tile.occupied))
                        nextTiles[idx(v)] = Tile.occupied;
                    else if(at(v) == Tile.occupied && neighbours(v).count!(t => t==Tile.occupied) >= 4)
                        nextTiles[idx(v)] = Tile.empty;
                } else {
                    if(at(v) == Tile.empty && neighbours(v, -1).all!(t => t!=Tile.occupied))
                        nextTiles[idx(v)] = Tile.occupied;
                    else if(at(v) == Tile.occupied && neighbours(v, -1).count!(t => t==Tile.occupied) >= 5)
                        nextTiles[idx(v)] = Tile.empty;
                }
            }
        }
        return Board(nextTiles, width, height);
    }
}

auto sol1(Board input) {
    auto prev = input; 
    auto cur = prev.next();
    while(prev != cur) {
        prev = cur;
        cur = prev.next();
    }
    return cur.tiles.count!(t => t==Tile.occupied);
}

//==============================================================================
// Puzzle 2
//==============================================================================
auto sol2(Board input) {
    auto prev = input; 
    auto cur = prev.next(false);
    while(prev != cur) {
        prev = cur;
        cur = prev.next(false);
    }
    return cur.tiles.count!(t => t==Tile.occupied);

}

//==============================================================================
// Unittests
//==============================================================================
unittest {
    auto content =
`L.LL.LL.LL
LLLLLLL.LL
L.L.L..L..
LLLL.LL.LL
L.LL.LL.LL
L.LLLLL.LL
..L.L.....
LLLLLLLLLL
L.LLLLLL.L
L.LLLLL.LL`;
    auto input = content.parse;
    expect(37, input.sol1);
    expect(26, input.sol2);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format!"Expected %s but got %s"(expected, actual), file, line);
}