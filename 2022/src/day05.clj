(ns day05
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn re-pos [re s]
  "Returns a map from positions to matching groups."
  (loop [m (re-matcher re s)
         res {}]
    (if (.find m)
      (recur m (assoc res (.start m) (.group m)))
      res)))

(defn parse-stack-line [line]
  (-> (re-pos #"\w" line)
      (update-keys #(inc (unchecked-divide-int (dec %) 4))) ; string index -> stack pos
      (update-vals first)))                                 ; string -> char

(defn parse-stacks [s]
  (->> (string/split-lines s)
       reverse                                              ; start with bottom
       (drop 1)                                             ; ignore index line
       (mapcat parse-stack-line)
       (map (fn [[k v]] {k (list v)}))                      ; {1 \N} {2 \C} -> {1 '(\N)} {2 '(\C)}
       (apply merge-with into)))

(defn parse-move [s]
  (->> (re-seq #"\d+" s)
       (map #(Integer/parseInt %))))

(defn parse-moves [s]
  (->> (string/split-lines s)
       (map parse-move)))

(defn parse [s]
  (let [[stacks-str moves-str] (string/split s #"\n\n")]
    {:stacks (parse-stacks stacks-str)
     :moves  (parse-moves moves-str)}))

; ==============================================================================
; Part 1
; ==============================================================================
(defn crate-mover-9000 [crates] crates)

(defn step [crate-mover stacks [n from to]]
  (let [crates (->> (get stacks from) (take n) crate-mover)]
    (-> stacks
        (update from #(drop n %))
        (update to #(into % crates)))))

(defn top-crates [stacks]
  (->> (sort stacks)
       (map (fn [[i stack]] (first stack)))
       (apply str)))

(defn sol1 [input]
  (->> (reduce (partial step crate-mover-9000)
               (:stacks input)
               (:moves input))
       top-crates))

(deftest part1-examples
  (let [input (parse "    [D]    \n[N] [C]    \n[Z] [M] [P]\n 1   2   3 \n\nmove 1 from 2 to 1\nmove 3 from 1 to 3\nmove 2 from 2 to 1\nmove 1 from 1 to 2\n")]
    (is (= (sol1 input) "CMZ"))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn crate-mover-9001 [crates] (reverse crates))

(defn sol2 [input]
  (->> (reduce (partial step crate-mover-9001)
               (:stacks input)
               (:moves input))
       top-crates))

(deftest part2-examples
  (let [input (parse "    [D]    \n[N] [C]    \n[Z] [M] [P]\n 1   2   3 \n\nmove 1 from 2 to 1\nmove 3 from 1 to 3\nmove 2 from 2 to 1\nmove 1 from 1 to 2\n")]
    (is (= (sol2 input) "MCD"))))

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
