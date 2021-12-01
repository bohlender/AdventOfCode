(ns day01
  (:use clojure.test))

(defn parse [s]
  (->> s
       clojure.string/split-lines
       (map #(Integer/parseInt %))))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn count-incr [coll]
  (->> coll
       (partition 2 1)
       (filter (fn [[a b]] (< a b)))
       count))

(def sol1 count-incr)

(deftest part1-examples
  (is (= 7 (sol1 [199 200 208 210 200 207 240 269 260 263]))))

; ===================
; Part 2
; ===================
(def sum #(reduce + %))

(defn sol2 [coll]
  (->> coll
       (partition 3 1)
       (map sum)
       count-incr))

(deftest part2-examples
  (is (= 5 (sol2 [199 200 208 210 200 207 240 269 260 263]))))

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
