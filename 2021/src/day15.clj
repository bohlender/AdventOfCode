(ns day15
  (:require [clojure.string :as string]
            [clojure.test :refer :all]
            [clojure.data.priority-map :refer [priority-map]]))

(defn parse-line [line]
  "Parses digit characters as a vector of integers."
  (mapv #(- (int %) (int \0)) line))

(defn parse [s]
  "Parses lines of digit characters as 2d-array (row-major)."
  (let [lines (string/split-lines s)]
    (mapv parse-line lines)))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn height [risk-map]
  (count risk-map))

(defn width [risk-map]
  (count (nth risk-map 0)))

(defn neighbours [risk-map [y x]]
  (letfn [(in-bounds? [[y x]]
            (and (<= 0 x) (< x (width risk-map))
                 (<= 0 y) (< y (height risk-map))))]
    (->> [[y (inc x)]
          [y (dec x)]
          [(inc y) x]
          [(dec y) x]]
         (filter in-bounds?))))

(defn second-< [lhs rhs]
  "Comparator that sorts by the second element."
  (compare [(second lhs) lhs] [(second rhs) rhs]))

; TODO: What if target is unreachable?
(defn min-risk [risk-map from to]
  (loop [worklist (priority-map from 0)        ; [elem risk] pairs
         visited #{}]
    (let [[cur cur-risk] (peek worklist)]
      (if (= cur to)
        cur-risk
        (let [worklist (pop worklist)
              visited (conj visited cur)
              next-worklist (->> cur
                                 (neighbours risk-map)
                                 (remove visited)
                                 (reduce (fn [res yx] (conj res [yx (+ cur-risk (get-in risk-map yx))]))
                                         worklist))]
          (recur next-worklist visited))))))

(defn sol1 [input]
  (let [upper-left [0 0]
        bottom-right [(dec (height input)) (dec (width input))]]
    (min-risk input upper-left bottom-right)))

(deftest part1-examples
  (let [input (parse "1163751742\n1381373672\n2136511328\n3694931569\n7463417111\n1319128137\n1359912421\n3125421639\n1293138521\n2311944581")]
    (is (= (sol1 input) 40))))

; ===================
; Part 2
; ===================
(defn inc-risk [risk]
  (-> (mod risk 9)
      inc))

(defn mult-risk-map [risk-map n]
  (letfn [(gen [vals] (iterate #(mapv inc-risk %) vals))]
    ; mult width
    (let [wide-input (map #(->> % gen (take n) flatten vec)
                          risk-map)]
      ; mult height
      (vec (apply mapcat vector (map #(->> % gen (take n))
                                     wide-input))))))

(defn sol2 [input]
  (-> (mult-risk-map input 5)
      sol1))

(deftest part2-examples
  (let [input (parse "1163751742\n1381373672\n2136511328\n3694931569\n7463417111\n1319128137\n1359912421\n3125421639\n1293138521\n2311944581")]
    (is (= (sol2 input) 315))))

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