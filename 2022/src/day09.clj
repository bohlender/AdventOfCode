(ns day09
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn parse-motion [line]
  (let [[dir dist] (string/split line #"\s")]
    {:dir  (keyword dir)
     :dist (Integer/parseInt dist)}))

(defn parse [s]
  (->> (string/split-lines s)
       (map parse-motion)))

; ==============================================================================
; Part 1
; ==============================================================================
(defrecord Vec2d [x y])
(def origin (->Vec2d 0 0))

(defn to-single-step-dirs [motion]
  "Turns a motion of distance N into a seq of motions of distance 1."
  (repeat (:dist motion) (:dir motion)))

(defn dir->dir-vec [dir]
  (case dir
    :R {:x 1 :y 0}
    :L {:x -1 :y 0}
    :U {:x 0 :y 1}
    :D {:x 0 :y -1}))

(defn vec-plus [coord delta]
  (merge-with + coord delta))

(defn vec-minus [coord delta]
  (merge-with - coord delta))

(defn touching? [coord other]
  (->> (vec-minus coord other)
       vals
       (map abs)
       (every? #(<= % 1))))

(defn update-knot [prev-knot knot]
  "Returns the new coordinate of a knot depending on the previous (potentially the head) knot."
  (if (touching? prev-knot knot)
    knot
    (let [delta (vec-minus prev-knot knot)
          bounded-delta (update-vals delta #(-> % (min 1) (max -1)))]
      (vec-plus knot bounded-delta))))

(defn move-rope [rope dir-vec]
  "Returns the knot coordinates resulting from moving the head knot by delta (incl. effect on tail knots)."
  (let [new-head (vec-plus (first rope) dir-vec)]
    (->> (rest rope)
         (reduce (fn [new-rope knot] (conj new-rope (update-knot (last new-rope) knot)))
                 [new-head]))))

; TODO: Extract coord tracking?
(defn count-last-knot-coords [init-rope motions]
  (->> motions
       (mapcat to-single-step-dirs)
       (map dir->dir-vec)
       (reduce (fn [{:keys [rope tail-coords]} delta]
                 (let [next-rope (move-rope rope delta)]
                   {:rope        next-rope
                    :tail-coords (conj tail-coords (last next-rope))}))
               {:rope        init-rope
                :tail-coords #{}})
       :tail-coords
       count))

(defn sol1 [motions]
  (count-last-knot-coords [origin origin] motions))

(deftest part1-examples
  (let [input (parse "R 4\nU 4\nL 3\nD 1\nR 4\nD 1\nL 5\nR 2\n")]
    (is (= (sol1 input) 13))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn sol2 [motions]
  (count-last-knot-coords (repeat 10 origin) motions))

(deftest part2-examples
  (let [input (parse "R 5\nU 8\nL 8\nD 3\nR 17\nD 10\nL 25\nU 20\n")]
    (is (= (sol2 input) 36))))

; ==============================================================================
; Main
; ==============================================================================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (parse (slurp filename))]
      (println "First:" (sol1 input))
      (println "Second:" (sol2 input)))))
