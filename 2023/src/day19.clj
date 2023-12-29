(ns day19
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defrecord Check [category op rhs])
(defrecord Rule [check outcome])

(defn parse-rule [s]
  (if-some [[_ category op operand workflow] (re-matches #"(\w+)([<>])(\d+):(\w+)" s)]
    (->Rule (->Check (keyword category)
                     op
                     (parse-long operand))
            workflow)
    (->Rule nil s)))

(defrecord Part [x m a s])

(defn parse-workflow [s]
  (let [[_ name rules-str] (re-matches #"(\w+)\{(.*)\}" s)]
    {name (->> (string/split rules-str #",")
               (mapv parse-rule))}))

(defn parse-part [s]
  (let [[x m a s] (->> (re-seq #"\d+" s)
                       (map parse-long))]
    (->Part x m a s)))

(defn parse [s]
  (let [[workflows-str parts-str] (string/split s #"\n\n")]
    {:workflows (->> (string/split-lines workflows-str)
                     (map parse-workflow)
                     (into {}))
     :parts     (->> (string/split-lines parts-str)
                     (map parse-part))}))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn terminal? [name]
  (some #{\R \A} name))

(defn rule-outcome [rule part]
  (if-some [{:keys [category op rhs]} (:check rule)]
    (when (apply (get {"<" <, ">" >} op) [(get part category) rhs])
      (:outcome rule))
    (:outcome rule)))

(defn workflow-outcome [workflow part]
  (->> workflow
       (keep #(rule-outcome % part))
       first))

(defn accepted? [workflows part]
  (loop [workflow-name "in"]
    (let [workflow (get workflows workflow-name)
          outcome (workflow-outcome workflow part)]
      (if (terminal? outcome)
        (= outcome "A")
        (recur outcome)))))

(defn ratings-sum [part]
  (->> (vals part)
       (reduce +)))

(defn sol1 [{:keys [parts workflows] :as input}]
  (->> parts
       (filter (fn [part] (accepted? workflows part)))
       (map ratings-sum)
       (reduce +)))

(deftest part1-examples
  (let [input (parse "px{a<2006:qkq,m>2090:A,rfg}\npv{a>1716:R,A}\nlnx{m>1548:A,A}\nrfg{s<537:gd,x>2440:R,A}\nqs{s>3448:A,lnx}\nqkq{x<1416:A,crn}\ncrn{x>2662:A,R}\nin{s<1351:px,qqz}\nqqz{s>2770:qs,m<1801:hdj,R}\ngd{a>3333:R,R}\nhdj{m>838:A,pv}\n\n{x=787,m=2655,a=1222,s=2876}\n{x=1679,m=44,a=2067,s=496}\n{x=2036,m=264,a=79,s=2244}\n{x=2461,m=1339,a=466,s=291}\n{x=2127,m=1623,a=2188,s=1013}\n")]
    (is (= 19114 (sol1 input)))))

; ==============================================================================
; Part 2
; ==============================================================================
(def init-part
  (->Part [1 4001] [1 4001] [1 4001] [1 4001]))

(defn concrete-parts-count [abstract-part]
  (->> (vals abstract-part)
       (map (fn [[lb ub]] (- ub lb)))
       (reduce *)))

(defn intersect-intervals [[lb1 ub1] [lb2 ub2]]
  (let [lb (max lb1 lb2)
        ub (min ub1 ub2)]
    (when (< lb ub)
      [lb ub])))

(defn split-by [rule part]
  (if-some [{:keys [category op rhs]} (:check rule)]
    (case op
      "<" {:sat   (update part category intersect-intervals [1 rhs])
           :unsat (update part category intersect-intervals [rhs 4001])}
      ">" {:sat   (update part category intersect-intervals [(inc rhs) 4001])
           :unsat (update part category intersect-intervals [1 (inc rhs)])})
    {:sat   part
     :unsat nil}))

(defn interval-workflow-outcome [workflow part]
  (loop [cur-part part
         rules workflow
         res []]
    (if-some [rule (first rules)]
      (if (some? (:check rule))
        (let [{:keys [sat unsat]} (split-by rule cur-part)]
          (recur unsat
                 (rest rules)
                 (conj res {:part sat, :loc (:outcome rule)})))
        (recur cur-part
               (rest rules)
               (conj res {:part cur-part, :loc (:outcome rule)})))
      res)))

(defn sol2 [input]
  (let [workflows (:workflows input)]
    (->> (loop [worklist (list {:part init-part, :loc "in"})
                res []]
           (if-some [{:keys [part loc] :as cur} (peek worklist)]
             (let [workflow (get workflows loc)
                   succs (->> (interval-workflow-outcome workflow part)
                              (group-by (fn [succ] (some? (terminal? (:loc succ))))))]
               (recur (into (pop worklist) (get succs false))
                      (into res (get succs true))))
             res))
         (filter (fn [succ] (= "A" (:loc succ))))
         (map :part)
         (map concrete-parts-count)
         (reduce +))))

(deftest part2-examples
  (let [input (parse "px{a<2006:qkq,m>2090:A,rfg}\npv{a>1716:R,A}\nlnx{m>1548:A,A}\nrfg{s<537:gd,x>2440:R,A}\nqs{s>3448:A,lnx}\nqkq{x<1416:A,crn}\ncrn{x>2662:A,R}\nin{s<1351:px,qqz}\nqqz{s>2770:qs,m<1801:hdj,R}\ngd{a>3333:R,R}\nhdj{m>838:A,pv}\n\n{x=787,m=2655,a=1222,s=2876}\n{x=1679,m=44,a=2067,s=496}\n{x=2036,m=264,a=79,s=2244}\n{x=2461,m=1339,a=466,s=291}\n{x=2127,m=1623,a=2188,s=1013}\n")]
    (is (= 167409079868000 (sol2 input)))))

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
