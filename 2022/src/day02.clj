(ns day02
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.set :as set]))

(defn parse-round [line]
  (->> (string/split line #"\s")
       (map keyword)))

(defn parse [s]
  (->> (string/split-lines s)
       (map parse-round)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn decrypt [[lhs rhs]]
  [(get {:A :rock, :B :paper, :C :scissors} lhs)
   (get {:X :rock, :Y :paper, :Z :scissors} rhs)])

(def counter-shape {:scissors :rock, :rock :paper, :paper :scissors})

(defn won? [[other me]]
  (= (counter-shape other) me))

(defn score [[other me :as choices]]
  (let [shape-score ({:rock 1 :paper 2 :scissors 3} me)
        outcome-score (cond
                        (= other me) 3
                        (won? choices) 6
                        :else 0)]
    (+ outcome-score shape-score)))

(defn sol1 [input]
  (->> input
       (map (comp score decrypt))
       (reduce +)))

(deftest part1-examples
  (let [input (parse "A Y\nB X\nC Z")]
    (is (= (sol1 input) 15))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn proper-decrypt [[lhs rhs]]
  [(get {:A :rock, :B :paper, :C :scissors} lhs)
   (get {:X :lose, :Y :draw, :Z :win} rhs)])

(defn interpret [[opponent-shape todo]]
  (let [reaction (case todo
                   :win (counter-shape opponent-shape)
                   :lose (get (set/map-invert counter-shape) opponent-shape)
                   opponent-shape)]
    [opponent-shape reaction]))

(defn sol2 [input]
  (->> input
       (map (comp score interpret proper-decrypt))
       (reduce +)))

(deftest part2-examples
  (let [input (parse "A Y\nB X\nC Z")]
    (is (= (sol2 input) 12))))

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