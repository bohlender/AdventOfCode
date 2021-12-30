(ns day11
  (:require [clojure.string :as string])
  (:use clojure.set)
  (:use clojure.test))

(defrecord Coord [x y])

(defn enumerate [coll]
  (map-indexed list coll))

(defn parse [s]
  (->> (for [[y line] (enumerate (string/split-lines s))
             [x c]    (enumerate line)
             :let [energy (Integer/parseInt (str c))]]
         {(->Coord x y) energy})
       (apply merge)))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn neighbors [coord]
  (for [dx (range -1 2)
        dy (range -1 2)
        :when (not= dx dy 0)]
    (->Coord (+ (:x coord) dx) (+ (:y coord) dy))))

; TODO: Simplify
(defn update-values
  "Updates map sequentially"
  ([m f] (update-values m f (keys m)))
  ([m f keyseq]
   (reduce (fn [m k] (update m k f)) m keyseq)))

(defn inc-energy
  ([state] (update-values state inc))
  ([state coords] (update-values state inc coords)))

(defn flashes? [state coord]
  (< 9 (get state coord)))

(defn flash-coords [state]
  (filter #(flashes? state %) (keys state)))

(defn propagate-flash
  ([state]
   (propagate-flash state #{} (flash-coords state)))
  ([state flashed flashing]
   (if (empty? flashing)
     [state flashed]
     (let [new-flashed (into flashed flashing)
           flash-neighbors (filter state (mapcat neighbors flashing))
           new-state (inc-energy state flash-neighbors)
           new-flashing (difference (set (filter #(flashes? new-state %) flash-neighbors)) new-flashed)]
       (recur new-state new-flashed new-flashing)))))

(defn step [state]
  (let [energized (inc-energy state)
        [state2 flashed] (propagate-flash energized)]
    (update-values state2 (fn [v] 0) flashed)))

(defn count-flashed [state]
  (->> state vals (filter zero?) count))

(defn sol1 [state]
  (->> state
       (iterate step)
       (take 101)
       (map count-flashed)
       (reduce +)))

(deftest part1-examples
  (let [state-5x5 (parse "11111\n19991\n19191\n19991\n11111")
        state-10x10 (parse "5483143223\n2745854711\n5264556173\n6141336146\n6357385478\n4167524645\n2176841721\n6882881134\n4846848554\n5283751526")]
    (is (= (step state-5x5) (parse "34543\n40004\n50005\n40004\n34543")))
    (is (= (step (step state-5x5)) (parse "45654\n51115\n61116\n51115\n45654")))
    (is (= (sol1 state-10x10) 1656))))

; ===================
; Part 2
; ===================

(defn all-flash? [state]
  (->> state vals (every? zero?)))

(defn sol2 [init-state]
  (->> init-state
       (iterate step)
       enumerate
       (filter #(all-flash? (second %)))
       first
       first))

(deftest part2-examples
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
