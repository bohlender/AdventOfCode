(ns day09
  (:require [clojure.string :as string])
  (:use clojure.set)
  (:use clojure.test))

(defn parse-line [s]
  (mapv #(Integer/parseInt %) (string/split s #"")))

(defn parse [s]
  "Returns a 2d array. Access via (get-in coll [y x])."
  (->> s
       string/split-lines
       (mapv parse-line)))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn get-height [array-2d] (count array-2d))
(defn get-width [array-2d] (count (first array-2d)))
(defn get-val [array-2d [x y]] (get-in array-2d [y x]))

(defn coords [array-2d]
  (for [y (range (get-height array-2d))
        x (range (get-width array-2d))]
    [x y]))

(defn neighbour-coords [[x y]]
  [[x (dec y)], [(dec x) y], [(inc x) y], [x (inc y)]])

(defn neighbours [array-2d coord]
  (->> coord
       neighbour-coords
       (map #(get-val array-2d %))
       (filter boolean)))

(defn low-point? [array-2d [x y]]
  (let [n (apply min (neighbours array-2d [x y]))
        v (get-in array-2d [y x])]
    (if (< v n) v false)))

(defn sol1 [array-2d]
  (->> array-2d
       coords
       (filter #(low-point? array-2d %))
       (map #(get-val array-2d %))
       (map inc)
       (reduce +)))

(deftest part1-examples
  (is (= 15 (sol1 (parse "2199943210\n3987894921\n9856789892\n8767896789\n9899965678")))))

; ===================
; Part 2
; ===================
(defn basin-ascent? [array-2d from to]
  (< (get-val array-2d from) (get-val array-2d to) 9))

(defn asc-neighbour-coords [array-2d from]
  (->> from
       neighbour-coords
       (filter #(get-val array-2d %))
       (filter #(basin-ascent? array-2d from %))))

(defn basin-coords
  ([array-2d low-point]
   (basin-coords array-2d #{low-point} #{low-point}))
  ([array-2d basin border]
   (let [new-coords (mapcat #(asc-neighbour-coords array-2d %) border)
         new-border (difference (set new-coords) basin)
         new-basin (into basin border)]
     (if (empty? new-border)
       new-basin
       (recur array-2d new-basin new-border))
   )))

(defn basin-size [array-2d low-point]
  (count (basin-coords array-2d low-point)))

(defn sol2 [array-2d]
  (->> array-2d
       coords
       (filter #(low-point? array-2d %))
       (map #(basin-size array-2d %))
       sort
       (take-last 3)
       (reduce *)))

(deftest part2-examples
  (is (= 1134 (sol2 (parse "2199943210\n3987894921\n9856789892\n8767896789\n9899965678")))))

; ===================
; Main
; ===================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (parse-file filename)]
      (println "First:" (time (sol1 input)))
      (println "Second:" (sol2 input)))))
