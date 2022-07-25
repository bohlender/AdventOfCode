(ns day18
  (:require [clojure.string :as string]
            [clojure.test :refer :all]
            [clojure.math :as math]))

(defn parse [s]
  (->> s string/split-lines (map read-string)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
;
; TODO: Check whether leveraging clojure.zip yields simpler the code
; ==============================================================================
(def leaf? number?)

(defn indexed-subtrees
  "Returns all (path, subtree) pairs in an in-order traversal."
  ([tree]
   (indexed-subtrees tree []))
  ([tree cur-path]
   (if (leaf? tree)
     [[cur-path tree]]
     (->> tree
          (keep-indexed (fn [idx subtree] (indexed-subtrees subtree (conj cur-path idx))))
          (apply concat [[cur-path tree]])))))

(defn filter-tree [pred tree]
  (->> tree
       indexed-subtrees
       (filter (fn [[path subtree]] (pred path subtree)))))

(defn leaf-paths [tree]
  (->> tree
       (filter-tree (fn [path subtree] (leaf? subtree)))
       (map first)))

;;; Explode
(defn must-explode? [path tree]
  (and (not (leaf? tree)) (>= (count path) 4)))

(defn explode-tree [tree path]
  (let [[lhs rhs] (get-in tree path)
        leaf-path-pairs (->> tree leaf-paths (partition 2 1))
        prev-path (->> leaf-path-pairs
                       (filter (fn [[_ p]] (= p (conj path 0))))
                       ffirst)
        next-path (->> leaf-path-pairs
                       (filter (fn [[p _]] (= p (conj path 1))))
                       first
                       second)]
    (letfn [(try-update-in [m ks f & args]
              (if ks (apply update-in m ks f args) m))]
      (-> tree
          (assoc-in path 0)
          (try-update-in prev-path + lhs)
          (try-update-in next-path + rhs)))))

;;; Split
(defn must-split? [path tree]
  (and (leaf? tree) (>= tree 10)))

(defn split-tree [tree path]
  (let [num (get-in tree path)
        lhs (-> num (/ 2.0) math/floor int)
        rhs (-> num (/ 2.0) math/ceil int)]
    (assoc-in tree path [lhs rhs])))

;;; Reduce/Normalize
(defn- normalize-step [tree]
  (if-let [[path _] (->> (filter-tree must-explode? tree) first)]
    (explode-tree tree path)
    (if-let [[path _] (->> (filter-tree must-split? tree) first)]
      (split-tree tree path)
      tree)))

(defn normalize [tree]
  (->> tree
       (iterate normalize-step)
       (partition 2 1)
       (drop-while (fn [[cur next]] (not= cur next)))
       ffirst))

;;; Sum snailfish numbers
(defn sum-trees [trees]
  (->> trees
       (reduce (fn [acc-tree tree] (normalize [acc-tree tree])))))

(defn magnitude [tree]
  (if (leaf? tree)
    tree
    (+ (* 3 (magnitude (first tree)))
       (* 2 (magnitude (second tree))))))

(defn sol1 [trees]
  (->> trees
       sum-trees
       magnitude))

(deftest part1-examples
  (is (->> [[[[0, 7], 4], [7, [[8, 4], 9]]], [1, 1]] (filter-tree must-explode?) first (= [[0 1 1 0] [8, 4]])))
  (is (-> [[[[0, 7], 4], [7, [[8, 4], 9]]], [1, 1]] (explode-tree [0 1 1 0]) (= [[[[0, 7], 4], [15, [0, 13]]], [1, 1]])))
  (is (->> [[[[0, 7], 4], [15, [0, 13]]], [1, 1]] (filter-tree must-split?) first (= [[0 1 0] 15])))
  (is (-> [[[[0, 7], 4], [15, [0, 13]]], [1, 1]] (split-tree [0 1 0]) (= [[[[0, 7], 4], [[7, 8], [0, 13]]], [1, 1]])))
  (is (-> [[[[[4, 3], 4], 4], [7, [[8, 4], 9]]], [1, 1]] normalize (= [[[[0, 7], 4], [[7, 8], [6, 0]]], [8, 1]])))
  (let [input (parse "[[[0,[4,5]],[0,0]],[[[4,5],[2,6]],[9,5]]]\n[7,[[[3,7],[4,3]],[[6,3],[8,8]]]]\n[[2,[[0,8],[3,4]]],[[[6,7],1],[7,[1,6]]]]\n[[[[2,4],7],[6,[0,5]]],[[[6,8],[2,8]],[[2,1],[4,5]]]]\n[7,[5,[[3,8],[1,4]]]]\n[[2,[2,2]],[8,[8,1]]]\n[2,9]\n[1,[[[9,3],9],[[9,0],[0,7]]]]\n[[[5,[7,4]],7],1]\n[[[[4,2],2],6],[8,7]]")]
    (is (-> input sum-trees (= [[[[8, 7], [7, 7]], [[8, 6], [7, 7]]], [[[0, 7], [6, 6]], [8, 7]]]))))
  (is (-> [[[[8, 7], [7, 7]], [[8, 6], [7, 7]]], [[[0, 7], [6, 6]], [8, 7]]] magnitude (= 3488)))
  (let [input (parse "[[[0,[5,8]],[[1,7],[9,6]]],[[4,[1,2]],[[1,4],2]]]\n[[[5,[2,8]],4],[5,[[9,9],0]]]\n[6,[[[6,2],[5,6]],[[7,6],[4,7]]]]\n[[[6,[0,7]],[0,9]],[4,[9,[9,0]]]]\n[[[7,[6,4]],[3,[1,3]]],[[[5,5],1],9]]\n[[6,[[7,3],[3,2]]],[[[3,8],[5,7]],4]]\n[[[[5,4],[7,7]],8],[[8,3],8]]\n[[9,3],[[9,9],[6,[4,9]]]]\n[[2,[[7,7],7]],[[5,8],[[9,3],[0,2]]]]\n[[[[5,2],5],[8,[3,7]]],[[5,[7,5]],[4,4]]]")]
    (is (-> input sol1 (= 4140)))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn sol2 [trees]
  (->> (for [lhs trees
             rhs trees]
         (sol1 [lhs rhs]))
       (apply max)))

(deftest part2-examples
  (let [input (parse "[[[0,[5,8]],[[1,7],[9,6]]],[[4,[1,2]],[[1,4],2]]]\n[[[5,[2,8]],4],[5,[[9,9],0]]]\n[6,[[[6,2],[5,6]],[[7,6],[4,7]]]]\n[[[6,[0,7]],[0,9]],[4,[9,[9,0]]]]\n[[[7,[6,4]],[3,[1,3]]],[[[5,5],1],9]]\n[[6,[[7,3],[3,2]]],[[[3,8],[5,7]],4]]\n[[[[5,4],[7,7]],8],[[8,3],8]]\n[[9,3],[[9,9],[6,[4,9]]]]\n[[2,[[7,7],7]],[[5,8],[[9,3],[0,2]]]]\n[[[[5,2],5],[8,[3,7]]],[[5,[7,5]],[4,4]]]")]
    (is (-> input sol2 (= 3993)))))

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