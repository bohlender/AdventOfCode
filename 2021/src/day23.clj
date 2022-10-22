(ns day23
  (:require [clojure.data.priority-map :refer [priority-map]]
            [clojure.pprint]
            [clojure.set :as set]
            [clojure.string :as string]
            [clojure.test :refer :all]))

(defn parse-tiles [s]
  (let [lines (string/split-lines s)]
    (->> (for [[y line] (map-indexed list lines)
               [x c] (map-indexed list line)
               :when (not (Character/isWhitespace c))]
           [[x y] c])
         (into {}))))

(declare mk-problem)
(defn parse [s]
  (->> (parse-tiles s)
       (reduce (fn [res [coord c]]
                 (case c
                   \# (update res :walls conj coord)
                   \A (update res :pods conj [coord :amber])
                   \B (update res :pods conj [coord :bronze])
                   \C (update res :pods conj [coord :copper])
                   \D (update res :pods conj [coord :desert])
                   res))                                    ; skip \.
               {:walls #{}, :pods {}})
       mk-problem))

; ==============================================================================
; Part 1
; TODO: Solve cleaner.
;       - Inconsistent to have :walls in state but inside-room? not
;       - Abstract A* instead of complecting search with problem-specifics
;       - Better parsing and insertion of missing lines for part 2
; ==============================================================================
(defrecord Problem [state goals])
(defrecord State [walls pods])
(defrecord Move [from to])

(defn mk-problem [{:keys [walls pods]}]
  (let [height (->> walls (map second) (reduce max))
        goals (->> (for [[pod x] {:amber 3 :bronze 5 :copper 7 :desert 9}]
                     (->> (map #(vector x %) (range 2 height))
                          (apply sorted-set)
                          (vector pod)))
                   (into {}))]
    (->Problem (->State walls pods) goals)))

(def immediately-outside-room?
  #{[3 1] [5 1] [7 1] [9 1]})

(defn inside-room? [[x y]]
  (and (#{3 5 7 9} x) (> y 1)))

(defn in-hallway? [coord]
  (not (inside-room? coord)))

(defn step-cost [pod]
  (case pod
    :amber 1
    :bronze 10
    :copper 100
    :desert 1000))

(defn neighbours [[x y]]
  "Returns the four neighbours of a 2d coordinate."
  [[(inc x) y] [(dec x) y] [x (inc y)] [x (dec y)]])

(defn manhattan-dist [from to]
  "Returns the manhattan distance between the coordinates from and to."
  (->> (map (comp abs -) to from)
       (reduce +)))

(defn cost-from-pods [goals pods]
  "Under-approximates the cost for pods to reach the goals."
  (let [pod-coords (->> pods (transduce (map (fn [[coord pod]] {pod #{coord}}))
                                        (partial merge-with into)))]
    (let [rest-pods (merge-with set/difference pod-coords goals)
          rest-goals (merge-with set/difference goals pod-coords)
          dists-per-pod (merge-with (partial map manhattan-dist) rest-pods rest-goals)
          ; additionally, pods in wrong rooms must move to the hall
          dists-to-hall (update-vals rest-pods (fn [coords] (->> coords
                                                                 (filter inside-room?)
                                                                 (map (fn [[x y]] (dec y))))))]
      (->> (merge-with into dists-per-pod dists-to-hall)
           (transduce (map (fn [[pod dists]] (* (reduce + dists) (step-cost pod))))
                      +)))))

(defn pod-at [state coord]
  (get-in state [:pods coord]))

(defn free? [state coord]
  (and (not (contains? (:walls state) coord))
       (nil? (pod-at state coord))))

(defn reachable [state from]
  "Returns a collection of coordinates reachable from the given coordinate. Excludes from."
  (let [free? (partial free? state)]
    (loop [stack (list from)
           seen #{}]
      (if-some [from (peek stack)]
        (let [next-stack (->> (neighbours from)
                              (into (pop stack) (comp (filter free?) (filter #(not (seen %))))))
              next-seen (conj seen from)]
          (recur next-stack next-seen))
        (disj seen from)))))

(defn implies [a b]
  (or (not a) b))

(defn moves [goals state]
  (let [deepest-goal-coord (->> (for [[pod coords] goals
                                      :when (->> coords
                                                 (keep #(pod-at state %))
                                                 (every? #{pod}))
                                      :let [deepest-coord (->> coords
                                                               (filter #(nil? (pod-at state %)))
                                                               last)]]
                                  [pod deepest-coord])
                                (into {}))]
    (for [[from pod] (:pods state)
          to (reachable state from)
          ; pods in rooms can move to hall but may not stop on the space immediately outside any room
          :when (implies (inside-room? from)
                         (and (in-hallway? to)
                              (not (immediately-outside-room? to))))
          ; pods in hall can move to a room if it's their destination room and no other-type pods are in there
          :when (implies (in-hallway? from)
                         (= to (deepest-goal-coord pod)))]
      (->Move from to))))

(defn move-cost [state {:keys [from, to] :as move}]
  (let [num-steps (manhattan-dist from to)]                 ; only works because we have no direct moves from room to room
    (->> (pod-at state from)
         step-cost
         (* num-steps))))

(defn apply-move [state {:keys [from, to] :as move}]
  (let [pod (pod-at state from)]
    (-> state
        (update :pods dissoc from)
        (update :pods assoc to pod))))

(defn sol1 [problem]
  (let [init-state (:state problem)
        goals (:goals problem)
        cost-from (memoize (partial cost-from-pods goals))]
    (loop [worklist (priority-map init-state (cost-from (:pods init-state))) ; (state, cost from start + underapprox cost to goal)
           cost-to-pods {(:pods init-state) 0}]
      (let [[state _] (peek worklist)
            cost-to-state (cost-to-pods (:pods state))]
        (if (zero? (cost-from (:pods state)))
          cost-to-state
          (let [succs-and-cost-to (->> (for [move (moves goals state)
                                             :let [succ (apply-move state move)
                                                   old-cost-to-succ (cost-to-pods (:pods succ))
                                                   new-cost-to-succ (+ cost-to-state (move-cost state move))]
                                             :when (or (nil? old-cost-to-succ) (< new-cost-to-succ old-cost-to-succ))]
                                         [succ new-cost-to-succ])
                                       (into {}))
                next-worklist (into (pop worklist)
                                    (map (fn [[succ cost-to-succ]] [succ (+ cost-to-succ (cost-from (:pods succ)))]))
                                    succs-and-cost-to)
                next-cost-to (into cost-to-pods
                                   (map (fn [[succ cost-to-succ]] [(:pods succ) cost-to-succ]))
                                   succs-and-cost-to)]
            (recur next-worklist next-cost-to)))))))

(deftest part1-examples
  (let [problem (parse "#############\n#...........#\n###B#C#B#D###\n  #A#D#C#A#\n  #########")]
    (is (= (sol1 problem) 12521))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn insert-missing-lines [s]
  (let [lines (string/split-lines s)]
    (->> (concat (take 3 lines)
                 (string/split-lines "  #D#C#B#A#\n  #D#B#A#C#")
                 (drop 3 lines))
         (string/join "\n"))))

(def sol2 sol1)

(deftest part2-examples
  (let [problem (parse (insert-missing-lines "#############\n#...........#\n###B#C#B#D###\n  #A#D#C#A#\n  #########"))]
    (is (= (sol2 problem) 44169))))

; ==============================================================================
; Main
; ==============================================================================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          s (slurp filename)]
      (println "First:" (->> s parse sol1))
      (println "Second:" (->> s insert-missing-lines parse sol2)))))