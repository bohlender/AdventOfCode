(ns day01
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn parse-calories [lines]
  (->> (string/split-lines lines)
       (map #(Integer/parseInt %))))

(defn parse [s]
  (->> (string/split s #"\n\n")
       (map parse-calories)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn sum [coll]
  (reduce + coll))

(defn sol1 [input]
  (->> input
       (map sum)
       (reduce max)))

(deftest part1-examples
  (let [input (parse "1000\n2000\n3000\n\n4000\n\n5000\n6000\n\n7000\n8000\n9000\n\n10000")]
    (is (= (sol1 input) 24000))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn sol2 [input]
  (->> input
       (map sum)
       (sort >)
       (take 3)
       (reduce +)))

(deftest part2-examples
  (let [input (parse "1000\n2000\n3000\n\n4000\n\n5000\n6000\n\n7000\n8000\n9000\n\n10000")]
    (is (= (sol2 input) 45000))))

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