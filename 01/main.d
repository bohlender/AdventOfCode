import std.stdio;
import std.string;
import std.file;

unittest{
    assert(sum("1122") == 3);
    assert(sum("1111") == 4);
    assert(sum("1234") == 0);
    assert(sum("91212129") == 9);
}

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters");
    }else{
        auto input = readText(args[1]);
        auto res = sum(input.strip);
        writeln(res);
    }
}

int digitToInt(in char c){
    assert(c >= '0' && c <= '9');
    return c - '0';
}

uint sum(in string input){
    uint res;
    char prev = input[$-1];
    foreach(ref cur; input) {
        if(cur == prev)
            res += digitToInt(prev);
        prev = cur;
    }

    return res;
}

