(ns day13
  (:require [clojure.string :as string]
            [clojure.test :refer :all]))

(defrecord Paper [dots instrs])
(defrecord Dot [x y])
(defrecord Instr [axis pos])

(defn parse [s]
  (let [lines       (string/split-lines s)
        empty-idx   (.indexOf lines "")
        dot-lines   (subvec lines 0 empty-idx)
        instr-lines (subvec lines (inc empty-idx))
        dots        (mapv (comp (fn [[x y]] (->Dot (Integer/parseInt x) (Integer/parseInt y)))
                                #(string/split % #","))
                          dot-lines)
        instrs      (mapv (comp (fn [[_ axis pos]] (->Instr (keyword axis) (Integer/parseInt pos)))
                                #(re-matches #"fold along (\w)=(\d+)" %))
                          instr-lines)]
    (->Paper (set dots) instrs)))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn dots-dim [dots axis]
  (->> dots
       (map axis)
       (apply max)
       inc))

(defn plot [p]
  (let [w (dots-dim (:dots p) :x)
        h (dots-dim (:dots p) :y)]
    (doseq [y (range h)]
      (->> (mapv (comp #(if % "#" ".")
                       #(contains? (:dots p) (->Dot % y)))
                 (range w))
           println))))

(defn dot-mirror [dot {axis :axis, pos :pos}]
  (update dot axis #(- pos (- % pos))))

(defn dots-fold [dots {axis :axis, pos :pos, :as instr}]
  (->> dots
       ; mirror if above pos on axis; otherwise keep
       (mapv #(if (> (axis %) pos)
                (dot-mirror % instr)
                %))
       set))

(defn step [paper]
  (if-let [instr (first (:instrs paper))]
    (-> paper
        (update :dots #(dots-fold % instr))
        (update :instrs rest))))

(defn sol1 [paper]
  (->> (step paper)
       :dots
       count))

(deftest part1-examples
  (let [input "6,10\n0,14\n9,10\n0,3\n10,4\n4,11\n6,0\n6,12\n4,1\n0,13\n10,12\n3,4\n3,0\n8,4\n1,10\n2,14\n8,10\n9,0\n\nfold along y=7\nfold along x=5"]
    (is (= (sol1 (parse input)) 17))))

; ===================
; Part 2
; ===================
(defn sol2 [paper]
  (let [it (iterate step paper)]
    (->> (last (take-while (complement nil?) it))
         plot)))

; ===================
; Main
; ===================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (parse-file filename)]
      (println "First:" (sol1 input))
      (println "Second:")
      (sol2 input))))
