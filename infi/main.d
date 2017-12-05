import std.stdio: writeln, writefln;
import std.file: readText;
import std.algorithm: startsWith, find, count;
import std.format: format, formattedRead;
import std.array: array;

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

    string toString() const {
        return format("(%d, %d)", x, y);
    }
}

uint collisionCount(string input){
    Vec2D[] bots;
    // Parse bot positions
    while(input.startsWith("[")){
        Vec2D pos;
        input.formattedRead!"[%d,%d]"(pos.x, pos.y);
        bots ~= pos;
    }
    
    uint numCollisions;    
    uint curBot;
    // Process moves
    for(; input.length>0; curBot = (curBot+1)%bots.length){
        // Parse next move
        Vec2D move;
        const numVars = input.formattedRead!"(%d,%d)"(move.x, move.y);
        assert(numVars == 2);
        
        // Move bot
        const succ = bots[curBot] + move;
        const collision = bots.count(succ) == bots.length-1;
        //writefln("Robot %s moves with %s\tfrom %s\tto %s\t(%s)", curBot+1, move, bots[curBot], succ, !collision);
        bots[curBot] = succ;
        if(collision)
            ++numCollisions;
    }

    return numCollisions;
}