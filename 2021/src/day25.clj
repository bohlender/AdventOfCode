(ns day25
  (:require [clojure.string :as string]
            [clojure.test :refer :all]))

(defrecord State [width height east south])

(defn mk-state [tiles]
  (reduce (fn [state [coord c]] (-> (case c
                                      \> (update state :east conj coord)
                                      \v (update state :south conj coord)
                                      state)
                                    (update :width max (inc (:x coord)))
                                    (update :height max (inc (:y coord)))))
          (->State 0 0 #{} #{})
          tiles))

(defn parse [s]
  (->> (for [[y line] (map-indexed list (string/split-lines s))
             [x c] (map-indexed list line)]
         [{:x x, :y y} c])
       mk-state))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn free? [state coord]
  (and (not (contains? (:east state) coord))
       (not (contains? (:south state) coord))))

(defn try-move [state axis from]
  (let [bound (if (= axis :x) (:width state) (:height state))
        to (update from axis #(-> % inc (mod bound)))]
    (if (free? state to) to from)))

(defn step [state]
  (let [intermediate (->> (:east state)
                          (into #{} (map #(try-move state :x %)))
                          (assoc state :east))
        next-state (->> (:south intermediate)
                        (into #{} (map #(try-move intermediate :y %)))
                        (assoc intermediate :south))]
    next-state))

(defn sol1 [init-state]
  (loop [state init-state
         count 0]
    (let [next-state (step state)
          next-count (inc count)]
      (if (= state next-state)
        next-count
        (recur next-state next-count)))))

(deftest part1-examples
  (let [state (step (parse "..........\n.>v....v..\n.......>..\n.........."))]
    (is (= state (parse "..........\n.>........\n..v....v>.\n.........."))))
  (let [init-state (parse "v...>>.vv>\n.vv>>.vv..\n>>.>v>...v\n>>v>>.>.v.\nv>v.vv.v..\n>.>>..v...\n.vv..>.>v.\nv.v..>>v.v\n....v..v.>")]
    (is (= (sol1 init-state) 58))))

; ==============================================================================
; Main
; ==============================================================================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (parse-file filename)]
      (println "First:" (sol1 input)))))