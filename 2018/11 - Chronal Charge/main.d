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
        auto input = args[1].readText.strip.to!int;
        auto res1 = input.bestCell;
        auto res2 = input.bestCellAndSize;
        writefln("First: %d,%d\nSecond: %d,%d,%d", res1.x, res1.y, res2.x, res2.y, res2.size);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.typecons;

alias Cell = Tuple!(int, "x", int, "y", int, "totalPower", int, "size");

auto powerLevel(in int serial, in int x, in int y) {
    auto rackId = x+10;
    auto powerLevel = (rackId*y + serial)*rackId;
    return powerLevel%1000/100 - 5;
}

auto totalPower(in int serial, in int x, in int y, in int size=3){
    int sum;
    foreach(curY; y..y+size){
        foreach(curX; x..x+size){
            sum += serial.powerLevel(curX, curY);
        }
    }
    return sum;
}

auto bestCell(in int serial, in int size=3){
    auto best = Cell(0,0,0,size);
    foreach(y; 0..300-size){
        foreach(x; 0..300-size){
            auto power = serial.totalPower(x+1, y+1, size);
            if(power > best.totalPower){
                best.totalPower = power;
                best.x = x+1;
                best.y = y+1;
            }
        }
    }
    return best;
}

//============================================================================
// Puzzle 2
//============================================================================
import std.typecons;

auto bestCellAndSize(in int serial){
    Cell best;
    foreach(size; 0..20){
        auto cell = serial.bestCell(size);
        if(cell.totalPower > best.totalPower){
            best = cell;
        }
    }
    return best;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    expect(4, 8.powerLevel(3, 5));
    expect(-5, 57.powerLevel(122, 79));
    expect(0, 39.powerLevel(217, 196));
    expect(4, 71.powerLevel(101, 153));

    expect(29, 18.totalPower(33,45));
    expect(30, 42.totalPower(21,61));

    expect(Cell(33,45,29,3), 18.bestCell);

    expect(Cell(90,269,113,16), 18.bestCellAndSize);
    expect(Cell(232,251,119,12), 42.bestCellAndSize);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}