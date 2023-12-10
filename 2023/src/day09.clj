(ns day09
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn parse-numbers [s]
  (->> (re-seq #"-?\d+" s)
       (mapv parse-long)))

(defn parse [s]
  (->> (string/split-lines s)
       (mapv parse-numbers)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn differences [history]
  (loop [sequences [history]]
    (let [cur-seq (last sequences)]
      (if (every? #{0} cur-seq)
        sequences
        (let [new-seq (->> (partition 2 1 cur-seq)
                           (mapv (fn [[from to]] (- to from))))]
          (recur (conj sequences new-seq)))))))

(defn extrapolate [history]
  (->> (differences history)
       drop-last
       reverse
       (reduce (fn [acc sequence]
                 (+ acc (last sequence)))
               0)))

(defn sol1 [input]
  (->> (map extrapolate input)
       (reduce +)))

(deftest part1-examples
  (let [input (parse "0 3 6 9 12 15\n1 3 6 10 15 21\n10 13 16 21 30 45")]
    (is (= (sol1 input) 114))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn extrapolate-backwards [history]
  (->> (differences history)
       drop-last
       reverse
       (reduce (fn [acc sequence]
                 (- (first sequence) acc))
               0)))

(defn sol2 [input]
  (->> (map extrapolate-backwards input)
       (reduce +)))

(deftest part2-examples
  (let [input (parse "0 3 6 9 12 15\n1 3 6 10 15 21\n10 13 16 21 30 45")]
    (is (= (sol2 input) 2))))

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
