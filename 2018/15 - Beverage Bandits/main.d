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
        auto res1 = input.outcome;
        auto res2 = input.outcome2; // 34*781
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.container;
import std.typecons;

struct Vec2{
    long x, y;
    long opCmp(Vec2 rhs) const {return (y==rhs.y) ? x-rhs.x : y-rhs.y;}
}
struct Unit{
    Vec2 pos;
    int hp;
    int atk;
    bool isGoblin;
    long opCmp(Unit rhs) const {return pos.opCmp(rhs.pos);}
}

struct State{
    const ulong width, height;
    const bool[] _walls;
    Unit[] units;

    @property walls() const {return _walls.chunks(width);}
    @property empty(in Vec2 pos) const{return !walls[pos.y][pos.x] && !units.canFind!(u => u.pos == pos);}
    @property elfCount() const {return units.count!(u => !u.isGoblin);}

    auto neighbours(in Vec2 pos) const {
        Vec2[] res;
        if(pos.y>0) res ~= Vec2(pos.x, pos.y-1); // N
        if(pos.x>0) res ~= Vec2(pos.x-1, pos.y); // W
        if(pos.y<height-1) res ~= Vec2(pos.x, pos.y+1); // S
        if(pos.x<width-1) res ~= Vec2(pos.x+1, pos.y); // E
        return res;
    }

    auto findMoveDst(in Unit unit){
        auto enemies = units.filter!(u => u.isGoblin != unit.isGoblin);
        auto goals = enemies.map!(u => neighbours(u.pos)).joiner.filter!(pos => empty(pos) || pos==unit.pos).redBlackTree;
        if(unit.pos in goals) // Already at goal
            return Nullable!Vec2();

        alias Dist = Tuple!(Vec2, "next", Vec2, "goal", int, "val");
        Dist[Vec2] dists;
        foreach(goal; goals)
            dists[goal] = Dist(goal, goal, 0);

        auto worklist = goals.array;
        while(!worklist.empty){
            auto cur = worklist.front;
            worklist.popFront;

            auto emptyNeighbours = neighbours(cur).filter!(pos => empty(pos) || pos==unit.pos);
            foreach(predPos; emptyNeighbours){
                auto predDist = dists[cur].val + 1;
                // Update distance from predPos if one of the following holds:
                // - no distance has been assigned to predPos yet
                // - the new distance value is better than the previous one
                // - the new distance value equals the currently stored one, but prefer the path to the goal in reading order (if the goals differ), otherwise prioritise by reading order of move destinations
                if(predPos !in dists || dists[predPos].val > predDist
                || (dists[predPos].val == predDist && (dists[cur].goal == dists[predPos].goal ? cur < dists[predPos].next : dists[cur].goal < dists[predPos].goal))){
                    dists[predPos] = Dist(cur, dists[cur].goal, predDist);
                    if(!worklist.canFind(predPos))
                        worklist ~= predPos;
                }
            }
        }

        auto pDist = unit.pos in dists;
        if(pDist) // Way to goal exists
            return (*pDist).next.nullable;
        return Nullable!Vec2();
    }

    auto step(){
        units.sort;
        // Deleting while iterating does not really work with the stdlib so we need this mess instead of foreach
        for(auto i=0; i<units.length; ++i){
            // Try moving to target
            auto dstPos = findMoveDst(units[i]);
            if(!dstPos.isNull)
                units[i].pos = dstPos;

            // Try attacking a target
            auto enemies = units.filter!(u => u.isGoblin != units[i].isGoblin);
            if(enemies.empty)
                return false;
            auto adjacent = neighbours(units[i].pos);
            auto enemiesInReach = enemies.filter!(u => adjacent.canFind(u.pos));
            if(!enemiesInReach.empty){
                auto enemyIdx = enemiesInReach.minIndex!((a,b) => a.hp==b.hp ? a<b : a.hp<b.hp);
                auto enemy = enemiesInReach.dropExactly(enemyIdx).front;
                auto unitIdx = units.countUntil(enemy);
                units[unitIdx].hp -= units[i].atk;
                if(units[unitIdx].hp <= 0){
                    units = units.remove(unitIdx);
                    if(unitIdx<i)
                        i--;
                }
            }
        }
        return true;
    }
}

