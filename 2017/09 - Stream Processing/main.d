import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: splitLines;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto contents = readText(args[1]);
        auto input = contents.splitLines[0];
        auto res1 = score(input);
        auto res2 = garbage(input);
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}

//============================================================================
// Puzzle 1
//============================================================================
import std.string: startsWith;
import std.algorithm: find, findAmong, max;

static uint score(in string s, in uint groupNesting = 0, in bool inGarbage = false){
    if(s == [])     return 0;
    if(s[0] == '!') return score(s[2..$], groupNesting, inGarbage);

    if(inGarbage) return score(s[1..$], groupNesting, s[0] != '>');
    switch(s[0]){
        case '<': return score(s[1..$],  groupNesting, true);
        case '{': return score(s[1..$],  groupNesting+1, false);
        case '}': return groupNesting + score(s[1..$],  max(0,groupNesting-1), false);
        default: return score(s[1..$],  groupNesting, false);
    }
    return 0;
}

//============================================================================
// Puzzle 2
//============================================================================
static uint garbage(in string s, in bool inGarbage = false){
    if(s == [])     return 0;
    if(s[0] == '!') return garbage(s[2..$], inGarbage);

    if(inGarbage)
        return (s[0] != '>') + garbage(s[1..$], s[0] != '>');
    return garbage(s[1..$], s[0] == '<');
}

//============================================================================
// Unittests
//============================================================================
unittest{
    expect(1, "{}".score);
    expect(6, "{{{}}}".score);
    expect(5, "{{},{}}".score);
    expect(16, "{{{},{},{{}}}}".score);
    expect(1, "{<a>,<a>,<a>,<a>}".score);
    expect(9, "{{<ab>},{<ab>},{<ab>},{<ab>}}".score);
    expect(9, "{{<!!>},{<!!>},{<!!>},{<!!>}}".score);
    expect(3, "{{<a!>},{<a!>},{<a!>},{<ab>}}".score);
}

unittest{
    expect(0, "<>".garbage);
    expect(17, "<random characters>".garbage);
    expect(3, "<<<<>".garbage);
    expect(2, "<{!>}>".garbage);
    expect(0, "<!!>".garbage);
    expect(0, "<!!!>>".garbage);
    expect(10, `<{o"i!a,<{i<a>`.garbage);
}