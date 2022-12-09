(ns day08
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn enumerate [coll]
  (map-indexed list coll))

(defn parse [s]
  (let [lines (string/split-lines s)]
    {:grid (->> (for [[y line] (enumerate lines)
                      [x ch] (enumerate line)]
                  {{:y y :x x} (Character/digit ch 10)})
                (into {}))
     :size (count lines)}))

; ==============================================================================
; Part 1
; ==============================================================================
(defn row [{:keys [grid size]} y]
  (for [x (range size)
        :let [coord {:y y, :x x}]]
    [coord (grid coord)]))

(defn rows [input]
  (->> (range (:size input))
       (map (partial row input))))

(defn col [{:keys [grid size]} x]
  (for [y (range size)
        :let [coord {:y y, :x x}]]
    [coord (grid coord)]))

(defn cols [input]
  (->> (range (:size input))
       (map (partial col input))))

(defn visible-in-line [entry-seq]
  "Returns the coords of visible trees, for a seq of (coord, height) pairs."
  (->> entry-seq
       (reduce (fn [{:keys [visible-coords max-height]} [coord height]]
                 {:visible-coords (if (< max-height height)
                                    (conj visible-coords coord)
                                    visible-coords)
                  :max-height     (max max-height height)})
               {:visible-coords #{}
                :max-height     -1})
       :visible-coords))

(defn visible [input]
  (let [lr-seqs (rows input)
        rl-seqs (map reverse lr-seqs)
        ub-seqs (cols input)
        bu-seqs (map reverse ub-seqs)]
    (->> (concat lr-seqs rl-seqs ub-seqs bu-seqs)
         (mapcat visible-in-line)
         set)))

(defn sol1 [input]
  (count (visible input)))

(deftest part1-examples
  (let [input (parse "30373\n25512\n65332\n33549\n35390\n")]
    (is (= (sol1 input) 21))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn viewing-distance [house-height entry-seq]
  (let [[lower not-lower] (->> entry-seq
                               (split-with (fn [[coord height]] (< height house-height))))]
    (+ (count lower)
       (count (take 1 not-lower)))))

(defn scenic-score [input {:keys [x y] :as coord} ]
  (let [house-height (get-in input [:grid coord])
        row (row input y)
        col (col input x)
        lr-seq (drop (inc x) row)
        rl-seq (reverse (take x row))
        ub-seq (drop (inc y) col)
        bu-seq (reverse (take y col))]
    (->> [lr-seq rl-seq ub-seq bu-seq]
         (map (partial viewing-distance house-height))
         (reduce *))))

(defn sol2 [input]
  (->> (:grid input)
       keys
       (map (partial scenic-score input))
       (apply max)))

(deftest part2-examples
  (let [input (parse "30373\n25512\n65332\n33549\n35390\n")]
    (is (= (sol2 input) 8))))

; ==============================================================================
; Main
; ==============================================================================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (parse (slurp filename))]
      (println "First:" (sol1 input))
      (println "Second:" (sol2 input)))))
