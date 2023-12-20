(ns day18
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn parse-line [s]
  (let [[_ dir len color] (re-matches #"(\w) (\d+) \(#(.+)\)" s)]
    {:dir   (keyword dir)
     :len   (parse-long len)
     :color color}))

(defn parse [s]
  (->> (string/split-lines s)
       (map parse-line)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defrecord Vec2 [x y])

(defn plan-entry->vec2 [{:keys [dir len]}]
  (case dir
    :R (->Vec2 len 0)
    :L (->Vec2 (- len) 0)
    :U (->Vec2 0 len)
    :D (->Vec2 0 (- len))))

(defn trench-vertices [input]
  (->> input
       (reduce (fn [coords plan-entry]
                 (->> (plan-entry->vec2 plan-entry)
                      (merge-with + (first coords))
                      (conj coords)))
               (list (->Vec2 0 0)))
       drop-last))

(defn shoelace [vertices]
  (->> vertices
       (partition 2 1 [(first vertices)])
       (map (fn [[lhs rhs]] (* (- (:x lhs) (:x rhs))
                               (+ (:y lhs) (:y rhs)))))
       (reduce +)
       (* 0.5)
       abs
       long))

(defn manhattan-dist [from to]
  (+ (abs (- (:x to) (:x from)))
     (abs (- (:y to) (:y from)))))

(defn circumference [vertices]
  (->> vertices
       (partition 2 1 [(first vertices)])
       (map (fn [[lhs rhs]] (manhattan-dist lhs rhs)))
       (reduce +)))

(defn interior [vertices]
  (- (shoelace vertices)
     (/ (circumference vertices) 2)
     -1))

(defn sol1 [input]
  (let [vertices (trench-vertices input)]
    (+ (interior vertices)
       (circumference vertices))))

(deftest part1-examples
  (is (= 62 (sol1 (parse "R 6 (#70c710)\nD 5 (#0dc571)\nL 2 (#5713f0)\nD 2 (#d2c081)\nR 2 (#59c680)\nD 2 (#411b91)\nL 5 (#8ceee2)\nU 2 (#caa173)\nL 1 (#1b58a2)\nU 2 (#caa171)\nR 2 (#7807d2)\nU 3 (#a77fa3)\nL 2 (#015232)\nU 2 (#7a21e3)\n")))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn fix-plan-entry [plan-entry]
  (let [[len dir] (->> (:color plan-entry)
                       (split-at 5)
                       (map (partial apply str)))]
    {:dir (get {"0" :R, "1" :D, "2" :L, "3" :U} dir)
     :len (Long/parseLong len 16)}))

(defn sol2 [input]
  (->> (map fix-plan-entry input)
       sol1))

(deftest part2-examples
  (is (= 952408144115 (sol2 (parse "R 6 (#70c710)\nD 5 (#0dc571)\nL 2 (#5713f0)\nD 2 (#d2c081)\nR 2 (#59c680)\nD 2 (#411b91)\nL 5 (#8ceee2)\nU 2 (#caa173)\nL 1 (#1b58a2)\nU 2 (#caa171)\nR 2 (#7807d2)\nU 3 (#a77fa3)\nL 2 (#015232)\nU 2 (#7a21e3)\n")))))

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
