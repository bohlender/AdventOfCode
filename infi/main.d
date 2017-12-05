import std.stdio: writeln, writefln;
import std.file: readText, write;
import std.algorithm: startsWith, find, count, reduce, min, max;
import std.format: format, formattedRead;
import std.array: array, appender;
import std.container: redBlackTree;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = readText(args[1]);
        auto res = collisionCount(input);
        writeln(res);
    }
}

unittest{
    assert(collisionCount("[0,0][1,1](1,0)(0,-1)(0,1)(-1,0)(-1,0)(0,1)(0,-1)(1,0)") == 2);
}

struct Vec2D{
    int x;
    int y;

    this(int x, int y){
        this.x = x;
        this.y = y;
    }

    Vec2D opBinary(string op)(Vec2D rhs) if(op == "+") {
        return Vec2D(this.x + rhs.x, this.y + rhs.y);
    }

    int opCmp(Vec2D other) const {
        return (x == other.x) ? other.y-y : other.x-x;
    }

    string toString() const {
        return format("(%d, %d)", x, y);
    }
}

static uint collisionCount(string input){
    Vec2D[] bots;
    // Parse bot positions
    while(input.startsWith("[")){
        Vec2D pos;
        input.formattedRead!"[%d,%d]"(pos.x, pos.y);
        bots ~= pos;
    }

    // Track visited locations
    int maxX, maxY;
    auto visited = redBlackTree!Vec2D;
    visited.insert(bots);

    uint numCollisions;
    // Process moves
    while(input.length>0){
        // Bots move simultaneously 
        foreach(ref bot; bots){
            // Parse next move
            Vec2D move;
            const numVars = input.formattedRead!"(%d,%d)"(move.x, move.y);
            assert(numVars == 2);

            // Move bot
            bot = bot + move;

            // Track location
            maxX = max(maxX, bot.x);
            maxY = max(maxY, bot.y);
        }
        
        // Only count collision if all bots are at the same location
        const collision = bots.count(bots[0]) == bots.length;
        if(collision){
            ++numCollisions;
            visited.insert(bots[0]);
        }
    }

    // Print results
    auto strBldr = appender!string;
    strBldr.put("P1\n"); // black & white
    strBldr.put("%d %d\n".format(maxX, maxY));
    for(auto y=0; y<=maxY; ++y)
        for(auto x=0; x<maxX; ++x)
            strBldr.put("%d ".format(Vec2D(x,y) in visited));
    "hidden_message.pbm".write(strBldr.data);

    return numCollisions;
}