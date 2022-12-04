(ns day04
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn parse-assignment [s]
  (->> (re-seq #"\d+" s)
       (mapv #(Integer/parseInt %))))

(defn parse-assignments [line]
  (->> (string/split line #",")
       (mapv parse-assignment)))

(defn parse [s]
  (->> (string/split-lines s)
       (mapv parse-assignments)))

; ==============================================================================
; Part 1
; ==============================================================================
(defn overlap [[l1 u1] [l2 u2]]
  (let [[l u :as res] [(max l1 l2) (min u1 u2)]]
    (when (<= l u)
      res)))

(defn redundant? [lhs rhs]
  (some #{(overlap lhs rhs)} [lhs rhs]))

(defn sol1 [interval-pairs]
  (->> interval-pairs
       (map #(apply redundant? %))
       (filter some?)
       count))

(deftest part1-examples
  (let [input (parse "2-4,6-8\n2-3,4-5\n5-7,7-9\n2-8,3-7\n6-6,4-6\n2-6,4-8\n")]
    (is (= (sol1 input) 2))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn sol2 [interval-pairs]
  (->> interval-pairs
       (map #(apply overlap %))
       (filter some?)
       count))

(deftest part2-examples
  (let [input (parse "2-4,6-8\n2-3,4-5\n5-7,7-9\n2-8,3-7\n6-6,4-6\n2-6,4-8\n")]
    (is (= (sol2 input) 4))))

; ==============================================================================
; Main
; ==============================================================================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (parse (slurp filename))]
      (println "First:" (sol1 input))
      (println "Second:" (sol2 input)))))
