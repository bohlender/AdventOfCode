(ns day10
  (:require [clojure.test :refer :all]
            [clojure.string :as string])
  (:import (clojure.lang PersistentQueue)))

(defn enumerate [coll]
  (map-indexed vector coll))

(defrecord Grid [width height cells])

(def connects-to
  {\| #{:N :S}
   \- #{:E :W}
   \L #{:N :E}
   \J #{:N :W}
   \7 #{:S :W}
   \F #{:S :E}
   \. #{}})

(defn north [coord] (update coord :y dec))
(defn east [coord] (update coord :x inc))
(defn south [coord] (update coord :y inc))
(defn west [coord] (update coord :x dec))

(defn adjacent [cells coord]
  (let [n-coord (north coord)
        e-coord (east coord)
        s-coord (south coord)
        w-coord (west coord)]
    (cond-> #{}
            (and (some #{:N} (get cells coord))
                 (some #{:S} (get cells n-coord))) (conj n-coord)
            (and (some #{:E} (get cells coord))
                 (some #{:W} (get cells e-coord))) (conj e-coord)
            (and (some #{:S} (get cells coord))
                 (some #{:N} (get cells s-coord))) (conj s-coord)
            (and (some #{:W} (get cells coord))
                 (some #{:E} (get cells w-coord))) (conj w-coord))))

(defn determine-pipe [cells coord]
  (cond-> #{}
          (some #{:S} (get cells (north coord))) (conj :N)
          (some #{:W} (get cells (east coord))) (conj :E)
          (some #{:N} (get cells (south coord))) (conj :S)
          (some #{:E} (get cells (west coord))) (conj :W)))

(defn parse [s]
  (let [lines (string/split-lines s)
        S-coord (first (for [[y line] (enumerate lines)
                             [x c] (enumerate line)
                             :when (= c \S)]
                         {:x x, :y y}))
        cells (->> (for [[y line] (enumerate lines)
                         [x c] (enumerate line)]
                     {{:x x, :y y} (connects-to c)})
                   (into {}))
        height (count lines)
        width (count (first lines))
        grid (->Grid width height cells)
        S-coord-pipe (determine-pipe (:cells grid) S-coord)]
    {:start S-coord
     :grid  (assoc-in grid [:cells S-coord] S-coord-pipe)}))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn distances [cells from]
  (loop [worklist (conj PersistentQueue/EMPTY from)
         distances {from 0}]
    (if-some [cur (peek worklist)]
      (let [new-dist (inc (distances cur))
            neighbours (->> (adjacent cells cur)
                            (remove #(contains? distances %)))
            new-distances (into distances (for [n neighbours]
                                            {n new-dist}))
            new-worklist (into (pop worklist) neighbours)]
        (recur new-worklist new-distances))
      distances)))

(defn sol1 [input]
  (->> (distances (:cells (:grid input)) (:start input))
       vals
       (apply max)))

(deftest part1-examples
  (is (= 4 (sol1 (parse "-L|F7\n7S-7|\nL|7||\n-L-J|\nL|-JF"))))
  (is (= 8 (sol1 (parse "..F7.\n.FJ|.\nSJ.L7\n|F--J\nLJ...\n")))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn enclosed-in-row [{:keys [width height cells] :as grid} loop-coords y]
  (loop [x 0
         inside-parts #{}
         res 0]
    (let [coord {:x x, :y y}
          coord-cell (get cells coord)]
      (if (= x width)
        res
        ; inside-parts only change when a vertical loop border is encountered
        (if (and (contains? loop-coords coord) (some #{:N :S} coord-cell))
          (let [next-inside-parts (case coord-cell
                                    #{:N :S} (if (empty? inside-parts)
                                               #{:N :S}
                                               #{})
                                    #{:N :E} (if (empty? inside-parts)
                                               #{:N}
                                               #{:S})
                                    #{:S :E} (if (empty? inside-parts)
                                               #{:S}
                                               #{:N})
                                    #{:N :W} (if (some #{:N} inside-parts)
                                               #{}
                                               #{:N :S})
                                    #{:S :W} (if (some #{:S} inside-parts)
                                               #{}
                                               #{:N :S}))]
            (recur (inc x) next-inside-parts res))
          (let [next-res (if (and (not-empty inside-parts) (= coord-cell #{}))
                           (inc res)
                           res)]
            (recur (inc x) inside-parts next-res)))))))

(defn sol2 [input]
  (let [loop-coords (set (keys (distances (:cells (:grid input)) (:start input))))
        new-cells (->> (for [[coord pipes] (:cells (:grid input))]
                         (if (contains? loop-coords coord)
                           {coord pipes}
                           {coord #{}}))
                       (into {}))
        grid (assoc (:grid input) :cells new-cells)]
    (->> (for [y (range 0 (:height grid))]
           (enclosed-in-row grid loop-coords y))
         (reduce +))))

(deftest part2-examples
  (is (= 4 (sol2 (parse "...........\n.S-------7.\n.|F-----7|.\n.||.....||.\n.||.....||.\n.|L-7.F-J|.\n.|..|.|..|.\n.L--J.L--J.\n..........."))))
  (is (= 8 (sol2 (parse ".F----7F7F7F7F-7....\n.|F--7||||||||FJ....\n.||.FJ||||||||L7....\nFJL7L7LJLJ||LJ.L-7..\nL--J.L7...LJS7F-7L7.\n....F-J..F7FJ|L7L7L7\n....L7.F7||L7|.L7L7|\n.....|FJLJ|FJ|F7|.LJ\n....FJL-7.||.||||...\n....L---J.LJ.LJLJ...\n"))))
  (is (= 10 (sol2 (parse "FF7FSF7F7F7F7F7F---7\nL|LJ||||||||||||F--J\nFL-7LJLJ||||||LJL-77\nF--JF--7||LJLJ7F7FJ-\nL---JF-JLJ.||-FJLJJ7\n|F|F-JF---7F7-L7L|7|\n|FFJF7L7F-JF7|JL---7\n7-L-JL7||F7|L7F-7F7|\nL.L7LFJ|||||FJL7||LJ\nL7JLJL-JLJLJL--JLJ.L\n")))))

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
