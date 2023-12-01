(ns day01
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn parse [s parse-line-fn]
  (->> (string/split-lines s)
       (map parse-line-fn)))

; ==============================================================================
; Part 1
; ==============================================================================
(defn parse-line [s]
  (let [digits (re-seq #"\d" s)]
    (->> (str (first digits) (last digits))
         Integer/parseInt)))

(defn sol1 [s]
  (->> (parse s parse-line)
       (reduce +)))

(deftest part1-examples
  (let [s "1abc2\npqr3stu8vwx\na1b2c3d4e5f\ntreb7uchet"]
    (is (= (sol1 s) 142))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn- replace-words [s]
  (reduce (fn [acc [from to]] (string/replace acc from (str from to from)))
          s
          {"one"   "1"
           "two"   "2"
           "three" "3"
           "four"  "4"
           "five"  "5"
           "six"   "6"
           "seven" "7"
           "eight" "8"
           "nine"  "9"}))

(defn sol2 [s]
  (->> (parse s (comp parse-line replace-words))
       (reduce +)))

(deftest part2-examples
  (let [s "two1nine\neightwothree\nabcone2threexyz\nxtwone3four\n4nineeightseven2\nzoneight234\n7pqrstsixteen"]
    (is (= (sol2 s) 281))))

; ==============================================================================
; Main
; ==============================================================================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          s (->> filename slurp)]
      (println "First:" (sol1 s))
      (println "Second:" (sol2 s)))))
