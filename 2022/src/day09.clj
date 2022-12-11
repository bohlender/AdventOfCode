(ns day09
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defrecord Motion [dir dist])

(defn parse-motion [line]
  (let [[dir dist] (string/split line #"\s")]
    (->Motion (keyword dir) (Integer/parseInt dist))))

(defn parse [s]
  (->> (string/split-lines s)
       (map parse-motion)))

; ==============================================================================
; Part 1
; ==============================================================================
(def origin {:x 0 :y 0})
(def vec-plus (partial merge-with +))
(def vec-minus (partial merge-with -))

(defn to-single-step-dirs [motion]
  "Turns a motion of distance N into a seq of motions of distance 1."
  (repeat (:dist motion) (:dir motion)))

(defn dir->dir-vec [dir]
  (case dir
    :R {:x 1 :y 0}
    :L {:x -1 :y 0}
    :U {:x 0 :y 1}
    :D {:x 0 :y -1}))

(defn touching? [coord other]
  (->> (vec-minus coord other)
       vals
       (map abs)
       (every? #(<= % 1))))

(defn update-knot [prev-knot knot]
  "Returns the new coordinate of a knot depending on the previous (potentially the head) knot."
  (if (touching? prev-knot knot)
    knot
    (let [dir-vec (vec-minus prev-knot knot)
          bounded-dir-vec (update-vals dir-vec #(-> % (min 1) (max -1)))]
      (vec-plus knot bounded-dir-vec))))

(defn move-rope [rope dir-vec]
  "Returns the knot coordinates resulting from moving the head knot by dir-vec (incl. effect on tail knots)."
  (let [new-head (vec-plus (first rope) dir-vec)]
    (->> (rest rope)
         (reduce (fn [new-rope knot] (conj new-rope (update-knot (last new-rope) knot)))
                 [new-head]))))

; TODO: Extract coord tracking?
(defn count-last-knot-coords [init-rope motions]
  (->> motions
       (mapcat to-single-step-dirs)
       (map dir->dir-vec)
       (reduce (fn [{:keys [rope last-coords]} dir-vec]
                 (let [next-rope (move-rope rope dir-vec)]
                   {:rope        next-rope
                    :last-coords (conj last-coords (last next-rope))}))
               {:rope        init-rope
                :last-coords #{}})
       :last-coords
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
