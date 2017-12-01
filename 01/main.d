import std.stdio;
import std.string;
import std.file;

unittest{
    assert(sum("1122") == 3);
    assert(sum("1111") == 4);
    assert(sum("1234") == 0);
    assert(sum("91212129") == 9);
}

unittest{
    assert(sum2("1212") == 6);
    assert(sum2("1221") == 0);
    assert(sum2("123425") == 4);
    assert(sum2("123123") == 12);
    assert(sum2("12131415") == 4);
}

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = readText(args[1]);
        auto res1 = sum(input.strip);
        auto res2 = sum2(input.strip);
        writefln("First: %s\nSecond: %s", res1, res2);
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

uint sum2(in string input){
    uint res;
    for(int i=0; i<input.length; ++i) {
        auto cur = input[i];
        auto next = input[(i+$/2) %$];
        if(cur == next)
            res += digitToInt(cur);
    }

    return res;
}