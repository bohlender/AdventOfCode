(ns day13
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.set :as set]))

(defrecord Grid [width height rows])

(defn parse-pattern [s]
  (let [lines (string/split-lines s)]
    (->Grid (count (first lines))
            (count lines)
            lines)))

(defn parse [s]
  (->> (string/split s #"\n\n")
       (map parse-pattern)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1 ; TODO: Too much code replication
; ==============================================================================
(defn row [grid y]
  (get-in grid [:rows y]))

(defn col [grid x]
  (for [row (:rows grid)] (nth row x)))

(defn reflection-x? [grid x]
  (when (< 0 x (:width grid))
    (->> (for [y (range (:height grid))
               :let [row (row grid y)
                     lhs (take x row)
                     rhs (drop x row)]]
           (->> (map (fn [s1 s2] (= s1 s2)) (reverse lhs) rhs)
                (every? true?)))
         (every? true?))))

(defn reflection-y? [grid y]
  (when (< 0 y (:height grid))
    (->> (for [x (range (:width grid))
               :let [col (col grid x)
                     lhs (take y col)
                     rhs (drop y col)]]
           (->> (map (fn [s1 s2] (= s1 s2)) (reverse lhs) rhs)
                (every? true?)))
         (every? true?))))

(defn reflection-xs [grid]
  (->> (range (:width grid))
       (filter (partial reflection-x? grid))))

(defn reflection-ys [grid]
  (->> (range (:height grid))
       (filter (partial reflection-y? grid))))

(defn score [x y]
  (+ (* 100 (or y 0))
     (or x 0)))

(defn sol1 [input]
  (->> (for [grid input]
         (let [x (first (reflection-xs grid))
               y (first (reflection-ys grid))]
           (score x y)))
       (reduce +)))

(deftest part1-examples
  (let [input (parse "#.##..##.\n..#.##.#.\n##......#\n##......#\n..#.##.#.\n..##..##.\n#.#.##.#.\n\n#...##..#\n#....#..#\n..##..###\n#####.##.\n#####.##.\n..##..###\n#....#..#\n")]
    (is (= 405 (sol1 input)))))

; ==============================================================================
; Part 2 ; TODO: Shabby duplicate difference check
; ==============================================================================
(defn enumerate [coll]
  (map-indexed vector coll))

(def flip {\. \#, \# \.})

(defn string-replace-at [s idx c]
  (str (subs s 0 idx) c (subs s (inc idx))))

(defn alt-grids [grid]
  (for [[y row] (enumerate (:rows grid))
        [x ch] (enumerate row)
        :let [new-row (string-replace-at row x (flip ch))]]
    (assoc-in grid [:rows y] new-row)))

(defn fix-smudge [grid]
  (let [old-xs (reflection-xs grid)
        old-ys (reflection-ys grid)]
    (->> (for [alt-grid (alt-grids grid)
               ; fixed grid may have multiple reflection indexes
               :let [new-xs (reflection-xs alt-grid)
                     new-ys (reflection-ys alt-grid)]
               :when (or (not-empty (set/difference (set new-xs) (set old-xs)))
                         (not-empty (set/difference (set new-ys) (set old-ys))))]
           alt-grid)
         first)))

(defn sol2 [input]
  (->> (for [grid input]
         (let [new-grid (fix-smudge grid)
               x (first (set/difference (set (reflection-xs new-grid))
                                        (set (reflection-xs grid))))
               y (first (set/difference (set (reflection-ys new-grid))
                                        (set (reflection-ys grid))))]
           (score x y)))
       (reduce +)))

(deftest part2-examples
  (let [input (parse "#.##..##.\n..#.##.#.\n##......#\n##......#\n..#.##.#.\n..##..##.\n#.#.##.#.\n\n#...##..#\n#....#..#\n..##..###\n#####.##.\n#####.##.\n..##..###\n#....#..#\n")]
    (is (= 400 (sol2 input)))))

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
