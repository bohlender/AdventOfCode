(ns day05
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defn parse-numbers [s]
  (->> (re-seq #"\d+" s)
       (mapv #(Long/parseLong %))))

(defn parse-map-entry [s]
  (let [[dst src len] (parse-numbers s)]
    {:src src
     :dst dst
     :len len}))

(defn parse-map [s]
  (let [lines (string/split-lines s)]
    {:name    (re-find #"\S+" (first lines))
     :entries (mapv parse-map-entry (rest lines))}))

(with-test
  (defn parse [s]
    (let [[seeds-part & maps-part] (string/split s #"\n\n")]
      {:seeds (parse-numbers seeds-part)
       :maps  (mapv parse-map maps-part)}))

  (is (= (parse "seeds: 79 14 55 13\n\nseed-to-soil map:\n50 98 2\n52 50 48\n\nsoil-to-fertilizer map:\n0 15 37\n37 52 2\n39 0 15")
         {:seeds [79 14 55 13],
          :maps  [{:name "seed-to-soil", :entries [{:src 98, :dst 50, :len 2} {:src 50, :dst 52, :len 48}]}
                  {:name    "soil-to-fertilizer",
                   :entries [{:src 15, :dst 0, :len 37} {:src 52, :dst 37, :len 2} {:src 0, :dst 39, :len 15}]}]})))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn split-interval-by-interval [[from1 to1] [from2 to2]]
  (cond
    ; only left bound of 1 intersects with 2
    (< from2 from1 to2 to1) [[from1 to2]
                             [to2 to1]]
    ; 2 contained in 1
    (< from1 from2 to2 to1) [[from1 (dec from2)]
                             [from2 to2]
                             [to2 to1]]
    ; only right bound of 1 intersects with 2
    (< from1 from2 to1 to2) [[from1 (dec from2)]
                             [from2 to1]]
    :else [[from1 to1]]))

(defn split-intervals-by-entries [intervals entries]
  (->> entries
       (reduce (fn [intervals {:keys [src len]}]
                 (mapcat #(split-interval-by-interval % [src (+ src len)]) intervals))
               intervals)))

; FIXME: Inefficient lookup
(defn transform-n [map n]
  (let [entries (:entries map)]
    (if-some [relevant-entry (first (filter (fn [{:keys [src len]}]
                                              (<= src n (+ src len -1))) entries))]
      (+ (:dst relevant-entry) (- n (:src relevant-entry)))
      n)))

(defn transform-interval [map [from to]]
  ; Note: `to` is outside the interval, so we transform `to`-1
  [(transform-n map from) (inc (transform-n map (dec to)))])

(defn transform-intervals [map intervals]
  (->> (:entries map)
       (split-intervals-by-entries intervals)
       (mapv (partial transform-interval map))))

(defn Map->fn [map]
  (partial transform-intervals map))

(defn input->fn [input]
  (->> (:maps input)
       (map Map->fn)
       reverse
       (reduce comp)))

(defn singleton-intervals [input]
  (->> (:seeds input)
       (map (fn [n] [n (inc n)]))))

(defn sol1 [input]
  (let [f (input->fn input)]
    (->> (singleton-intervals input)
         f
         flatten
         (apply min))))

(deftest part1-examples
  (let [input (parse "seeds: 79 14 55 13\n\nseed-to-soil map:\n50 98 2\n52 50 48\n\nsoil-to-fertilizer map:\n0 15 37\n37 52 2\n39 0 15\n\nfertilizer-to-water map:\n49 53 8\n0 11 42\n42 0 7\n57 7 4\n\nwater-to-light map:\n88 18 7\n18 25 70\n\nlight-to-temperature map:\n45 77 23\n81 45 19\n68 64 13\n\ntemperature-to-humidity map:\n0 69 1\n1 0 69\n\nhumidity-to-location map:\n60 56 37\n56 93 4")]
    (is (= (sol1 input) 35))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn interval-ranges [input]
  (->> (:seeds input)
       (partition 2)
       (map (fn [[from len]] [from (+ from len)]))))

(defn sol2 [input]
  (let [f (input->fn input)]
    (->> (interval-ranges input)
         f
         (remove (fn [[from to]] (zero? from))) ; FIXME: Workaround for bug in exercise or solution
         flatten
         (apply min))))

(deftest part2-examples
  (let [input (parse "seeds: 79 14 55 13\n\nseed-to-soil map:\n50 98 2\n52 50 48\n\nsoil-to-fertilizer map:\n0 15 37\n37 52 2\n39 0 15\n\nfertilizer-to-water map:\n49 53 8\n0 11 42\n42 0 7\n57 7 4\n\nwater-to-light map:\n88 18 7\n18 25 70\n\nlight-to-temperature map:\n45 77 23\n81 45 19\n68 64 13\n\ntemperature-to-humidity map:\n0 69 1\n1 0 69\n\nhumidity-to-location map:\n60 56 37\n56 93 4")]
    (is (= (sol2 input) 46))))

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
