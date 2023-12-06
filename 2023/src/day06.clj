(ns day06
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

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
(defn distance [time-limit hold-time]
  (let [drive-time (- time-limit hold-time)
        speed (* 1 hold-time)]
    (* speed drive-time)))

(defn counter
  ([acc _] (inc acc))
  ([acc] acc)
  ([] 0))

(defn ways-to-win [[time-limit record-distance :as race]]
  (->> (range 0 (inc time-limit))
       (transduce (comp (map (partial distance time-limit))
                        (filter #(> % record-distance)))
                  counter)))

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
