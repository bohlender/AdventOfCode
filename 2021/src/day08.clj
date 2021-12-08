(ns day08
  (:require [clojure.string :as string])
  (:require [clojure.math.numeric-tower :as math])
  (:use clojure.set)
  (:use clojure.test))

(defn parse-patterns [s]
  (->> s
       (re-seq #"\w+")
       (map set)))

(defn parse-line [s]
  (let [[digits-str value-str] (string/split s #"\|")
        digit-patterns (parse-patterns digits-str)
        value-patterns (parse-patterns value-str)]
    {:digit-patterns digit-patterns :value-patterns value-patterns}))

(defn parse [s]
  (->> s
       string/split-lines
       (map parse-line)))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn unique-segment-count? [pattern]
  (->> pattern
       count
       (contains? #{2 4 3 7}))) ; #segments in patterns of digits 1 4 7 8

(defn sum-unique-patterns [patterns]
  (->> patterns
       (filter unique-segment-count?)
       count))

(defn sol1 [input]
  (->> input
       (map :value-patterns)
       (map sum-unique-patterns)
       (reduce +)))

; ===================
; Part 2
; ===================
(defn find-first [pred coll]
  (first (filter pred coll)))

(defn sort-patterns [patterns]
  (let [pat-1 (find-first #(= 2 (count %)) patterns)
        pat-7 (find-first #(= 3 (count %)) patterns)
        pat-4 (find-first #(= 4 (count %)) patterns)
        pat-8 (find-first #(= 7 (count %)) patterns)
        pat-9 (find-first #(and (= 6 (count %)) (subset? pat-4 %)) patterns)
        pat-0 (find-first #(and (= 6 (count %)) (subset? pat-1 %) (not= pat-9 %)) patterns)
        pat-6 (find-first #(and (= 6 (count %)) (not= pat-9 %) (not= pat-0 %)) patterns)
        pat-3 (find-first #(and (= 5 (count %)) (subset? pat-1 %)) patterns)
        pat-5 (find-first #(and (= 5 (count %)) (subset? (difference pat-4 pat-1) %)) patterns)
        pat-2 (find-first #(and (= 5 (count %)) (not= pat-3 %) (not= pat-5 %)) patterns)]
    [pat-0 pat-1 pat-2 pat-3 pat-4 pat-5 pat-6 pat-7 pat-8 pat-9]))

(defn calc-value [digit-patterns value-patterns]
  (let [digits (map #(.indexOf digit-patterns %) value-patterns)]
    (reduce + (map * digits [1000 100 10 1]))))

(defn decode [entry]
  (let [digit-patterns (sort-patterns (:digit-patterns entry))]
    (calc-value digit-patterns (:value-patterns entry))))

(defn sol2 [input]
  (->> input
       (map decode)
       (reduce +)))

; ===================
; Tests
; ===================
(def example-input "be cfbegad cbdgef fgaecd cgeb fdcge agebfd fecdb fabcd edb | fdgacbe cefdb cefbgd gcbe\nedbfga begcd cbg gc gcadebf fbgde acbgfd abcde gfcbed gfec | fcgedb cgb dgebacf gc\nfgaebd cg bdaec gdafb agbcfd gdcbef bgcad gfac gcb cdgabef | cg cg fdcagb cbg\nfbegcd cbd adcefb dageb afcb bc aefdc ecdab fgdeca fcdbega | efabcd cedba gadfec cb\naecbfdg fbg gf bafeg dbefa fcge gcbea fcaegb dgceab fcbdga | gecf egdcabf bgf bfgea\nfgeab ca afcebg bdacfeg cfaedg gcfdb baec bfadeg bafgc acf | gebdcfa ecba ca fadegcb\ndbcfg fgd bdegcaf fgec aegbdf ecdfab fbedc dacgb gdcebf gf | cefg dcbef fcge gbcadfe\nbdfegc cbegaf gecbf dfcage bdacg ed bedf ced adcbefg gebcd | ed bcgafe cdgba cbgef\negadfb cdbfeg cegd fecab cgb gbdefca cg fgcdab egfdb bfceg | gbdfcae bgc cg cgb\ngcafb gcf dcaebfg ecagb gf abcdeg gaef cafbge fdbac fegbdc | fgae cfgab fg bagce")

(deftest part1-examples
  (is (= 26 (sol1 (parse example-input)))))

(deftest part2-examples
  (is (= 5353 (decode (parse-line "acedgfb cdfbe gcdfa fbcad dab cefabd cdfgeb eafb cagedb ab |
cdfeb fcadb cdfeb cdbaf"))))
  (is (= 61229 (sol2 (parse example-input)))))

; ===================
; Main
; ===================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (parse-file filename)]
      (println "First:" (sol1 input))
      (println "Second:" (sol2 input)))))
