(ns day14
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn enumerate [coll]
  (map-indexed vector coll))

(defrecord Coord [x y])
(defrecord Grid [width height cells])

(defn parse [s]
  (let [lines (string/split-lines s)
        width (count (first lines))
        height (count lines)
        cells (into {} (for [[y line] (enumerate lines)
                             [x ch] (enumerate line)]
                         {(->Coord x y) ch}))]
    (->Grid width height cells)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn neighbour [dir coord]
  (case dir
    :north (update coord :y dec)
    :south (update coord :y inc)
    :east (update coord :x inc)
    :west (update coord :x dec)))

(defn space? [grid coord]
  (= (get-in grid [:cells coord]) \.))

(defn slide [grid dir coord]
  (assert (= (get-in grid [:cells coord]) \O))
  (loop [cur-grid grid
         cur-coord coord]
    (let [new-coord (neighbour dir cur-coord)
          tile (get-in cur-grid [:cells cur-coord])]
      (if (space? cur-grid new-coord)
        (recur (-> cur-grid
                   (assoc-in [:cells new-coord] tile)
                   (assoc-in [:cells cur-coord] \.))
               new-coord)
        cur-grid))))

(defn tilt [{:keys [width height cells] :as grid} dir]
  (->> (case dir
         :north (for [y (range height) x (range width)] (->Coord x y))
         :south (for [y (reverse (range height)) x (range width)] (->Coord x y))
         :west (for [x (range width) y (range height)] (->Coord x y))
         :east (for [x (reverse (range width)) y (range height)] (->Coord x y)))
       (filter (fn [coord] (= (get cells coord) \O)))
       (reduce (fn [grid coord] (slide grid dir coord))
               grid)))

(defn total-load [grid]
  (->> (:cells grid)
       (filter (fn [[coord tile]] (= tile \O)))
       (map (fn [[coord tile]] (- (:height grid) (:y coord))))
       (reduce +)))

(defn sol1 [input]
  (total-load (tilt input :north)))

(deftest part1-examples
  (let [input (parse "O....#....\nO.OO#....#\n.....##...\nOO.#O....O\n.O.....O#.\nO.#..O.#.#\n..O..#O..O\n.......O..\n#....###..\n#OO..#....\n")]
    (is (= 136 (sol1 input)))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn spin-cycle [grid]
  (->> [:north :west :south :east]
       (reduce (fn [grid dir-fn] (tilt grid dir-fn))
               grid)))

(defn calc-loop [grid]
  (loop [grid grid
         i 0
         seen-at {}]
    (if (contains? seen-at grid)
      {:from  (seen-at grid)
       :every (- i (seen-at grid))}
      (recur (spin-cycle grid) (inc i) (conj seen-at {grid i})))))

(defn sol2 [input]
  (let [{:keys [from every]} (calc-loop input)
        i (+ (mod (- 1000000000 from) every) from)]
    (->> (iterate spin-cycle input)
         (take (inc i))
         last
         total-load)))

(deftest part2-examples
  (let [input (parse "O....#....\nO.OO#....#\n.....##...\nOO.#O....O\n.O.....O#.\nO.#..O.#.#\n..O..#O..O\n.......O..\n#....###..\n#OO..#....\n")]
    (is (= 64 (sol2 input)))))

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
