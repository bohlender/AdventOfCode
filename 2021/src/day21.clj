(ns day21
  (:require [clojure.string :as string]
            [clojure.test :refer :all]))

(defn parse-starting-pos [line]
  (->> line
       (re-seq #"\d")
       (mapv #(Integer/parseInt %))))

(defn parse [s]
  (->> s
       string/split-lines
       (map parse-starting-pos)
       (into {})))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(def deterministic-die-rolls (cycle (range 1 101)))

(defrecord State [positions scores turn])
(defn init-state [positions]
  (->State positions {1 0, 2 0} 1))

(defn winner [state target]
  "Returns the player reaching the target score if there is one -- nil otherwise."
  (->> (:scores state)
       (filter (fn [[player score]] (>= score target)))
       ffirst))

(defn other [player]
  "Returns the number of the other player."
  (get {1 2, 2 1} player))

(defn advance [from roll-sum]
  "Returns the position a player ends up at when starting at 'from' and moving 'roll-sum' fields."
  (-> (dec from)                                            ; [1, 10] -> [0, 9]
      (+ roll-sum)
      (mod 10)
      inc))                                                 ; [0,9] -> [1, 10]

(defn successor [state roll-sum]
  "Returns successor state from advancing the current player by 'roll-sum' fields."
  (let [cur-player (:turn state)
        new-pos (advance (get-in state [:positions cur-player]) roll-sum)]
    (-> state
        (assoc-in [:positions cur-player] new-pos)
        (update-in [:scores cur-player] + new-pos)
        (update :turn other))))

(defn sol1 [input]
  (loop [state (init-state input)
         roll-seq deterministic-die-rolls
         roll-count 0]
    (if-some [player (winner state 1000)]
      (* (get-in state [:scores (other player)])
         roll-count)
      (let [roll-sum (->> roll-seq (take 3) (reduce +))]
        (recur (successor state roll-sum)
               (nthrest roll-seq 3)
               (+ roll-count 3))))))

(deftest part1-examples
  (let [input (parse "Player 1 starting position: 4\nPlayer 2 starting position: 8")]
    (is (= (sol1 input) 739785))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn histogram [coll]
  (reduce (fn [m v] (update m v (fnil inc 0)))
          {}
          coll))

(def roll-sum-histogram
  "The histogram of all possible 3-roll-sums when using a dirac die."
  (->> (for [x [1 2 3]
             y [1 2 3]
             z [1 2 3]]
         (+ x y z))
       histogram))

(def outcomes
  "Determines how many games each player can win starting from the given state. Returns a map."
  (memoize (fn [state]
             (if-some [player (winner state 21)]
               {player 1, (other player) 0}
               (->> (for [[roll-sum times] roll-sum-histogram]
                      (let [succ (successor state roll-sum)]
                        (update-vals (outcomes succ) #(* % times))))
                    (apply merge-with +))))))

(defn sol2 [input]
  (->> (init-state input)
       outcomes
       vals
       (apply max)))

(deftest part2-examples
  (let [input (parse "Player 1 starting position: 4\nPlayer 2 starting position: 8")
        init (init-state input)]
    (is (= (outcomes init) {1 444356092776315, 2 341960390180808}))
    (is (= (sol2 input) 444356092776315))))

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