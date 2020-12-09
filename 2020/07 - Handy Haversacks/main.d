import std.file: readText;
import std.stdio;
import std.string;
import std.algorithm;
import std.range;
import std.typecons;
import std.conv;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = args[1].readText.parse;
        auto res1 = input.sol1;
        auto res2 = input.sol2();
        writefln!"First: %s\nSecond: %s"(res1, res2);
    }
}

auto parse(in string s) {
    Rules rules;
    foreach(line; s.lineSplitter()) {
        auto parts = line.split(" bags contain ");
        BagSpec[] bags;
        if(!parts[1].startsWith("no")) {
            bags = parts[1].split(", ")
                .map!(bagStr => bagStr.split())
                .map!(p => BagSpec(format!"%s %s"(p[1], p[2]), p[0].to!uint))
                .array;
        }
        rules[parts[0]] = bags;
    }
    return rules;
}

//==============================================================================
// Puzzle 1
//==============================================================================
alias BagSpec = Tuple!(string, "type", uint, "num");
alias Rules = BagSpec[][string];

bool canContainShiny(string type, Rules rules) {
    return rules[type].any!(bagSpec =>
        bagSpec.type == "shiny gold" || bagSpec.type.canContainShiny(rules)
    );
}

auto sol1(Range)(Range input) {
    return input.byKey.count!canContainShiny(input);
}

//==============================================================================
// Puzzle 2
//==============================================================================
uint countSubbags(string type, Rules rules) {
    return rules[type].map!(bagSpec =>
        bagSpec.num * (bagSpec.type.countSubbags(rules) + 1)
    ).sum;
}

auto sol2(Range)(Range input) {
    return "shiny gold".countSubbags(input);
}

//==============================================================================
// Unittests
//==============================================================================
unittest {
    auto content =
`light red bags contain 1 bright white bag, 2 muted yellow bags.
dark orange bags contain 3 bright white bags, 4 muted yellow bags.
bright white bags contain 1 shiny gold bag.
muted yellow bags contain 2 shiny gold bags, 9 faded blue bags.
shiny gold bags contain 1 dark olive bag, 2 vibrant plum bags.
dark olive bags contain 3 faded blue bags, 4 dotted black bags.
vibrant plum bags contain 5 faded blue bags, 6 dotted black bags.
faded blue bags contain no other bags.
dotted black bags contain no other bags.`;
    auto input = content.parse;
    expect(4, input.sol1);
    expect(32, input.sol2);
}

unittest {
    auto content =
`shiny gold bags contain 2 dark red bags.
dark red bags contain 2 dark orange bags.
dark orange bags contain 2 dark yellow bags.
dark yellow bags contain 2 dark green bags.
dark green bags contain 2 dark blue bags.
dark blue bags contain 2 dark violet bags.
dark violet bags contain no other bags.`;
    auto input = content.parse;
    expect(126, input.sol2);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format!"Expected %s but got %s"(expected, actual), file, line);
}