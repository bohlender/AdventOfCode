(ns day04
  (:require [clojure.string :as string])
  (:use clojure.test))

; input = comma-separated numbers, paragraph, paragraph-separated 5x5 boards
(defn parse-csv [s]
  (map #(Integer/parseInt %) (string/split s #",")))

(defn parse-board-line [s]
  (mapv #(Integer/parseInt %) (re-seq #"\d+" s)))

(defn parse-board [s]
  (let [lines (string/split-lines s)]
    (mapv parse-board-line lines)))

(defn parse [s]
  (let [[nums-str & boards-str]  (string/split s #"\n\n")
        nums (parse-csv nums-str)
        boards (map parse-board boards-str)]
    [nums boards]))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn all-marked? [nums marked-nums]
  (assert (set? marked-nums))
  (every? #(contains? marked-nums %) nums))

(defn get-cols [board]
  (for [x (range (count (nth board 0)))]
    (for [row board]
      (nth row x))))

(defn win? [board marked-nums]
  (let [rows board
        cols (get-cols board)]
    (or (some #(all-marked? % marked-nums) rows)
        (some #(all-marked? % marked-nums) cols))))

(defn sum-unmarked [board marked-nums]
  (->> (flatten board)
       (remove #(contains? marked-nums %))
       (reduce +)))

(defn sol1
  ([[nums boards]]
   (sol1 [nums boards] #{}))

  ([[nums boards] marked-nums]
   (let [[x & xs] nums
         new-marked-nums (conj marked-nums x)
         winner-board (first (filter #(win? % new-marked-nums) boards))]
     (if winner-board
       (* (sum-unmarked winner-board new-marked-nums) x)
       (recur [xs boards] new-marked-nums)))))

(deftest part1-examples
  (let [board (parse-board "14 21 17 24  4\n10 16 15  9 19\n18  8 23 26 20\n22 11 13  6  5\n 2  0 12  3  7")]
    (is (not (win? board #{7 4 9 5 11 17 23 2 0 14 21})))
    (is (win? board #{7 4 9 5 11 17 23 2 0 14 21 24}))
    (is (= 188 (sum-unmarked board #{7 4 9 5 11 17 23 2 0 14 21 24}))))
    (is (= 4512 (sol1 (parse "7,4,9,5,11,17,23,2,0,14,21,24,10,16,13,6,15,25,12,22,18,20,8,19,3,26,1\n\n22 13 17 11  0\n 8  2 23  4 24\n21  9 14 16  7\n 6 10  3 18  5\n 1 12 20 15 19\n\n 3 15  0  2 22\n 9 18 13 17  5\n19  8  7 25 23\n20 11 10 24  4\n14 21 16 12  6\n\n14 21 17 24  4\n10 16 15  9 19\n18  8 23 26 20\n22 11 13  6  5\n 2  0 12  3  7")))))

; ===================
; Part 2
; ===================

(defn sol2
  ([[nums boards]]
   (sol2 [nums boards] #{}))

  ([[nums boards] marked-nums]
   (let [[x & xs] nums
         new-marked-nums (conj marked-nums x)
         loser-boards (remove #(win? % new-marked-nums) boards)]
     (if (empty? loser-boards)
       (* (sum-unmarked (nth boards 0) new-marked-nums) x)
       (recur [xs loser-boards] new-marked-nums)))))

(deftest part2-examples
    (is (= 1924 (sol2 (parse "7,4,9,5,11,17,23,2,0,14,21,24,10,16,13,6,15,25,12,22,18,20,8,19,3,26,1\n\n22 13 17 11  0\n 8  2 23  4 24\n21  9 14 16  7\n 6 10  3 18  5\n 1 12 20 15 19\n\n 3 15  0  2 22\n 9 18 13 17  5\n19  8  7 25 23\n20 11 10 24  4\n14 21 16 12  6\n\n14 21 17 24  4\n10 16 15  9 19\n18  8 23 26 20\n22 11 13  6  5\n 2  0 12  3  7")))))

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
