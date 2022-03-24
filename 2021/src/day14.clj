(ns day14
  (:require [clojure.string :as string]
            [clojure.test :refer :all]))

(defn parse [s]
  (let [lines (string/split-lines s)
        template (first lines)
        rules (->> (subvec lines 2)
                   (map (comp (fn [[_ ab c]] [ab c])
                              #(re-matches #"(\w+) -> (\w+)" %)))
                   (into {}))]
    {:template template, :rules rules}))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn pair-freqs [s]
  (->> s (partition 2 1) (map #(apply str %)) frequencies))

(defn step [rules pair-freqs]
  (reduce (fn [acc [[a b :as ab] n]]
            (if-some [c (rules ab)]
              (-> acc
                  (update (str a c) (fnil #(+ n %) 0))
                  (update (str c b) (fnil #(+ n %) 0)))
              (assoc acc ab n)))
          {}
          pair-freqs))

(defn pair-freqs->char-freqs [template pair-freqs]
  (->> (reduce (fn [acc [[a _] n]] (update acc a (fnil #(+ n %) 0)))
               {(last template) 1}
               pair-freqs)))

(defn score [{template :template, rules :rules} steps]
  (let [init-pair-freqs (pair-freqs template)
        it              (iterate #(step rules %) init-pair-freqs)
        char-freqs      (pair-freqs->char-freqs template (nth it steps))]
    (- (apply max (vals char-freqs))
       (apply min (vals char-freqs)))))

(defn sol1 [input]
  (score input 10))

(deftest part1-examples
  (let [input (parse "NNCB\n\nCH -> B\nHH -> N\nCB -> H\nNH -> C\nHB -> C\nHC -> B\nHN -> C\nNN -> C\nBH -> H\nNC -> B\nNB -> B\nBN -> B\nBB -> N\nBC -> B\nCC -> N\nCN -> C")
        it    (iterate #(step (:rules input) %) (pair-freqs (:template input)))]
    (is (= (nth it 1) (pair-freqs "NCNBCHB")))
    (is (= (nth it 2) (pair-freqs "NBCCNBBBCBHCB")))
    (is (= (nth it 3) (pair-freqs "NBBBCNCCNBBNBNBBCHBHHBCHB")))
    (is (= (nth it 4) (pair-freqs "NBBNBNBBCCNBCNCCNBBNBBNBBBNBBNBBCBHCBHHNHCBBCBHCB")))
    (is (= (sol1 input) 1588))))

; ===================
; Part 2
; ===================
(defn sol2 [input]
  (score input 40))

(deftest part2-examples
  (let [s "NNCB\n\nCH -> B\nHH -> N\nCB -> H\nNH -> C\nHB -> C\nHC -> B\nHN -> C\nNN -> C\nBH -> H\nNC -> B\nNB -> B\nBN -> B\nBB -> N\nBC -> B\nCC -> N\nCN -> C"
        input (parse s)]
    (is (= (sol2 input) 2188189693529))))

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
