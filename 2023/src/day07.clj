(ns day07
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn parse-line [s]
  (let [[hand-str bid-str] (string/split s #"\s")]
    {:hand (mapv (comp keyword str) hand-str)
     :bid  (parse-long bid-str)}))

(defn parse [s]
  (->> (string/split-lines s)
       (mapv parse-line)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(def card-strength
  {:2 2 :3 3 :4 4 :5 5 :6 6 :7 7 :8 8 :9 9 :T 10 :J 11 :Q 12 :K 13 :A 14})

(defn n-of-a-kind? [n hand]
  (->> (frequencies hand)
       (some (fn [[card count]] (= count n)))))

(defn full-house? [hand]
  (->> (frequencies hand)
       vals
       set
       (= #{2 3})))

(defn two-pair? [hand]
  (->> (frequencies hand)
       (filter (fn [[card count]] (= count 2)))
       count
       (= 2)))

(defn one-pair? [hand]
  (->> (frequencies hand)
       (filter (fn [[card count]] (= count 2)))
       count
       (= 1)))

(defn hand-type [hand]
  (cond
    (n-of-a-kind? 5 hand) :five-of-a-kind
    (n-of-a-kind? 4 hand) :four-of-a-kind
    (full-house? hand) :full-house
    (n-of-a-kind? 3 hand) :three-of-a-kind
    (two-pair? hand) :two-pair
    (one-pair? hand) :one-pair
    :else :high-card))

(def type-strength
  {:high-card 0 :one-pair 1 :two-pair 2 :three-of-a-kind 3 :full-house 4 :four-of-a-kind 5 :five-of-a-kind 6})

(defn compare-hand [card-strength hand-type lhs rhs]
  (let [type-cmp-res (compare (type-strength (hand-type lhs))
                              (type-strength (hand-type rhs)))]
    (if (zero? type-cmp-res)
      (compare (mapv card-strength lhs) (mapv card-strength rhs))
      type-cmp-res)))

(defn rank [card-strength hand-type hands]
  (->> hands
       (sort (partial compare-hand card-strength hand-type))
       (map-indexed (fn [i hand] {hand (inc i)}))
       (into {})))

(defn sol1 [input]
  (let [hands (mapv :hand input)
        ranks (rank card-strength hand-type hands)]
    (->> (for [{:keys [hand bid]} input]
           (* bid (ranks hand)))
         (reduce +))))

(deftest part1-examples
  (let [input (parse "32T3K 765\nT55J5 684\nKK677 28\nKTJJT 220\nQQQJA 483")]
    (is (= (sol1 input) 6440))))

; ==============================================================================
; Part 2
; ==============================================================================
(def card-strength-with-joker
  (assoc card-strength :J 1))

(def cards-without-joker
  (remove #{:J} (keys card-strength)))

(defn replace-jokers [hands]
  (lazy-seq (when-first [hand hands]
     (let [joker-idx (.indexOf hand :J)]
       (if (neg? joker-idx)
         (cons hand (replace-jokers (rest hands)))
         (replace-jokers (concat (for [c cards-without-joker] (assoc hand joker-idx c))
                                 (rest hands))))))))

(defn hand-type-with-joker [hand]
  (->> (replace-jokers [hand]) ; TODO: Determine best achievable hand type without brute force
       (into #{} (map hand-type))
       (apply (partial max-key type-strength))))

(defn sol2 [input]
  (let [hands (mapv :hand input)
        ranks (rank card-strength-with-joker hand-type-with-joker hands)]
    (->> (for [{:keys [hand bid]} input]
           (* bid (ranks hand)))
         (reduce +))))

(deftest part2-examples
  (let [input (parse "32T3K 765\nT55J5 684\nKK677 28\nKTJJT 220\nQQQJA 483")]
    (is (= (sol2 input) 5905))))

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
