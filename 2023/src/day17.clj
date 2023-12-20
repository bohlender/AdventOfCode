(ns day17
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.data.priority-map :refer [priority-map-keyfn]]))

(defrecord Vec2 [x y])
(defrecord Grid [width height cells])

(defn enumerate [coll]
  (map-indexed vector coll))

(defn parse [s]
  (let [lines (string/split-lines s)
        width (count (first lines))
        height (count lines)
        cells (into {} (for [[y line] (enumerate lines)
                             [x ch] (enumerate line)]
                         {(->Vec2 x y) (parse-long (str ch))}))]
    (->Grid width height cells)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defrecord DirInfo [last-dir same-dir-count])
(defrecord State [pos dir-info])

(defn mk-state [pos]
  (->State pos nil))

(defn add-vec2 [lhs rhs]
  (merge-with + lhs rhs))

(def dir->Vec2
  {:north (->Vec2 0 -1)
   :east  (->Vec2 1 0)
   :south (->Vec2 0 1)
   :west  (->Vec2 -1 0)})

(def dirs (keys dir->Vec2))

(def opposite-dir {:north :south, :east :west, :south :north, :west :east})

(defn move [state dir]
  (let [moved-state (update state :pos add-vec2 (dir->Vec2 dir))]
    (if (= dir (get-in moved-state [:dir-info :last-dir]))
      (update-in moved-state [:dir-info :same-dir-count] inc)
      (assoc moved-state :dir-info (->DirInfo dir 1)))))

(defn successors-part1 [grid state]
  (->> dirs
       (remove (fn [dir] (= dir (opposite-dir (get-in state [:dir-info :last-dir])))))
       (map (partial move state))
       (filter (fn [s] (and (contains? (:cells grid) (:pos s))
                            (<= (get-in s [:dir-info :same-dir-count]) 3))))))

(defn manhattan-dist [from to]
  (+ (abs (- (:x to) (:x from)))
     (abs (- (:y to) (:y from)))))

(defn approx-cost [pos cost-so-far goal]
  (-> (manhattan-dist pos goal)
      (+ cost-so-far)))

; FIXME: Super slow
(defn min-cost [{:keys [width height cells] :as grid} successors-fn]
  (let [goal-pos (->Vec2 (dec width) (dec height))
        init-state (mk-state (->Vec2 0 0))]
    (loop [pqueue (priority-map-keyfn :approx init-state {:so-far 0, :approx (approx-cost (:pos init-state) 0 goal-pos)})
           visited (transient #{})]
      (let [[state cost :as cur] (peek pqueue)]
        (if (= (:pos state) goal-pos)
          (:so-far cost)
          (let [succs (->> (successors-fn grid state)
                           (remove (fn [s] (or (contains? (pop pqueue) s)
                                               (contains? visited s)))))
                succs-w-cost (for [s succs]
                               (let [cost-to-succ (+ (:so-far cost) (get cells (:pos s)))]
                                 {s {:so-far cost-to-succ
                                     :approx (approx-cost (:pos s) cost-to-succ goal-pos)}}))]
            (recur (into (pop pqueue) succs-w-cost)
                   (conj! visited (assoc-in state [:dir-info :same-dir-count] nil)))))))))

(defn sol1 [input]
  (min-cost input successors-part1))

(deftest part1-examples
  (let [input (parse "2413432311323\n3215453535623\n3255245654254\n3446585845452\n4546657867536\n1438598798454\n4457876987766\n3637877979653\n4654967986887\n4564679986453\n1224686865563\n2546548887735\n4322674655533\n")]
    (is (= 102 (sol1 input)))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn successors-part2 [grid state]
  (if (nil? (:dir-info state))
    (->> dirs
         (map (partial move state))
         (filter (fn [s] (contains? (:cells grid) (:pos s)))))

    (let [last-dir (get-in state [:dir-info :last-dir])]
      (->> (if (< (get-in state [:dir-info :same-dir-count]) 4)
             [last-dir]
             dirs)
           (remove (fn [dir] (= dir (opposite-dir last-dir))))
           (map (partial move state))
           (filter (fn [s] (and (contains? (:cells grid) (:pos s))
                                (<= (get-in s [:dir-info :same-dir-count]) 10))))))))

(defn sol2 [input]
  (min-cost input successors-part2))

(deftest part2-examples
  (is (= 94 (sol2 (parse "2413432311323\n3215453535623\n3255245654254\n3446585845452\n4546657867536\n1438598798454\n4457876987766\n3637877979653\n4654967986887\n4564679986453\n1224686865563\n2546548887735\n4322674655533\n"))))
  (is (= 47 (sol2 (parse "111111111111\n999999999991\n999999999991\n999999999991\n999999999991\n")))))

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
