(ns day16
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defrecord Grid [width height cells])
(defrecord Vec2 [x y])

(defn enumerate [coll]
  (map-indexed vector coll))

(defn parse [s]
  (let [lines (string/split-lines s)
        width (count (first lines))
        height (count lines)
        cells (into {} (for [[y line] (enumerate lines)
                             [x ch] (enumerate line)]
                         {(->Vec2 x (- height y 1)) ch}))]
    (->Grid width height cells)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defrecord Vec2 [x y])
(defrecord Beam [pos dir])

(defn rot-l [{:keys [x y] :as v}] (->Vec2 (- y) x))
(defn rot-r [{:keys [x y] :as v}] (->Vec2 y (- x)))

(defn horz? [v] (not (zero? (:x v))))

(defn add-vec2 [lhs rhs]
  (merge-with + lhs rhs))

(defn step [beam]
  (update beam :pos add-vec2 (:dir beam)))

(defn propagate [grid beam]
  (loop [worklist (list beam)
         visited #{}]
    (if-some [{:keys [pos dir] :as cur} (peek worklist)]
      (let [tile (get-in grid [:cells pos])
            rot-beams (case tile
                        \. [cur]
                        \| (if (horz? dir)
                             [(update cur :dir rot-l) (update cur :dir rot-r)]
                             [cur])
                        \- (if (horz? dir)
                             [cur]
                             [(update cur :dir rot-l) (update cur :dir rot-r)])
                        \/ (if (horz? dir)
                             [(update cur :dir rot-l)]
                             [(update cur :dir rot-r)])
                        \\ (if (horz? dir)
                             [(update cur :dir rot-r)]
                             [(update cur :dir rot-l)]))]
        (recur (into (pop worklist)
                     (comp
                       (map step)
                       (filter (fn [beam] (contains? (:cells grid) (:pos beam))))
                       (remove (fn [beam] (contains? visited beam))))
                     rot-beams)
               (conj visited cur)))
      visited)))

(defn sol1
  ([input]
   (->> (->Beam (->Vec2 0 (dec (:height input))) (->Vec2 1 0))
        (sol1 input)))

  ([input init-beam]
   (->> (propagate input init-beam)
        (map :pos)
        set
        count)))

(deftest part1-examples
  (let [input (parse ".|...\\....\n|.-.\\.....\n.....|-...\n........|.\n..........\n.........\\\n..../.\\\\..\n.-.-/..|..\n.|....-|.\\\n..//.|....\n")]
    (is (= 46 (sol1 input)))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn sol2 [{:keys [width height] :as input}]
  (let [from-left (for [y (range height)] (->Beam (->Vec2 0 y) (->Vec2 1 0)))
        from-right (for [y (range height)] (->Beam (->Vec2 (dec width) y) (->Vec2 -1 0)))
        from-bottom (for [x (range width)] (->Beam (->Vec2 x 0) (->Vec2 0 1)))
        from-top (for [x (range width)] (->Beam (->Vec2 x (dec height)) (->Vec2 0 -1)))
        beams (concat from-left from-right from-bottom from-top)]
    (->> beams
         (map (partial sol1 input))
         (reduce max))))

(deftest part2-examples
  (let [input (parse ".|...\\....\n|.-.\\.....\n.....|-...\n........|.\n..........\n.........\\\n..../.\\\\..\n.-.-/..|..\n.|....-|.\\\n..//.|....\n")]
    (is (= 51 (sol2 input)))))

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
