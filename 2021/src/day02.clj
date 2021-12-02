(ns day02 (:use clojure.test))

(defn parse [s]
  (->> s
       clojure.string/split-lines
       (map #(clojure.string/split % #" "))
       (map (fn [[dir val]] [(keyword dir) (Integer/parseInt val)]))))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn move [state action]
  (let [[pos depth] state
        [dir x] action]
    (case dir
      :forward [(+ pos x) depth]
      :down [pos (+ depth x)]
      :up [pos (- depth x)])))

(defn steer [pos course transition-fun]
  (reduce transition-fun pos course))

(defn sol1 [course]
  (let [[pos depth] (steer [0 0] course move)]
    (* pos depth)))

(deftest part1-example
  (is (= 150 (sol1 [[:forward 5] [:down 5] [:forward 8] [:up 3] [:down 8] [:forward 2]]))))

; ===================
; Part 2
; ===================
(defn move-with-aim [state action]
  (let [[pos depth aim] state
        [dir x] action]
    (case dir
      :forward [(+ pos x) (+ depth (* aim x)) aim]
      :down [pos depth (+ aim x)]
      :up [pos depth (- aim x)])))

(defn sol2 [course]
  (let [[x depth] (steer [0 0 0] course move-with-aim)]
    (* x depth)))

(deftest part2-example
  (is (= 900 (sol2 [[:forward 5] [:down 5] [:forward 8] [:up 3] [:down 8] [:forward 2]]))))

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