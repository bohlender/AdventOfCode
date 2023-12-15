(ns day15
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn parse [s]
  (-> (string/trim s)
      (string/split #",")))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn HASH [s]
  (->> s
       (reduce (fn [acc ch] (-> (int ch) (+ acc) (* 17) (mod 256)))
               0)))

(defn sol1 [input]
  (->> (map HASH input)
       (reduce +)))

(deftest part1-examples
  (is (= 52 (HASH "HASH")))
  (is (= 1320 (sol1 (parse "rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7\n")))))

; ==============================================================================
; Part 2
; ==============================================================================
(defrecord Step [label op focal-length])
(defrecord Lens [label focal-length])

(defn parse-step [s]
  (let [[_ label op focal-length] (re-matches #"(\w+)(=|-)(\d?)" s)]
    (->Step label (first op) (when focal-length (parse-long focal-length)))))

(defn remove-lens [box label]
  (->> box
       (filterv (fn [lens] (not= (:label lens) label)))))

(defn lens-idx [box label]
  (->> box
       (keep-indexed (fn [idx lens] (when (= (:label lens) label) idx)))
       first))

(defn add-lens [box lens]
  (if-some [idx (lens-idx box (:label lens))]
    (assoc box idx lens)
    (conj box lens)))

(defn HASHMAP [input]
  (->> (map parse-step input)
       (reduce (fn [boxes {:keys [label op focal-length] :as step}]
                 (let [box-idx (HASH label)]
                   (case op
                     \- (update boxes box-idx remove-lens label)
                     \= (update boxes box-idx add-lens (->Lens label focal-length)))))
               (vec (repeat 256 [])))))

(defn enumerate [coll]
  (map-indexed vector coll))

(defn sol2 [input]
  (let [m (HASHMAP input)]
    (->> (for [[box-idx box] (enumerate m)
               [slot-idx lens] (enumerate box)]
           (* (inc box-idx)
              (inc slot-idx)
              (:focal-length lens)))
         (reduce +))))

(deftest part2-examples
  (is (= 145 (sol2 (parse "rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7\n")))))

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
