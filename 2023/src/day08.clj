(ns day08
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.math.numeric-tower :refer [lcm]]))

(defn parse-node [s]
  (let [[id left right] (re-seq #"\w+" s)]
    {id {:L left
         :R right}}))

(defn parse [s]
  (let [[instructions nodes] (string/split s #"\n\n")]
    {:instrs (map (comp keyword str) instructions)
     :nodes  (->> (map parse-node (string/split-lines nodes))
                  (apply merge))}))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn step [nodes cur-node instr]
  (get-in nodes [cur-node instr]))

(defn num-steps-to-goal [{:keys [nodes instrs] :as input} goal? node]
  (loop [cur-node node
         step-count 0]
    (if (goal? cur-node)
      step-count
      (let [instr-idx (mod step-count (count instrs))
            next-node (step nodes cur-node (nth instrs instr-idx))]
        (recur next-node (inc step-count))))))

(defn sol1 [input]
  (num-steps-to-goal input #(= "ZZZ" %) "AAA"))

(deftest part1-examples
  (is (= 2 (sol1 (parse "RL\n\nAAA = (BBB, CCC)\nBBB = (DDD, EEE)\nCCC = (ZZZ, GGG)\nDDD = (DDD, DDD)\nEEE = (EEE, EEE)\nGGG = (GGG, GGG)\nZZZ = (ZZZ, ZZZ)\n"))))
  (is (= 6 (sol1 (parse "LLR\n\nAAA = (BBB, BBB)\nBBB = (AAA, ZZZ)\nZZZ = (ZZZ, ZZZ)\n")))))

; ==============================================================================
; Part 2 (where every node reaches a goal state every X steps)
; ==============================================================================
(defn sol2 [input]
  (let [init-nodes (->> (keys (:nodes input))
                        (filter #(string/ends-with? % "A"))
                        (into []))
        goal? (fn [node] (string/ends-with? node "Z"))]
    (->> init-nodes
         (map (partial num-steps-to-goal input goal?))
         (reduce lcm))))

(deftest part2-examples
  (let [input (parse "LR\n\n11A = (11B, XXX)\n11B = (XXX, 11Z)\n11Z = (11B, XXX)\n22A = (22B, XXX)\n22B = (22C, 22C)\n22C = (22Z, 22Z)\n22Z = (22B, 22B)\nXXX = (XXX, XXX)\n")]
    (is (= (sol2 input) 6))))

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
