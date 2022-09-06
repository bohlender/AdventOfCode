(ns day20
  (:require [clojure.string :as string]
            [clojure.test :refer :all]))

(defn parse-image-enhancement-algo [enhancement-str]
  "Returns a fn which yields true if the input pattern (encoded as an integer) maps to a lit pixel."
  (fn [pattern] (= (nth enhancement-str pattern) \#)))

(defn enumerate [coll]
  (map-indexed vector coll))

(defn parse-lit-pixels [image-str]
  "Returns the coordinates of lit pixels."
  (->> (for [[y line] (enumerate (string/split-lines image-str))
             [x c] (enumerate line)
             :when (= c \#)]
         {:x x :y y})
       (into #{})))

(defn parse [s]
  "Returns the 'image enhancement algorithm' as a function and the coordinates of lit pixels."
  (let [[enhancement-str image-str] (string/split s #"\n{2,}")]
    {:enhance-algo (parse-image-enhancement-algo enhancement-str)
     :lit-pixels   (parse-lit-pixels image-str)}))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(def min-max
  "Returns the min and max of a collection."
  (juxt (partial apply min)
        (partial apply max)))

(defn print-pixels [lit-pixels]
  (println "count:" (count lit-pixels))
  (let [[min-x max-x] (min-max (map :x lit-pixels))
        [min-y max-y] (min-max (map :y lit-pixels))]
    (doseq [y (range min-y (inc max-y))]
      (->> (range min-x (inc max-x))
           (map (fn [x] (if (get lit-pixels {:x x :y y}) "#" ".")))
           (apply str)
           println))))

(defn bits->int [bits]
  "Returns the (unsigned) integer value represented by a bit sequence."
  (->> bits
       reverse
       (map-indexed (fn [bit-idx bit-val] (bit-shift-left bit-val bit-idx)))
       (reduce +)))

(defn pattern-at [lit-pixels {:keys [x y]}]
  "Returns the 3x3 pattern centered at a coordinate (encoded as an integer)."
  (->> (for [y (range (dec y) (+ y 2))
             x (range (dec x) (+ x 2))]
         {:x x :y y})
       (map (comp #(if % 1 0) lit-pixels))
       bits->int))

(def bg-border-width 2)

(defn- enhance [enhance-algo lit-pixels]
  "Returns the set of lit pixels after one application of the 'enhancement algorithm'."
  (let [[min-x max-x] (min-max (map :x lit-pixels))
        [min-y max-y] (min-max (map :y lit-pixels))]
    (->> (for [y (range (- min-y 1 bg-border-width) (+ max-y 2 bg-border-width))
               x (range (- min-x 1 bg-border-width) (+ max-x 2 bg-border-width))
               :let [coord {:x x :y y}
                     pattern (pattern-at lit-pixels coord)]
               :when (enhance-algo pattern)]
           coord)
         (into #{}))))

; TODO: Avoid this workaround for handling the infinite "flashing" plane by adding the bg-color to the image state
(defn enhance-twice [enhance-algo lit-pixels]
  (let [[min-x max-x] (min-max (map :x lit-pixels))
        [min-y max-y] (min-max (map :y lit-pixels))
        step (partial enhance enhance-algo)]
    (->> lit-pixels
         step
         step
         (filter #(and (<= (- min-x 2) (:x %) (+ max-x 2)) ; trim image to non-background part
                       (<= (- min-y 2) (:y %) (+ max-y 2))))
         (into #{}))))

(defn sol1 [{:keys [enhance-algo lit-pixels]}]
  (let [step (partial enhance-twice enhance-algo)
        it (iterate step lit-pixels)]
    (->> (nth it 1)
         count)))

(deftest part1-examples
  (let [input (parse "..#.#..#####.#.#.#.###.##.....###.##.#..###.####..#####..#....#..#..##..###..######.###...####..#..#####..##..#.#####...##.#.#..#.##..#.#......#.###.######.###.####...#.##.##..#..#..#####.....#.#....###..#.##......#.....#..#..#..##..#...##.######.####.####.#.#...#.......#..#.#.#...####.##.#......#..#...##.#.##..#...##.#.##..###.#......#.#.......#.#.#.####.###.##...#.....####.#..#..#.##.#....##..#.####....##...##..#...#......#.#.......#.......##..####..#...#.#.#...##..#.#..###..#####........#..####......#..#\n\n#..#.\n#....\n##..#\n..#..\n..###")]
    (is (= (pattern-at (:lit-pixels input) {:x 2 :y 2}) 34))
    (is (= (sol1 input) 35))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn sol2 [{:keys [enhance-algo lit-pixels]}]
  (let [step (partial enhance-twice enhance-algo)
        it (iterate step lit-pixels)]
    (->> (nth it 25)
         count)))

(deftest part2-examples
  (let [input (parse "..#.#..#####.#.#.#.###.##.....###.##.#..###.####..#####..#....#..#..##..###..######.###...####..#..#####..##..#.#####...##.#.#..#.##..#.#......#.###.######.###.####...#.##.##..#..#..#####.....#.#....###..#.##......#.....#..#..#..##..#...##.######.####.####.#.#...#.......#..#.#.#...####.##.#......#..#...##.#.##..#...##.#.##..###.#......#.#.......#.#.#.####.###.##...#.....####.#..#..#.##.#....##..#.####....##...##..#...#......#.#.......#.......##..####..#...#.#.#...##..#.#..###..#####........#..####......#..#\n\n#..#.\n#....\n##..#\n..#..\n..###")]
    (is (= (sol2 input) 3351))))

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