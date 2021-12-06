(ns day06
  (:require [clojure.string :as string])
  (:use clojure.test))

(defn parse [s]
  (let [init-timers (vec (repeat 9 0))]
    (->> (-> s string/trim (string/split #","))
         (map #(Integer/parseInt %))
         (reduce (fn [timers timer] (update timers timer inc)) init-timers))))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn step [timers]
  (let [timers-at-zero (nth timers 0)]
    (map-indexed
      (fn [timer _]
        (case timer
          8 timers-at-zero
          6 (+ (nth timers (inc timer)) timers-at-zero)
          (nth timers (inc timer))))
      timers)))

(defn count-after-days [population days]
  (->> population
       (iterate step)
       (drop days)
       first
       (reduce +)))

(defn sol1 [population]
  (count-after-days population 80))

(deftest part1-examples
  (let [init-population (parse "3,4,3,1,2")]
    (is (= 26 (count-after-days init-population 18)))
    (is (= 5934 (sol1 init-population)))))

; ===================
; Part 2
; ===================
(defn sol2 [population]
  (count-after-days population 256))

(deftest part2-examples
  (let [init-population (parse "3,4,3,1,2")]
    (is (= 26984457539 (sol2 init-population)))))

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
