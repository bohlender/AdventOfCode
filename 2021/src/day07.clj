(ns day07
  (:require [clojure.string :as string])
  (:require [clojure.math.numeric-tower :as math])
  (:use clojure.test))

(defn parse [s]
  (->> (string/split (string/trim s) #",")
       (map #(Integer/parseInt %))))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn linear-move-cost [from to]
  (math/abs (- to from)))

(defn move-costs [cost-fn positions]
  (let [lb (apply min positions)
        ub (inc (apply max positions))]
    (for [to (range lb ub)]
      (->> positions
           (map #(cost-fn % to))
           (reduce +)))))

(defn min-cost [cost-fn positions]
  (->> positions
       (move-costs cost-fn)
       (partition 2 1)
       (drop-while #(>= (first %) (second %))) ; TODO: O(n) -> O(log n)
       first
       first))

(defn sol1 [positions]
  (min-cost linear-move-cost positions))

(deftest part1-examples
 (let [positions (parse "16,1,2,0,4,2,7,1,2,14")
       costs (move-costs linear-move-cost positions)]
   (is (= 14 (linear-move-cost 16 2)))
   (is (= 41 (nth costs 1)))
   (is (= 39 (nth costs 3)))
   (is (= 71 (nth costs 10)))
   (is (= 37 (sol1 positions)))))

; ===================
; Part 2
; ===================
(defn triangular-move-cost [from to]
  (let [span (math/abs (- to from))]
    (/ (* span (+ span 1)) 2)))

(defn sol2 [positions]
  (min-cost triangular-move-cost positions))

(deftest part2-examples
 (let [positions (parse "16,1,2,0,4,2,7,1,2,14")
       costs (move-costs triangular-move-cost positions)]
   (is (= 66 (triangular-move-cost 16 5)))
   (is (= 206 (nth costs 2)))
   (is (= 168 (sol2 positions)))))

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
