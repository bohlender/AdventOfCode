(ns day12
  (:require [clojure.string :as string])
  (:require [clojure.test :refer :all]))

(defn parse [s]
  "Turns lines of dash-separated vertex identifiers into an adjacency list"
  (let [lines (string/split-lines s)
        edges (mapv #(string/split % #"-") lines)]
    (reduce
      (fn [res [v1 v2]] (-> res
                            (update v1 conj v2)
                            (update v2 conj v1)))
      {}
      edges)))

(defn parse-file [filename]
  (parse (slurp filename)))

; ===================
; Part 1
; ===================
(defn paths-to-end
  "Finds paths to \"end\", only exploring vertices v where (candidate? path v)"
  ([g candidate? from]
   (paths-to-end g candidate? from '()))
  ([g candidate? from path]
   (let [new-path (conj path from)]
     (if (= from "end")
       (list new-path)
       (->> (get g from)
            (filter #(candidate? new-path %))
            (mapcat #(paths-to-end g candidate? % new-path)))))))

(defn upper-case? [s]
  (= s (string/upper-case s)))

(defn sol1 [g]
  (letfn [(candidate? [path v]
            (or (upper-case? v)
                (not (some #{v} path))))]
    (->> (paths-to-end g candidate? "start")
         count)))

(deftest part1-examples
  (is (= (sol1 (parse "start-A\nstart-b\nA-c\nA-b\nb-d\nA-end\nb-end")) 10))
  (is (= (sol1 (parse "dc-end\nHN-start\nstart-kj\ndc-start\ndc-HN\nLN-dc\nHN-end\nkj-sa\nkj-HN\nkj-dc")) 19))
  (is (= (sol1 (parse "fs-end\nhe-DX\nfs-he\nstart-DX\npj-DX\nend-zg\nzg-sl\nzg-pj\npj-he\nRW-he\nfs-DX\npj-RW\nzg-RW\nstart-pj\nhe-WI\nzg-he\npj-fs\nstart-RW")) 226)))

; ===================
; Part 2
; ===================
(defn extremal? [v]
  (some #{v} ["start" "end"]))

(defn upper-or-extremal? [v]
  (or (upper-case? v)
      (extremal? v)))

(defn sol2 [g]
  (letfn [(candidate? [path v]
            (or (upper-case? v)
                (if (extremal? v)
                  ; extremal vertices may be visited once
                  (not (some #{v} path))
                  ; of the others, one may be visited twice
                  (let [others (remove upper-or-extremal? (conj path v))
                        freqs  (vals (frequencies others))]
                    (and (every? #(<= % 2) freqs)
                         (-> (filter #{2} freqs)
                             count
                             (< 2)))))))]
    (->> (paths-to-end g candidate? "start")
         count)))

(deftest part2-examples
  (is (= (sol2 (parse "start-A\nstart-b\nA-c\nA-b\nb-d\nA-end\nb-end")) 36)))

; ===================
; Main
; ===================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (parse-file filename)]
      (println "First:" (sol1 input))
      (println "Second:" (sol2 input)))))
