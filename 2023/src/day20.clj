(ns day20
  (:require [clojure.set :as set]
            [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.math.numeric-tower :refer [lcm]])
  (:import (clojure.lang PersistentQueue)))

(defn parse-line [s]
  (let [[lhs rhs] (string/split s #"->")]
    {:type         (cond
                     (string/starts-with? lhs "%") :flip-flop
                     (string/starts-with? lhs "&") :conjunction
                     :else :broadcast)
     :name         (re-find #"\w+" lhs)
     :destinations (vec (re-seq #"\w+" rhs))}))

(defn parse [s]
  (->> (string/split-lines s)
       (map parse-line)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defprotocol Module
  (on-pulse [this high? from] "Returns an updated instance and resulting pulses"))

(defrecord Broadcast [name destinations]
  Module
  (on-pulse [this high? _] {:updated-module this
                            :high-out?      high?}))

(defrecord FlipFlop [name destinations on?]
  Module
  (on-pulse [this high? _]
    (if high?
      {:updated-module this
       :high-out?      nil}
      (let [turns-on? (not (:on? this))]
        {:updated-module (assoc this :on? turns-on?)
         :high-out?      turns-on?}))))

(defrecord Conjunction [name destinations last-inputs]
  Module
  (on-pulse [this high? from]
    (let [updated-module (assoc-in this [:last-inputs from] high?)]
      {:updated-module updated-module
       :high-out?      (not-every? true? (vals (:last-inputs updated-module)))})))

(defn sources [modules]
  (->> (for [module modules
             dst (:destinations module)]
         {dst #{(:name module)}})
       (apply merge-with set/union)))

(defn include-sinks [modules]
  (let [sinks (->> (vals modules)
                   (mapcat :destinations)
                   (remove (fn [dst] (contains? modules dst))))]
    (->> sinks
         (reduce (fn [modules sink] (assoc modules sink (->Broadcast sink [])))
                 modules))))

(defn input->modules [input]
  (let [sources-map (sources input)]
    (->> (for [{:keys [name type destinations]} input]
           {name (case type
                   :broadcast (->Broadcast name destinations)
                   :flip-flop (->FlipFlop name destinations false)
                   :conjunction (->Conjunction name destinations (->> (for [src (get sources-map name)] {src false})
                                                                      (into {}))))})
         (into {})
         include-sinks)))

(defn push-button [{:keys [modules pulse-freqs broadcaster goal]
                    :or   {pulse-freqs {:low 0, :high 0}
                           broadcaster "broadcaster"}}]
  (loop [pulses (conj PersistentQueue/EMPTY {:dst broadcaster, :high? false, :src nil})
         res modules
         pulse-freqs pulse-freqs
         delivered-high-to-goal false]
    (if-some [pulse (peek pulses)]
      (let [module (get res (:dst pulse))
            dsts (:destinations module)
            {:keys [updated-module high-out?]} (on-pulse module (:high? pulse) (:src pulse))
            new-pulses (if (some? high-out?)
                         (for [dst dsts] {:dst dst, :high? high-out?, :src (:dst pulse)})
                         [])]
        (recur (into (pop pulses) new-pulses)
               (assoc res (:dst pulse) updated-module)
               (update pulse-freqs (if (:high? pulse) :high :low) inc)
               (or delivered-high-to-goal
                   (and (= (:dst pulse) goal) (:high? pulse)))))
      {:modules                res
       :pulse-freqs            pulse-freqs
       :broadcaster            broadcaster
       :goal                   goal
       :delivered-high-to-goal delivered-high-to-goal})))

(defn sol1 [input]
  (let [state-seq (iterate push-button {:modules (input->modules input)})]
    (->> (nth state-seq 1000)
         :pulse-freqs
         vals
         (reduce *))))

(deftest part1-examples
  (is (= 32000000 (sol1 (parse "broadcaster -> a, b, c\n%a -> b\n%b -> c\n%c -> inv\n&inv -> a\n"))))
  (is (= 11687500 (sol1 (parse "broadcaster -> a\n%a -> inv, con\n&inv -> b\n%b -> con\n&con -> output\n")))))

; ==============================================================================
; Part 2 ; FIXME: Exploits input structure rx <- &... <- ..., ..., ..., ...
; ==============================================================================
(defn cycle-length [modules broadcaster goal]
  (let [state-seq (iterate push-button {:modules     modules
                                        :broadcaster broadcaster
                                        :goal        goal})]
    (->> state-seq
         (map-indexed vector)
         (filter (fn [[i state]] (:delivered-high-to-goal state)))
         ffirst)))

(defn sol2 [input]
  (let [modules (input->modules input)]
    (let [rx-src (->> (vals modules)
                      (filter (fn [module] (some #{"rx"} (:destinations module))))
                      first
                      :name)]
      (->> (get modules "broadcaster")
           :destinations
           (map #(cycle-length modules % rx-src))
           (reduce lcm)))))

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
