(ns day06
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

; ==============================================================================
; Part 1
; ==============================================================================
(defn start-of-packet [num-distinct-chars s]
  (->> s
       (partition num-distinct-chars 1)
       (take-while #(not (apply distinct? %)))
       count
       (+ num-distinct-chars)))

(def sol1 (partial start-of-packet 4))

(deftest part1-examples
  (let [input "mjqjpqmgbljsphdztnvjfqwrcgsmlb"]
    (is (= (sol1 input) 7))))

; ==============================================================================
; Part 2
; ==============================================================================
(def sol2 (partial start-of-packet 14))

(deftest part2-examples
  (let [input "mjqjpqmgbljsphdztnvjfqwrcgsmlb"]
    (is (= (sol2 input) 19))))

; ==============================================================================
; Main
; ==============================================================================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (string/trim (slurp filename))]
      (println "First:" (sol1 input))
      (println "Second:" (sol2 input)))))
