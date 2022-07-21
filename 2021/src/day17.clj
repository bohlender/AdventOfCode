(ns day17
  (:require [clojure.string :as string]
            [clojure.test :refer :all]
            [clojure.math :refer [signum]]))

(defn parse-target-area [s]
  (let [groups (re-find #"x=(-?\d+)..(-?\d+), y=(-?\d+)..(-?\d+)" s)
        [xmin xmax ymin ymax] (map #(Integer/parseInt %) (rest groups))]
    {:x [xmin xmax]
     :y [ymin ymax]}))

(defn parse-file [filename]
  (->> filename slurp string/split-lines first parse-target-area))

; ==============================================================================
; Part 1
;
; Note:
; - The target is always in the 4th quadrant.
; - The trajectory reaching max height also reaches max speed at the target.
; - There's a speed limit due to discrete stepping => must not "overshoot".
; - Every shot going up goes back down through y=0.
; => aim to reach min y coord of target in one last step, i.e. from y=0
; ==============================================================================
(defn max-yvel0 [target]
  "Determines the initial velocity (in y) of trajectories reaching max height."
  (-> target :y first (* -1) (- 1)))

(defn sol1 [target]
  (let [v (max-yvel0 target)]
    (/ (* v (inc v)) 2)))                                   ;(reduce + (range v 0 -1))

(deftest part1-examples
  (is (-> "target area: x=20..30, y=-10..-5" parse-target-area sol1 (= 45))))

; ==============================================================================
; Part 2
; ==============================================================================
(defrecord State [pos vel])                                 ; maps with :x :y

(defn inc-towards-zero [n]
  (->> n signum (* -1) (+ n) int))

(defn step [state]
  (-> state
      (update :pos (fn [pos] (merge-with + pos (:vel state))))
      (update :vel (fn [vel] (-> vel
                                 (update :x inc-towards-zero)
                                 (update :y dec))))))

(defn may-hit? [state target]
  (and (>= (get-in state [:pos :y]) (apply min (:y target))) ; not below target yet
       (<= (get-in state [:pos :x]) (apply max (:x target))))) ; not left of target yet

(defn- trajectory [state target]
  (->> (iterate step state)
       (take-while #(may-hit? % target))
       (map :pos)))

(defn in-target? [pos {[xmin xmax] :x [ymin ymax] :y :as target}]
  (and (<= xmin (:x pos) xmax)
       (<= ymin (:y pos) ymax)))

(defn hits? [trajectory target]
  (some #(in-target? % target) trajectory))

(defn init-velocities [{[xmin xmax] :x [ymin ymax] :y :as target}]
  (for [yvel (range ymin (inc (max-yvel0 target)))
        xvel (range (inc xmax))
        :when (-> (->State {:x 0 :y 0} {:x xvel :y yvel})
                  (trajectory target)
                  (hits? target))]
    {:x xvel :y yvel}))

(defn sol2 [target]
  (->> target init-velocities count))

(deftest part2-examples
  (is (-> "target area: x=20..30, y=-10..-5" parse-target-area sol2 (= 112))))

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