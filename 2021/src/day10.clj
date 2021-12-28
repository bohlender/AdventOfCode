(ns day10
  (:require [clojure.string :as string])
  (:use clojure.set)
  (:use clojure.test))

(defn parse [s]
  (string/split-lines s))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn opens-chunk? [c]
  (contains? #{\( \[ \{ \<} c))

(defn get-matching [c]
  (case c
    \( \)
    \[ \]
    \{ \}
    \< \>))

(defn closes-chunk? [stack c]
  (= c (get-matching (first stack))))

; TODO use reduce
(defn consume-legal
  "Returns the remaining line and character stack of open chunks at the first illegal character."
  ([line]
   (consume-legal line '()))
  ([line stack]
   (let [[c & cs] line]
     (if (opens-chunk? c)
       (recur cs (conj stack c))
       (if (closes-chunk? stack c)
         (recur cs (rest stack))
         [line stack])))))

(defn error-score [c]
  (case c
    \) 3
    \] 57
    \} 1197
    \> 25137))

(defn sol1 [lines]
  (->> lines
       (map consume-legal)
       (map first)
       (filter boolean)
       (map first)
       (map error-score)
       (reduce +)))

(deftest part1-examples
  (is (= 26397 (sol1 (parse "[({(<(())[]>[[{[]{<()<>>\n[(()[<>])]({[<{<<[]>>(\n{([(<{}[<>[]}>{[]{[(<()>\n(((({<>}<{<{<>}{[]{[]{}\n[[<[([]))<([[{}[[()]]]\n[{[{({}]{}}([{[{{{}}([]\n{<[[]]>}<{[{[{[]{()[[[]\n[<(<(<(<{}))><([]([]()\n<{([([[(<>()){}]>(<<{{\n<{([{{}}[<[[[<>{}]]]>[]]")))))

; ===================
; Part 2
; ===================
(defn complete [stack]
  (->> stack
       (map get-matching)
       (apply str)))

(defn complete-score [c]
  (case c
    \) 1
    \] 2
    \} 3
    \> 4))

(defn completion-string-score [s]
  (->> s
       (map complete-score)
       (reduce (fn [res v] (+ (* res 5) v)) 0)))

(defn get-middle [coll]
  (let [mid-idx (/ (count coll) 2)]
    (nth coll mid-idx)))

(defn sol2 [lines]
  (->> lines
       (map consume-legal)
       (remove #(first %))
       (map second)
       (map complete)
       (map completion-string-score)
       sort
       get-middle
       ))

(deftest part2-examples
  (is (= 288957 (sol2 (parse "[({(<(())[]>[[{[]{<()<>>\n[(()[<>])]({[<{<<[]>>(\n{([(<{}[<>[]}>{[]{[(<()>\n(((({<>}<{<{<>}{[]{[]{}\n[[<[([]))<([[{}[[()]]]\n[{[{({}]{}}([{[{{{}}([]\n{<[[]]>}<{[{[{[]{()[[[]\n[<(<(<(<{}))><([]([]()\n<{([([[(<>()){}]>(<<{{\n<{([{{}}[<[[[<>{}]]]>[]]")))))

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
