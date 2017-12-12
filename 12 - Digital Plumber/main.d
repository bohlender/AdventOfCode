import std.stdio: writeln, writefln;
import std.file: readText;
import std.format: format, formattedRead;
import std.string: strip, split, splitLines;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const contents = readText(args[1]);
        auto input = contents.parse;
        auto res1 = input.progsInGroupOf(0);
        auto res2 = input.countGroups;
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
import std.container: RedBlackTree, redBlackTree;
import std.algorithm: min, each, map;
import std.range: chain;
import std.conv: to;

alias Equiv = EquivClass!uint;
class EquivClass(T){
    this(in T element){
        _elements = redBlackTree!T;
        add(element);
    }

    void add(in T element){
        _representative = _elements.empty ? element : min(_representative, element);
        _elements.insert(element);
    }
    
    T representative() const @property {return _representative;}
    bool contains(in T element) const @property {return element in _elements;}
    void mergeWith(in EquivClass!T other){ other._elements.each!(e => add(e)); }

    auto elements() const @property {return _elements;}

    override string toString() const {
        return "Representative: %s\nElements: %s (%s)".format(_representative.to!string, _elements.to!string, &_elements);
    }
protected:
    T _representative;
    RedBlackTree!T _elements;
}

static Equiv[uint] parse(in string contents){
    Equiv[uint] res;
    foreach(line; contents.splitLines){
        // Parse line
        uint src;
        uint[] dsts;
        line.formattedRead!("%d <-> %(%d, %)")(src, dsts);

        // Get equivalence class of src
        auto srcEquiv = res.get(src, new Equiv(src));
        res[src] = srcEquiv;

        // Merge equivalence classes of each dst into src class
        foreach(dst; dsts){
            auto dstEquiv = res.get(dst, new Equiv(dst));
            srcEquiv.mergeWith(dstEquiv);
            
            // Update map
            res[dst] = srcEquiv;
            foreach(e; dstEquiv.elements)
                res[e] = srcEquiv;
        }
    }
    return res;
}

static ulong progsInGroupOf(in Equiv[uint] equivs, in uint n){
    return equivs[n].elements.length;
}

//============================================================================
// Puzzle 2
//============================================================================
static ulong countGroups(in Equiv[uint] equivs){
    auto representatives = redBlackTree!uint;
    foreach(eqiv; equivs)
        representatives.insert(eqiv.representative);
    return representatives.length;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    auto contents = r"0 <-> 2
1 <-> 1
2 <-> 0, 3, 4
3 <-> 2, 4
4 <-> 2, 3, 6
5 <-> 6
6 <-> 4, 5";
    auto equivs = contents.parse;
    expect(6, equivs.progsInGroupOf(0));
}

unittest{
    auto contents = r"0 <-> 2
1 <-> 1
2 <-> 0, 3, 4
3 <-> 2, 4
4 <-> 2, 3, 6
5 <-> 6
6 <-> 4, 5";
    auto equivs = contents.parse;
    expect(2, equivs.countGroups);
}