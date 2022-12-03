(ns day03
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.set :as set]))

(defn parse-rucksack [line]
  (->> line
       (split-at (/ (count line) 2))))

(defn parse [s]
  (->> (string/split-lines s)
       (map parse-rucksack)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn common-item [colls]
  (->> (map set colls)
       (apply set/intersection)
       first))

(defn priority [item]
  (if (Character/isLowerCase item)
    (+ 1 (- (Character/getNumericValue item) (Character/getNumericValue \a)))
    (+ 27 (- (Character/getNumericValue item) (Character/getNumericValue \A)))))

(defn sol1 [input]
  (->> input
       (map (comp priority common-item))
       (reduce +)))

(deftest part1-examples
  (let [input (parse "vJrwpWtwJgWrhcsFMMfFFhFp\njqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL\nPmmdzqPrVvPwwTWBwg\nwMqvLMZHhHMvwLHjbvcjnnSBnvTQFn\nttgJtRGJQctTZtZT\nCrZsJsPPZsGzwwsLwLmpwMDw")]
    (= (sol1 input) 157)))

; ==============================================================================
; Part 2
; ==============================================================================
(let [input (parse "vJrwpWtwJgWrhcsFMMfFFhFp\njqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL\nPmmdzqPrVvPwwTWBwg\nwMqvLMZHhHMvwLHjbvcjnnSBnvTQFn\nttgJtRGJQctTZtZT\nCrZsJsPPZsGzwwsLwLmpwMDw")]
  (->> input
       (map #(apply concat %))
       (partition 3)
       (map (comp priority common-item))
       (reduce +)))

(deftest part2-examples
  (let [input (parse "vJrwpWtwJgWrhcsFMMfFFhFp\njqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL\nPmmdzqPrVvPwwTWBwg\nwMqvLMZHhHMvwLHjbvcjnnSBnvTQFn\nttgJtRGJQctTZtZT\nCrZsJsPPZsGzwwsLwLmpwMDw")]
    (= (sol2 input) 70)))

; ==============================================================================
; Main
; ==============================================================================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (parse-file filename)]
      (println "First:" (sol1 input))
      (println "Second:" (sol2 input)))))