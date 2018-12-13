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
        auto res1 = input.initial.step(input.rules, 20).potSum;
        auto res2 = input.potSum(50_000_000_000);
        writefln("First: %d\nSecond: %d", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.typecons;

alias State = Tuple!(string, "pots", int, "zeroIdx");
alias Input = Tuple!(State, "initial", char[string], "rules");

auto potSum(in State s){
    long sum;
    foreach(i, c; s.pots){
        if(c == '#')
            sum += (i-s.zeroIdx);
    }
    return sum;
}

auto step(in State s, in char[string] rules, in long times, in bool killUnmatched = false){
    State cur = s;
    foreach(i; 0..times)
        cur = cur.step(rules, killUnmatched);
    return cur;
}

auto step(in State s, in char[string] rules, in bool killUnmatched = false){
    auto dotsAtStart = s.pots.countUntil("#");
    auto dotsAtEnd = s.pots.retro.countUntil("#");
    auto padded = '.'.repeat(max(0,3-dotsAtStart)).array ~ s.pots ~ '.'.repeat(max(0,3-dotsAtEnd)).array;
    auto res = padded.dup;
    foreach(i, sub; padded.slide(5).enumerate){
        if(auto c = sub.to!string in rules)
            res[i+2] = *c;
        else if(killUnmatched)
            res[i+2] = '.';
    }
    auto zeroIdx = s.zeroIdx + max(0, 3-dotsAtStart);
    return State(res.to!string, zeroIdx);
}

auto parse(in string s){
    auto lines = s.splitLines;    
    Input res;
    res.initial.pots = lines[0].split[2];
    foreach(line; lines[2..$]){
        auto parts = line.split;
        res.rules[parts[0]] = parts[2].to!char;
    }
    return res;
}

//============================================================================
// Puzzle 2
//============================================================================
auto potSum(in Input input, in long gens){
    if(gens < 1000)
        return input.initial.step(input.rules, gens).potSum;
    auto s1000 = input.initial.step(input.rules, 1000);
    auto s1001 = s1000.step(input.rules);
    auto diff = s1001.potSum - s1000.potSum; // Expect fixedpoint after 1000 iterations
    return s1001.potSum + (gens-1001)*diff;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto input = `initial state: #..#.#..##......###...###

...## => #
..#.. => #
.#... => #
.#.#. => #
.#.## => #
.##.. => #
.#### => #
#.#.# => #
#.### => #
##.#. => #
##.## => #
###.. => #
###.# => #
####. => #`.parse;
    expect(State("...#....##....#####...#######....#.#..##..",5), input.initial.step(input.rules, 20, true));
    expect(325, input.initial.step(input.rules, 20, true).potSum);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}