auto outcome(in State input){
    auto s = State(input.width, input.height, input._walls, input.units.dup);
    int i;
    for(i=0; s.step; ++i){}
    return i*s.units.map!(u => u.hp).sum;
}

auto parse(in string s){
    auto lines = s.strip.splitLines;
    bool[] walls;
    Unit[] units;
    foreach(int y, line; lines){
        foreach(int x, c; line){
            assert(c!=' ');
            walls ~= (c=='#') ? true : false;
            if(c.among('G','E'))
                units ~= Unit(Vec2(x,y), 200, 3, c=='G');
        }
    }
    return State(lines[0].length, lines.length, walls, units);
}

//============================================================================
// Puzzle 2
//============================================================================
auto outcome2(in State input){
    int i;
    auto s = State(input.width, input.height, input._walls, input.units.dup);
    int elfAtk=3;
    do{
        // Create initial state with increased elf atk value
        s.units = input.units.dup;
        foreach(ref unit; s.units.filter!(u => !u.isGoblin))
            unit.atk = elfAtk;

        // Battle till end or elf dies
        for(i=0; s.step && input.elfCount == s.elfCount; ++i){}
        elfAtk++;
    }while(input.elfCount != s.elfCount); // increase elf atk value while deaths occur
    return i*s.units.map!(u => u.hp).sum; // once a battle goes without losses, return score
}

//============================================================================
// Unittests
//============================================================================
unittest{
    {
        auto input = `
#######
#E..G.#
#...#.#
#.G.#G#
#######`.parse;
        auto elf = input.units.find!(u => !u.isGoblin)[0];

        expect(Vec2(2,1), input.findMoveDst(elf));
        assert(input.findMoveDst(Unit(Vec2(3,1),0,0,false)).isNull);
        expect(Vec2(2,1), input.findMoveDst(Unit(Vec2(3,1),0,0,true)));
    }
    {
        auto input = `
#######
#.E..G#
#.#####
#G#####
#######`.parse;
        auto elf = input.units.find!(u => !u.isGoblin)[0];
        expect(Vec2(3,1), input.findMoveDst(elf));
    }
}

unittest{
    27730.expect(`
#######
#.G...#
#...EG#
#.#.#G#
#..G#E#
#.....#
#######`.parse.outcome);
    36334.expect(`
#######
#G..#E#
#E#E.E#
#G.##.#
#...#E#
#...E.#
#######`.parse.outcome);
    39514.expect(`
#######
#E..EG#
#.#G.E#
#E.##E#
#G..#.#
#..E#.#
#######`.parse.outcome);
    27755.expect(`
#######
#E.G#.#
#.#G..#
#G.#.G#
#G..#.#
#...E.#
#######`.parse.outcome);
    28944.expect(`
#######
#.E...#
#.#..G#
#.###.#
#E#G#G#
#...#G#
#######`.parse.outcome);
    18740.expect(`
#########
#G......#
#.E.#...#
#..##..G#
#...##..#
#...#...#
#.G...G.#
#.....G.#
#########`.parse.outcome);
    13400.expect(`
####
##E#
#GG#
####`.parse.outcome);
    13987.expect(`
#####
#GG##
#.###
#..E#
#.#G#
#.E##
#####`.parse.outcome);
}

unittest{
    4988.expect(`
#######
#.G...#
#...EG#
#.#.#G#
#..G#E#
#.....#
#######`.parse.outcome2);
    31284.expect(`
#######
#E..EG#
#.#G.E#
#E.##E#
#G..#.#
#..E#.#
#######`.parse.outcome2);
    3478.expect(`
#######
#E.G#.#
#.#G..#
#G.#.G#
#G..#.#
#...E.#
#######`.parse.outcome2);
    6474.expect(`
#######
#.E...#
#.#..G#
#.###.#
#E#G#G#
#...#G#
#######`.parse.outcome2);
    1140.expect(`
#########
#G......#
#.E.#...#
#..##..G#
#...##..#
#...#...#
#.G...G.#
#.....G.#
#########`.parse.outcome2);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}