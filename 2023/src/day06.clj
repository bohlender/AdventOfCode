(ns day06
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.math :refer [sqrt pow ceil floor]]))

(defn parse-numbers [s]
  (->> (re-seq #"\d+" s)
       (mapv #(Long/parseLong %))))

(with-test
  (defn parse [s]
    (let [lines (string/split-lines s)
          time-limits (parse-numbers (first lines))
          distances (parse-numbers (second lines))]
      (map vector time-limits distances)))

  (is (= (parse "Time:      7  15   30\nDistance:  9  40  200\n")
         '([7 9] [15 40] [30 200]))))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
; x² + px + q = 0
(defn solve-quardratic [p q]
  (let [from (- (- (/ p 2)) (sqrt (- (pow (/ p 2) 2) q)))
        to (+ (- (/ p 2)) (sqrt (- (pow (/ p 2) 2) q)))]
    [from to]))

(defn dbl-is-int? [d]
  (= d (floor d)))

(defn ways-to-win [[time-limit record-distance :as race]]
  (let [[from-double to-double] (solve-quardratic (- time-limit) record-distance)
        ; We actually need the next integer above `from` & below `to`
        from (if (dbl-is-int? from-double) (inc from-double) (ceil from-double))
        to (if (dbl-is-int? to-double) (dec to-double) (floor to-double))]
    (->> (- to from) inc int)))

(defn sol1 [input]
  (->> (map ways-to-win input)
       (reduce *)))

(deftest part1-examples
  (let [input (parse "Time:      7  15   30\nDistance:  9  40  200\n")]
    (is (= (sol1 input) 288))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn join-ints [ints]
  (->> (apply str ints)
       Long/parseLong))

(defn fix-kerning [input]
  (let [[times distances] (apply (partial map vector) input)]
    (list [(join-ints times) (join-ints distances)])))

(defn sol2 [input]
  (->> (fix-kerning input)
       sol1))

(deftest part2-examples
  (let [input (parse "Time:      7  15   30\nDistance:  9  40  200\n")]
    (is (= (sol2 input) 71503))))

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
