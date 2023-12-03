(ns day03
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn enumerate [coll]
  (map-indexed vector coll))

(with-test
  (defn re-pos [re s]
    (loop [matcher (re-matcher re s)
           res []]
      (if (.find matcher)
        (recur matcher (conj res [(.group matcher) (.start matcher) (.end matcher)]))
        res)))

  (is (= (re-pos #"\d+|[^\d\.]" ".....+.58.")
         [["+" 5 6] ["58" 7 9]])))

(def num-or-symbol-re #"\d+|[^\d\.]")

(defn parse [s]
  (let [lines (string/split-lines s)]
    (for [[y line] (enumerate lines)
          [group x-from x-to] (re-pos num-or-symbol-re line)]
      (->> (if (Character/isDigit ^char (first group))
             {:type  :number
              :value (Integer/parseInt group)}
             {:type  :symbol
              :value group})
           (merge {:y y
                   :x [x-from x-to]})))))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
; TODO: O(n) --> O(log n) or O(1)
(defn get-element [elements [x y]]
  (->> elements
       (filter (fn [e] (and (= (:y e) y)
                            (<= (get-in e [:x 0]) x (dec (get-in e [:x 1]))))))
       first))

(defn adjacent-coords [y x-from x-to]
  (for [cur-y (range (dec y) (+ y 2))
        cur-x (range (dec x-from) (inc x-to))
        :when (not (and (= cur-y y) (<= x-from cur-x (dec x-to))))]
    [cur-x cur-y]))

(defn adjacent-elements [elements element]
  (->> (adjacent-coords (:y element)
                        (get-in element [:x 0])
                        (get-in element [:x 1]))
       (keep (partial get-element elements))
       (into #{})))

(defn part-number? [elements element]
  (->> (adjacent-elements elements element)
       (filter (fn [e] (= (:type e) :symbol)))
       first))

(defn sol1 [input]
  (->> input
       (transduce (comp (filter (partial part-number? input))
                        (map :value))
                  +)))

(deftest part1-examples
  (let [input (parse "467..114..\n...*......\n..35..633.\n......#...\n617*......\n.....+.58.\n..592.....\n......755.\n...$.*....\n.664.598..")]
    (is (= (sol1 input) 4361))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn gear-ratio [elements element]
  (let [adj-part-numbers (->> (adjacent-elements elements element)
                              (filter (fn [e] (= (:type e) :number))))]
    (when (= (count adj-part-numbers) 2)
      (->> (map :value adj-part-numbers)
           (reduce *)))))

(defn sol2 [input]
  (->> input
       (transduce (comp (filter (fn [e] (and (= (:type e) :symbol))))
                        (keep (partial gear-ratio input)))
                  +)))

(deftest part2-examples
  (let [input (parse "467..114..\n...*......\n..35..633.\n......#...\n617*......\n.....+.58.\n..592.....\n......755.\n...$.*....\n.664.598..")]
    (is (= (sol2 input) 467835))))

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
