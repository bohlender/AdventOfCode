(ns day02
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defrecord Game [id grabs])

(defn parse-game-id [s]
  (->> (re-find #"\d+" s)
       Integer/parseInt))

; "3 blue, 4 red" --> {:blue 3 :red 4}
(defn parse-grab [s]
  (->> (for [[_ count color] (re-seq #"(\d+) (\w+)" s)]
         [(keyword color) (Integer/parseInt count)])
       (into {})))

(defn parse-grabs [s]
  (->> (string/split s #";")
       (map parse-grab)))

(defn parse-game [s]
  (let [[pre-colon post-colon] (string/split s #":")]
    (->Game (parse-game-id pre-colon)
            (parse-grabs post-colon))))

(defn parse [s]
  (->> (string/split-lines s)
       (map parse-game)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn impossible? [game]
  (->> (:grabs game)
       (some (fn [grab] (or
                          (> (get grab :red 0) 12)
                          (> (get grab :green 0) 13)
                          (> (get grab :blue 0) 14))))))

(defn sol1 [input]
  (->> (remove impossible? input)
       (map :id)
       (reduce +)))

(deftest part1-examples
  (let [input (parse "Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green\nGame 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue\nGame 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red\nGame 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red\nGame 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green")]
    (is (= (sol1 input) 8))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn needed-cubes [game]
  (apply merge-with max (:grabs game)))

(defn power [cubes]
  (->> (vals cubes)
       (reduce *)))

(defn sol2 [input]
  (->> input
       (map (comp power needed-cubes))
       (reduce +)))

(deftest part2-examples
  (let [input (parse "Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green\nGame 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue\nGame 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red\nGame 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red\nGame 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green")]
    (sol2 input)))

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
