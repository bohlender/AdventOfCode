(ns day11
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn enumerate [coll]
  (map-indexed vector coll))

(defn parse-galaxies [lines]
  (->> (for [[y line] (enumerate lines)
             [x c] (enumerate line)
             :when (= c \#)]
         {:x x, :y y})
       (map-indexed (fn [idx coord] {:id    idx
                                     :coord coord}))
       (into [])))

(defn empty-rows [lines]
  (for [[y line] (enumerate lines)
        :when (every? #{\.} line)]
    y))

(defn empty-cols [lines]
  (let [width (count (first lines))]
    (for [x (range width)
          :let [col (for [line lines] (nth line x))]
          :when (every? #{\.} col)]
      x)))

(defn parse [s]
  (let [lines (string/split-lines s)]
    {:galaxies   (parse-galaxies lines)
     :empty-rows (empty-rows lines)
     :empty-cols (empty-cols lines)}))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn manhattan-dist [from to]
  (+ (abs (- (:x to) (:x from)))
     (abs (- (:y to) (:y from)))))

(defn transform-coord [empty-rows empty-cols factor coord]
  (let [num-relevant-cols (count (filter #(< % (:x coord)) empty-cols))
        num-relevant-rows (count (filter #(< % (:y coord)) empty-rows))]
    (-> coord
        (update :x #(+ % (* num-relevant-cols factor)))
        (update :y #(+ % (* num-relevant-rows factor))))))

(defn transform-space [coord-tf galaxies]
  (->> (for [galaxy galaxies]
         (update galaxy :coord coord-tf))
       (into [])))

(defn distance-pairs [galaxies]
  (for [from-id (range (count galaxies))
        to-id (range (count galaxies))
        :while (> from-id to-id)]
    {:from from-id
     :to   to-id
     :dist (manhattan-dist (:coord (galaxies from-id))
                           (:coord (galaxies to-id)))}))

(defn sol1 [{:keys [empty-rows empty-cols galaxies] :as input}]
  (->> galaxies
       (transform-space (partial transform-coord empty-rows empty-cols 1))
       distance-pairs
       (map :dist)
       (reduce +)))

(deftest part1-examples
  (let [input (parse "...#......\n.......#..\n#.........\n..........\n......#...\n.#........\n.........#\n..........\n.......#..\n#...#.....\n")]
    (is (= (sol1 input) 374))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn sol2 [{:keys [empty-rows empty-cols galaxies] :as input}]
  (->> galaxies
       (transform-space (partial transform-coord empty-rows empty-cols (dec 1000000)))
       distance-pairs
       (map :dist)
       (reduce +)))

(deftest part2-examples
  (let [input (parse "...#......\n.......#..\n#.........\n..........\n......#...\n.#........\n.........#\n..........\n.......#..\n#...#.....\n")]
    (is (= (sol2 input) 82000210))))

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
