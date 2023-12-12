(ns day12
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn parse-numbers [s]
  (->> (re-seq #"-?\d+" s)
       (mapv parse-long)))

(defn parse-line [s]
  (let [parts (string/split s #"\s")]
    {:springs     (first parts)
     :group-sizes (parse-numbers (second parts))}))

(defn parse [s]
  (->> (string/split-lines s)
       (map parse-line)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn string-replace-at [s idx c]
  (str (subs s 0 idx) c (subs s (inc idx))))

(def arrangements-count
  (memoize
    (fn [{:keys [springs group-sizes] :as record}]
      (if (empty? springs)
        (if (empty? group-sizes) 1 0)
        (case (first springs)
          \. (arrangements-count (update record :springs subs 1))
          \# (if-let [len (first group-sizes)]
               (if (and (>= (count springs) len)
                        (every? #{\# \?} (take len springs)) ; turnable into sequence of len \#
                        (not= (nth springs len \.) \#))      ; not another \#
                 (arrangements-count (-> record
                                         (update :springs subs (min (inc len) (count springs)))
                                         (update :group-sizes rest)))
                 0)
               0)
          \? (->> [(update record :springs string-replace-at 0 \.)
                   (update record :springs string-replace-at 0 \#)]
                  (map arrangements-count)
                  (reduce +)))))))

(defn sol1 [input]
  (->> (map arrangements-count input)
       (reduce +)))

(deftest part1-examples
  (is (= 1 (arrangements-count (parse-line "???.### 1,1,3"))))
  (is (= 4 (arrangements-count (parse-line ".??..??...?##. 1,1,3"))))
  (is (= 1 (arrangements-count (parse-line "?#?#?#?#?#?#?#? 1,3,1,6"))))
  (is (= 1 (arrangements-count (parse-line "????.#...#... 4,1,1"))))
  (is (= 4 (arrangements-count (parse-line "????.######..#####. 1,6,5"))))
  (is (= 10 (arrangements-count (parse-line "?###???????? 3,2,1"))))
  (is (= 21 (sol1 (parse "???.### 1,1,3\n.??..??...?##. 1,1,3\n?#?#?#?#?#?#?#? 1,3,1,6\n????.#...#... 4,1,1\n????.######..#####. 1,6,5\n?###???????? 3,2,1\n")) 21)))

; ==============================================================================
; Part 2
; ==============================================================================
(defn unfold-record [{:keys [springs group-sizes] :as record}]
  {:springs     (string/join "?" (repeat 5 springs))
   :group-sizes (flatten (repeat 5 group-sizes))})

(defn sol2 [input]
  (sol1 (map unfold-record input)))

(deftest part2-examples
  (is (= 1 (arrangements-count (unfold-record (parse-line "???.### 1,1,3")))))
  (is (= 16384 (arrangements-count (unfold-record (parse-line ".??..??...?##. 1,1,3")))))
  (is (= 525152 (sol2 (parse "???.### 1,1,3\n.??..??...?##. 1,1,3\n?#?#?#?#?#?#?#? 1,3,1,6\n????.#...#... 4,1,1\n????.######..#####. 1,6,5\n?###???????? 3,2,1\n")))))

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

