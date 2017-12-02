import std.stdio: writeln, writefln;
import std.string: split, splitLines;
import std.file: readText;
import std.algorithm: minElement, maxElement, swap;
import std.conv: to;
import std.exception: enforce;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = readText(args[1]);
        auto res1 = checksum(input);
        auto res2 = checksum2(input);
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
unittest{
    assert(rowDifference([5,1,9,5]) == 8);
    assert(rowDifference([7,5,3]) == 4);
    assert(rowDifference([2,4,6,8]) == 6);

    assert(checksum("5 1 9 5\n7 5 3\n2 4 6 8") == 18);
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

//============================================================================
// Puzzle 2
//============================================================================
unittest{
    assert(rowQuotient([5,9,2,8]) == 4);
    assert(rowQuotient([9,4,7,3]) == 3);
    assert(rowQuotient([3,8,6,5]) == 2);
    
    assert(checksum2("5 9 2 8\n9 4 7 3\n3 8 6 5") == 9);
}

auto rowQuotient(T)(in T[] row){
    for(auto i=0; i<row.length; ++i){
        for(auto j=i+1; j<row.length; ++j){
            T big = row[i];
            T small = row[j];
            if (row[i]<row[j])
                swap(big, small);
            if(big%small == 0)
                return big/small;
        }
    }
    enforce(false, "Expected a row with evenly divisable values");
    assert(false);
}

uint checksum2(in string input){
    uint res;
    foreach(line; input.splitLines){
        const nums = line.split.to!(uint[]);
        const quot = rowQuotient(nums);
        res += quot;
    }

    return res;
}