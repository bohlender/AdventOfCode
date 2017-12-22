import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = input.pixelsAfter(5);
        auto res2 = input.pixelsAfter(18);
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
// TODO: Use mir
import std.string: strip, splitLines, split;
import std.algorithm: count, map, sum, reverse, swap;
import std.range: retro;
import std.array: array, appender;
import std.math: sqrt;
import std.conv: to;

auto startPattern = Pattern([[0,1,0], [0,0,1], [1,1,1]]);

struct Pattern{
    this(in size_t size){
        this.size = size;
        matrix = new bool[][](size, size);
    }
    this(in Pattern pat){this(pat.matrix);}
    this(in bool[][] matrix){
        this.size = matrix.length;
        this.matrix = matrix.map!(row => row.dup).array;
    }
    this(in Pattern[] patterns){
        auto patInLine = sqrt(patterns.length.to!real).to!size_t;
        auto oldSize = patterns[0].size;
        this.size = oldSize * patInLine;
        matrix = new bool[][](size, size);
        foreach(patY; 0..patInLine)
            foreach(patX; 0..patInLine)
                foreach(i, row; patterns[patY*patInLine + patX].matrix)
                    matrix[patY*oldSize+i][patX*oldSize..(patX+1)*oldSize] = row;
    }
    bool isSet(size_t x, size_t y) const {return matrix[y][x];}
    void setRow(size_t y, bool[] row){
        assert(row.length == size);
        matrix[y] = row;
    }
    string toString() const {
        auto strBldr = appender!string;
        foreach(y; 0..size){
            foreach(x; 0..size){
                auto c = isSet(x,y) ? '#' : '.';
                strBldr.put(c);
            }
            strBldr.put('\n');
        }
        return strBldr.data;
    }
    Pattern flip() const{
        auto res = Pattern(matrix);
        foreach(ref row; res.matrix)
            reverse(row);
        return res;
    }
    Pattern rotate(size_t times=1) const{
        auto res = Pattern(matrix);
        foreach(_; 0..times){
            foreach(ring; 0..size/2){
                //writefln!"ring: %d"(ring);
                const ringSize = size-ring*2;
                foreach(i; 1..ringSize){
                    //writefln!"(%d,%d) <-ul-> (%d,%d)"(i,ring,ring,size-1-i);
                    res.matrix[ring][i].swap(res.matrix[size-1-i][ring]); // up <-> left
                    //writefln!"(%d,%d) <-ld-> (%d,%d)"(ring,size-1-i,size-1-i,size-1-ring);
                    res.matrix[size-1-i][ring].swap(res.matrix[size-1-ring][size-1-i]); // left <-> down
                    //writefln!"(%d,%d) <-dr-> (%d,%d)\n"(size-1-i,size-1-ring,size-1-ring,i);
                    res.matrix[size-1-ring][size-1-i].swap(res.matrix[i][size-1-ring]); // down <-> right
                }
            }
        }
        return res;
    }
    Pattern[] split() {
        auto patInLine = size%2 == 0 ? size/2 : size/3;
        auto newSize = size%2 == 0 ? 2 : 3;
        Pattern[] subPatterns;
        foreach(patY; 0..patInLine){
            foreach(patX; 0..patInLine){
                auto pat = Pattern(newSize);
                foreach(y; 0..newSize)
                    pat.setRow(y, matrix[patY*newSize+y][patX*newSize..(patX+1)*newSize]);
                subPatterns ~= pat;
            }
        }
        return subPatterns;
    }
private:
    size_t size;
    bool[][] matrix;
}

Pattern[Pattern] parse(in string s){
    Pattern[Pattern] rules;
    foreach(line; s.strip.splitLines){
        auto parts = line.split("=>");
        // Parse src pattern
        auto srcSize = parts[0].count('/') + 1;
        auto srcPattern = Pattern(srcSize);
        foreach(i, rowStr; parts[0].strip.split('/')){
            auto row = rowStr.map!(c => c == '#').array;
            srcPattern.setRow(i, row);
        }
        // Parse dst pattern
        auto dstSize = srcSize + 1;
        auto dstPattern = Pattern(dstSize);
        foreach(i, rowStr; parts[1].strip.split('/')){
            auto row = rowStr.map!(c => c == '#').array;
            dstPattern.setRow(i, row);
        }

        // Add each variant of the pattern
        auto rot = (Pattern p) => (p.rotate);
        auto flip = (Pattern p) => (p.flip);
        auto funcs = [rot, rot, rot, rot, flip, rot, rot, rot];
        foreach(f; funcs){
            srcPattern = f(srcPattern);
            rules[Pattern(srcPattern)] = dstPattern;
        }
    }
    return rules;
}

auto step(Pattern pat, Pattern[Pattern] rules){
    auto splitRes = pat.split;
    auto ruleRes = splitRes.map!(p => rules[p]).array;
    return Pattern(ruleRes);
}

auto pixelsAfter(Pattern[Pattern] rules, in uint numSteps){
    auto curPat = startPattern;
    foreach(_; 0..numSteps)
        curPat = curPat.step(rules);
    return curPat.matrix.map!(row => row.count(true)).sum;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto same = Pattern([[0,1,0], [0,0,1], [1,1,1]]);
    expect(same, startPattern);

    auto flipped = Pattern([[0,1,0], [1,0,0], [1,1,1]]);
    expect(flipped, startPattern.flip);

    auto rotR = Pattern([[1,0,0], [1,0,1], [1,1,0]]);
    expect(rotR, startPattern.rotate);

    auto rotR2 = Pattern([[1,1,1], [1,0,0], [0,1,0]]);
    expect(rotR2, startPattern.rotate(2));
}

unittest{
    auto contents = r"../.# => ##./#../...
.#./..#/### => #..#/..../..../#..#";
    auto input = contents.parse;

    auto ruleRes = input[startPattern];
    auto splitRes = ruleRes.split;
    expect(ruleRes, Pattern(splitRes));

    auto ulPattern = Pattern([[1,0], [0,0]]);
    expect(ulPattern, splitRes[0]);

    expect(12, input.pixelsAfter(2));
}