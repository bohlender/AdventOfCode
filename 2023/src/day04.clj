(ns day04
  (:require [clojure.set :as set]
            [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.math :refer [pow]]))

(defn parse-numbers [s]
  (->> (re-seq #"\d+" s)
       (into #{} (map #(Integer/parseInt %)))))

(defn parse-line [line]
  (let [[card-part numbers-part] (string/split line #":")
        [winning-numbers-part chosen-numbers-part] (string/split numbers-part #"\|")]
    {:id      (first (parse-numbers card-part))
     :winning (parse-numbers winning-numbers-part)
     :chosen  (parse-numbers chosen-numbers-part)}))

(with-test
  (defn parse [s]
    (->> (string/split-lines s)
         (mapv parse-line)))

  (is (= (parse "Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53\nCard 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19")
         [{:id 1, :winning #{86 48 41 17 83}, :chosen #{86 48 31 6 17 9 83 53}}
          {:id 2, :winning #{20 32 13 61 16}, :chosen #{24 32 61 17 82 19 68 30}}])))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn matching-numbers [card]
  (set/intersection (:winning card) (:chosen card)))

(defn score [card]
  (let [num-matches (count (matching-numbers card))]
    (if (zero? num-matches)
      0
      (->> num-matches dec (pow 2) int))))

(defn sol1 [input]
  (transduce (map score) + input))

(deftest part1-examples
  (let [input (parse "Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53\nCard 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19\nCard 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1\nCard 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83\nCard 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36\nCard 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11")]
    (is (= (sol1 input) 13))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn win-cards [cards]
  """Returns how many instances of each card one has after winning new ones."""
  (let [init-card-counts (into {} (for [card cards] {(:id card) 1}))]
    (reduce (fn [card-counts card]
              (let [cards-till-end (- (count cards) (:id card))
                    relevant-count (min cards-till-end (count (matching-numbers card)))
                    new-card-ids (range (inc (:id card))
                                        (inc (+ (:id card) relevant-count)))
                    card-counts-to-add (into {} (for [id new-card-ids] {id (get card-counts (:id card))}))]
                (merge-with + card-counts card-counts-to-add)))
            init-card-counts
            cards)))

(defn sol2 [input]
  (->> (win-cards input)
       vals
       (reduce +)))

(deftest part2-examples
  (let [input (parse "Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53\nCard 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19\nCard 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1\nCard 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83\nCard 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36\nCard 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11")]
    (is (= (sol2 input) 30))))

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

