import std.file: readText;
import std.conv;
import std.stdio;
import std.format;
import std.string;
import std.algorithm;
import std.range;
import std.typecons;
import std.regex;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        auto input = args[1].readText().parse();
        auto res1 = input.sol1();
        auto res2 = input.sol2();
        writefln!"First: %s\nSecond: %s"(res1, res2);
    }
}

auto parse(in string s) {
    return s.split("\n\n").map!(passStr =>
        passStr.split()
        .map!(kvStr => kvStr.split(':'))
        .map!(kv => tuple(kv[0], kv[1]))
        .assocArray
    );
}

//==============================================================================
// Puzzle 1
//==============================================================================
alias Pass = string[string];

auto fieldsPresent(Pass pass) {
    auto required = ["byr", "iyr", "eyr", "hgt", "hcl", "ecl", "pid"];
    return required.all!(field => field in pass);
}

auto sol1(Range)(Range input) {
    return input.count!fieldsPresent;
}

//==============================================================================
// Puzzle 2
//==============================================================================

auto fieldsValid(Pass pass) {
    if(!pass.fieldsPresent()) return false;

    auto byr = pass["byr"].to!int;
    if(byr < 1920 || 2002 < byr) return false;

    auto iyr = pass["iyr"].to!int;
    if(iyr < 2010 || 2020 < iyr) return false;

    auto eyr = pass["eyr"].to!int;
    if(eyr < 2020 || 2030 < eyr) return false;

    int hgt; string unit;
    pass["hgt"].dup.formattedRead!"%d%s"(hgt, unit);
    if(!["cm", "in"].canFind(unit)) return false;
    if(unit == "cm" && (hgt < 150 || 193 < hgt)) return false;
    if(unit == "in" && (hgt < 59 || 76 < hgt)) return false;

    if(!pass["hcl"].matchFirst(`^#[0-9a-f]{6}$`)) return false;

    if(!["amb", "blu", "brn", "gry", "grn", "hzl", "oth"].canFind(pass["ecl"])) return false;

    if(!pass["pid"].matchFirst(`^\d{9}$`)) return false;

    return true;
}

auto sol2(Range)(Range input) {
    return input.count!fieldsValid;
}

//==============================================================================
// Unittests
//==============================================================================
unittest {
    auto content =
`ecl:gry pid:860033327 eyr:2020 hcl:#fffffd
byr:1937 iyr:2017 cid:147 hgt:183cm

iyr:2013 ecl:amb cid:350 eyr:2023 pid:028048884
hcl:#cfa07d byr:1929

hcl:#ae17e1 iyr:2013
eyr:2024
ecl:brn pid:760753108 byr:1931
hgt:179cm

hcl:#cfa07d eyr:2025 pid:166559648
iyr:2011 ecl:brn hgt:59in`;
    auto input = content.parse();
    expect(2, input.sol1());
}

unittest {
    auto content =
`eyr:1972 cid:100
hcl:#18171d ecl:amb hgt:170 pid:186cm iyr:2018 byr:1926

iyr:2019
hcl:#602927 eyr:1967 hgt:170cm
ecl:grn pid:012533040 byr:1946

hcl:dab227 iyr:2012
ecl:brn hgt:182cm pid:021572410 eyr:2020 byr:1992 cid:277

hgt:59cm ecl:zzz
eyr:2038 hcl:74454a iyr:2023
pid:3556412378 byr:2007`;
    auto input = content.parse;
    assert(! input.all!fieldsValid);
}

unittest {
    auto content =
`pid:087499704 hgt:74in ecl:grn iyr:2012 eyr:2030 byr:1980
hcl:#623a2f

eyr:2029 ecl:blu cid:129 byr:1989
iyr:2014 pid:896056539 hcl:#a97842 hgt:165cm

hcl:#888785
hgt:164cm byr:2001 iyr:2015 cid:88
pid:545766238 ecl:hzl
eyr:2022

iyr:2010 hgt:158cm hcl:#b6652a ecl:blu byr:1944 eyr:2021 pid:093154719`;
    auto input = content.parse;
    assert(input.all!fieldsValid);
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format!"Expected %s but got %s"(expected, actual), file, line);
}