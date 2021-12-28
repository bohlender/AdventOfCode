(ns day11
  (:require [clojure.string :as string])
  (:use clojure.set)
  (:use clojure.test))

(defn parse-line [s]
  (mapv #(Integer/parseInt (str %)) s))

(defn parse [s]
  "Returns a 2d array. Access via (get-in coll [y x])."
  (mapv parse-line (string/split-lines s)))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn dims [state]
  (let [height (count state)
        width (count (get state 0))]
    [width height]))

(defn in-bounds? [[x y] state]
  (let [[w h] (dims state)]
    (and (<= 0 x (dec w))
         (<= 0 y (dec h)))))

(defn coords [state]
  (let [[width height] (dims state)]
    (for [x (range width)
          y (range height)]
      [x y])))

(defn neighbors [[x y]]
  (for [dx (range -1 2)
        dy (range -1 2)
        :when (not= dx dy 0)]
    [(+ x dx) (+ y dy)]))

(defn inc-energy
  ([state] (inc-energy state (coords state)))
  ([state coords]
   (reduce
     (fn [state [x y]] (update-in state [y x] inc))
     state
     coords)))

(defn flashes? [state [x y]]
  (< 9 (get-in state [y x])))

(defn flash-coords [state]
  (filter #(flashes? state %) (coords state)))

(defn propagate-flash
  ([state]
   (propagate-flash state #{} (flash-coords state)))
  ([state flashed flashing]
   (if (empty? flashing)
     [state flashed]
     (let [new-flashed (into flashed flashing)
           flash-neighbors (filter
                             #(in-bounds? % state)
                             (mapcat neighbors flashing))
           new-state (inc-energy state flash-neighbors)
           new-flashing (difference (set (filter #(flashes? new-state %) flash-neighbors)) new-flashed)]
       (recur new-state new-flashed new-flashing)))))

(defn step [state]
  (let [energized (inc-energy state)
        [state2 flashed] (propagate-flash energized)]
    (reduce
      (fn [state [x y]] (assoc-in state [y x] 0))
      state2
      flashed)))

(defn count-flashed [state]
  (->> state
       flatten
       (filter zero?)
       count))

(def input "5483143223\n2745854711\n5264556173\n6141336146\n6357385478\n4167524645\n2176841721\n6882881134\n4846848554\n5283751526")
(def tmp (parse input))

(defn sol1 [init-state]
  (->> init-state
       (iterate step)
       (take 101)
       (map count-flashed)
       (reduce +)))

(deftest part1-examples
  (let [state-5x5 (parse "11111\n19991\n19191\n19991\n11111")
        state-10x10 (parse "5483143223\n2745854711\n5264556173\n6141336146\n6357385478\n4167524645\n2176841721\n6882881134\n4846848554\n5283751526")]
    (is (= (step state-5x5) [[3 4 5 4 3] [4 0 0 0 4] [5 0 0 0 5] [4 0 0 0 4] [3 4 5 4 3]]))
    (is (= (step (step state-5x5)) [[4 5 6 5 4] [5 1 1 1 5] [6 1 1 1 6] [5 1 1 1 5] [4 5 6 5 4]]))
    (is (= (sol1 state-10x10) 1656))))

; ===================
; Part 2
; ===================

(defn all-flash? [state]
  (->> state
       flatten
       (every? zero?)))

(defn enumerate [coll]
  (->> coll
       (map-indexed list)))

(defn sol2 [init-state]
  (->> init-state
       (iterate step)
       enumerate
       (filter #(all-flash? (second %)))
       first
       first))

(deftest part1-examples
  (let [state-10x10 (parse "5483143223\n2745854711\n5264556173\n6141336146\n6357385478\n4167524645\n2176841721\n6882881134\n4846848554\n5283751526")]
    (is (= (sol2 state-10x10) 195))))

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
