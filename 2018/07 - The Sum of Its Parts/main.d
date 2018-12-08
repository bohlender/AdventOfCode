import std.file;
import std.stdio;
import std.format;
import std.string;
import std.algorithm;
import std.range;

void main(string[] args) {
    if(args.length != 2){
        writeln("Invalid number of parameters. Expecting one input file.");
    }else{
        const input = readText(args[1]).parse;
        auto res1 = input.process;
        auto res2 = input.processTime(60, 5);
        writefln("First: %s\nSecond: %s", res1, res2);
    }
}

//============================================================================
// Puzzle 1
//============================================================================
import std.conv;
import std.container;

struct Order(T){
    const T[][T] succs;

    auto minima() const {
        auto allSuccs = succs.values.dup.joiner;
        return elements.filter!(e => !allSuccs.canFind(e)).array;
    }

    @property elements() const {return succs.keys;}

    // Note: Does not consider lexicographic ordering
    auto less(in T lhs, in T rhs) const {
        auto ss = succs[lhs];
        if (ss.canFind(rhs))
            return true;
        return ss.map!(s => less(s, rhs)).canFind(true);
    }

    auto lessInclLex(in T lhs, in T rhs) const {
        if(less(lhs,rhs))
            return true;
        if(less(rhs,lhs))
            return false;
        return lhs<rhs;
    }

    auto invert() const{
        T[][T] preds;
        foreach(src, dsts; succs){
            if(src !in preds)
                preds[src] = [];
            foreach(dst; dsts)
                preds[dst] ~= src;
        }
        return preds;
    }
}

auto process(in Order!dchar input){
    dchar[] visited;
    auto worklist = redBlackTree!((a,b) => input.lessInclLex(a,b))(input.minima);

    while(!worklist.empty){
        auto cur = worklist.front;
        worklist.removeFront;
        visited ~= cur;

        worklist.insert(input.succs[cur]);
    }
    return visited.to!string;
}

// Note: Using dchar instead of char since range functionality autodecodes :/
auto parse(in string s){
    dchar[][dchar] succs;
    foreach(line; s.splitLines){
        auto parts = line.split;
        auto src = parts[1].to!dchar;
        auto dst = parts[7].to!dchar;
        succs[src] ~= dst;
        if(dst !in succs)
            succs[dst] = []; // init map for elements w/o succs too
    }
    return Order!dchar(succs);
}
//============================================================================
// Puzzle 2
//============================================================================
struct Job{
    dchar name;
    int remaining;
}

auto processTime(in Order!dchar input, in int stepTime, in int numWorkers){
    auto preds = input.invert;
    int time;
    dchar[] jobsDone;

    Job[] jobs;
    auto worklist = input.minima;
    while(!worklist.empty || !jobs.empty){
        // Assign new jobs (whose requirements are satisfied)
        while(jobs.length < numWorkers && !worklist.empty){
            auto curSat = worklist.sort!((a,b) => input.lessInclLex(a,b))
                                  .find!(w => preds[w].all!(req => jobsDone.canFind(req)));
            if (!curSat.empty){
                auto cur = curSat.front;
                jobs ~= Job(cur, stepTime+(cur-'A')+1);
                worklist = worklist.remove!(w => w==cur);
            }else break;
        }

        // Process scheduled jobs
        auto curStepTime = jobs.map!(j => j.remaining).minElement;
        time += curStepTime;

        Job[] toRemove;
        foreach(i, ref job; jobs){
            job.remaining -= curStepTime;
            if(job.remaining <= 0){
                jobsDone ~= job.name;
                toRemove ~= job;
                foreach(name; input.succs[job.name])
                    if(!jobsDone.canFind(name) && !worklist.canFind(name))
                        worklist ~= name;
            }
        }
        // Remove finished jobs
        foreach(remJ; toRemove)
            jobs = jobs.remove!(j => j==remJ);
    }
    return time;
}

//============================================================================
// Unittests
//============================================================================
unittest{
    const input = `Step C must be finished before step A can begin.
Step C must be finished before step F can begin.
Step A must be finished before step B can begin.
Step A must be finished before step D can begin.
Step B must be finished before step E can begin.
Step D must be finished before step E can begin.
Step F must be finished before step E can begin.`.parse;
    expect(true, input.less('C', 'A'));
    expect(true, input.less('C', 'E'));
    expect("CABDFE", input.process);
    expect(15, input.processTime(0,2));
}

static void expect(T1, T2)(T1 expected, T2 actual, in string file = __FILE__, in size_t line = __LINE__) if(is(typeof(expected == actual) == bool)) {
    import std.format: format;
    import core.exception: AssertError;

    if(!(expected == actual))
        throw new AssertError(format("Expected %s but got %s", expected, actual), file, line);
}