(ns day05
  (:require [clojure.string :as string])
  (:use clojure.test))

(defn parse-line [s]
  (let [[_ & str-coords] (re-matches #"(\d+),(\d+) -> (\d+),(\d+)" s)
        [x1 y1 x2 y2] (map #(Integer/parseInt %) str-coords)]
    {:src {:x x1 :y y1} :dst {:x x2 :y y2}}))

(defn parse [s]
  (map parse-line (string/split-lines s)))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn get-coords [line]
  (let [[left right]  (sort-by :x [(:src line) (:dst line)])
        [min-y max-y] (sort (map :y [left right]))
        dx            (- (:x right) (:x left))
        dy            (- (:y right) (:y left))]
    (if (= dx 0)
      (for [y (range min-y (+ max-y 1))]
        {:x (:x left) :y y})
      (for [x (range (:x left) (+ (:x right) 1))]
        (let [x-off (- x (:x left))
              y-off (* (/ dy dx) x-off)]
          {:x x :y (+ y-off (:y left))})))))

(defn diagonal? [line]
  (and (not= (get-in line [:src :x]) (get-in line [:dst :x]))
       (not= (get-in line [:src :y]) (get-in line [:dst :y]))))

(defn sol1 [lines]
  (let [relevant-lines (remove diagonal? lines)
        coords (flatten (map get-coords relevant-lines))
        freqs (frequencies coords)]
   (count (filter #(>= (val %) 2) freqs))))

(deftest part1-examples
  (is (= 5 (sol1 (parse "0,9 -> 5,9\n8,0 -> 0,8\n9,4 -> 3,4\n2,2 -> 2,1\n7,0 -> 7,4\n6,4 -> 2,0\n0,9 -> 2,9\n3,4 -> 1,4\n0,0 -> 8,8\n5,5 -> 8,2")))))

; ===================
; Part 2
; ===================
(defn sol2 [lines]
  (let [coords (flatten (map get-coords lines))
        freqs (frequencies coords)]
   (count (filter #(>= (val %) 2) freqs))))

(deftest part2-examples
  (is (= 12 (sol2 (parse "0,9 -> 5,9\n8,0 -> 0,8\n9,4 -> 3,4\n2,2 -> 2,1\n7,0 -> 7,4\n6,4 -> 2,0\n0,9 -> 2,9\n3,4 -> 1,4\n0,0 -> 8,8\n5,5 -> 8,2")))))

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
