(ns day03
  (:require [clojure.math.numeric-tower :as math])
  (:use clojure.test))

(defn parse [s]
  (clojure.string/split-lines s))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn digit-str->int-vec [s]
  "\"1102\" -> [1 1 0 2]"
  (map #(Integer/parseInt %) (clojure.string/split s #"")))

(defn bool-vec->int [v]
  "[true true false true] -> 13"
  (->> v
       reverse
       (map-indexed (fn [i b] (if b (math/expt 2 i) 0)))
       (reduce +)))

(defn add-vec [lhs rhs]
  (map + lhs rhs))

(defn sol1 [bit-strings]
  (let [bit-counts (->> bit-strings (map digit-str->int-vec) (reduce add-vec))
        string-count (count bit-strings)]
    (let [gamma-bits (map #(>= % (/ string-count 2)) bit-counts)
          gamma (bool-vec->int gamma-bits)
          epsilon-bits (map not gamma-bits)
          epsilon (bool-vec->int epsilon-bits)]
      (* gamma epsilon))))

(deftest part1-example
  (is (= 198 (sol1 (parse "00100\n11110\n10110\n10111\n10101\n01111\n00111\n11100\n10000\n11001\n00010\n01010")))))

; ===================
; Part 2
; ===================

(defn most-common-bit [bit-strings idx]
  (let [bit-count (->> bit-strings (map #(get % idx)) frequencies)]
    (if (>= (get bit-count \1) (get bit-count \0)) \1 \0)))

(defn least-common-bit [bit-strings idx]
  (let [bit-count (->> bit-strings (map #(get % idx)) frequencies)]
    (if (>= (get bit-count \1) (get bit-count \0)) \0 \1)))

(defn filter-bit-strings [bit-strings idx bit-picker-fun]
  (if (> (count bit-strings) 1)
    (let [filter-bit (bit-picker-fun bit-strings idx)
          filtered-strings (filter #(= (get % idx) filter-bit) bit-strings)]
      (recur filtered-strings (+ idx 1) bit-picker-fun))
    bit-strings))

(defn sol2 [input]
  (let [[oxy-bit-str] (filter-bit-strings input 0 most-common-bit)
        oxy-rating (Integer/parseInt oxy-bit-str 2)
        [co2-bit-str] (filter-bit-strings input 0 least-common-bit)
        co2-rating (Integer/parseInt co2-bit-str 2)]
    (* oxy-rating co2-rating)))

(deftest part2-example
  (is (= 230 (sol2 (parse "00100\n11110\n10110\n10111\n10101\n01111\n00111\n11100\n10000\n11001\n00010\n01010")))))

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

(-main "inputs/day03.txt")