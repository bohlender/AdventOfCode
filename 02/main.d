import std.stdio: writeln;
import std.string: split, splitLines;
import std.file: readText;
import std.algorithm: minElement, maxElement;
import std.conv: to;

unittest{
    assert(rowDifference([5,1,9,5]) == 8);
    assert(rowDifference([7,5,3]) == 4);
    assert(rowDifference([2,4,6,8]) == 6);

    assert(checksum("5 1 9 5\n7 5 3\n2 4 6 8") == 18);
}

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = readText(args[1]);
        auto res1 = checksum(input);
        //auto res2 = sum2(input.strip);
        //writefln("First: %s\nSecond: %s", res1, res2);
        writeln(res1);
    }
}

auto rowDifference(T)(T[] row){
    return row.maxElement - row.minElement;
}

uint checksum(in string input){
    uint res;
    foreach(line; input.splitLines){
        const nums = line.split.to!(uint[]);
        const diff = rowDifference(nums);
        res += diff;
    }

    return res;
}