(ns day16
  (:require [clojure.string :as string]
            [clojure.test :refer :all]))

(defn hex-str->bin-str [s]
  (->> (mapcat #(->> (Character/digit % 16)
                     Integer/toBinaryString
                     (format "%4s")
                     (map (fn [s] (string/replace s \space \0))))
               s)
       (apply str)))

(defn parse-hex-str [hex-str]
  (->> hex-str
       hex-str->bin-str))

(defn parse-file [filename]
  (->> filename slurp string/split-lines first parse-hex-str))

; ===================
; Part 1
; ===================
(defrecord Header [version type-id])
(defrecord LiteralPacket [header val])
(defrecord OperatorPacket [header packets])

(defn parse-header [s]
  (let [version (-> s (.substring 0 3) (Integer/parseInt 2))
        type-id (-> s (.substring 3 6) (Integer/parseInt 2))]
    [(->Header version type-id)
     (.substring s 6)]))

(defn take-until [pred coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (if (pred (first s))
        (cons (first s) '())
        (cons (first s) (take-until pred (rest s)))))))

(defn parse-literal-packet-body [s]
  (let [groups (->> s
                    (partition 5)                           ; groups of 5 bits
                    (take-until #(= (first %) \0)))         ; last group starts with 0
        val (->> groups
                 (map (comp #(apply str %) #(drop 1 %)))    ; drop each "last-group? bit"
                 (apply str)                                ; concat 4-bit groups
                 (#(Long/parseLong % 2)))
        s (.substring s (* (count groups) 5))]
    [val s]))

(declare parse-all-packets)
(declare parse-n-packets)

(defn parse-operator-packet-body [s]
  (let [len-type-id (first s)
        s (.substring s 1)]                                 ; drop len type bit
    (case len-type-id
      \0 (let [sub-packets-len (-> (.substring s 0 15) (Integer/parseInt 2)) ; first 15 bits encode the subpackets' total length in bits
               sub-packets-str (.substring s 15 (+ 15 sub-packets-len))
               s-rest (.substring s (+ 15 sub-packets-len))
               [packets _] (parse-all-packets sub-packets-str)]
           [packets s-rest])
      \1 (let [sub-packets-num (-> (.substring s 0 11) (Integer/parseInt 2))] ; first 11 bits encode the number of subpackets
           (parse-n-packets (.substring s 11) sub-packets-num)))))

(defn parse-packet [s]
  (let [[header s] (parse-header s)]
    (case (:type-id header)
      4 (let [[body s-rest] (parse-literal-packet-body s)]
          [(->LiteralPacket header body) s-rest])
      (let [[body s-rest] (parse-operator-packet-body s)]
        [(->OperatorPacket header body) s-rest]))))

(defn- parse-packets [done? s]
  (loop [s s
         packets []]
    (if (done? packets s)
      [packets s]
      (let [[packet s-rest] (parse-packet s)]
        (recur s-rest (conj packets packet))))))

(defn parse-all-packets [s]
  (->> s (parse-packets (fn [packets s-rest] (empty? s-rest)))))

(defn parse-n-packets [s n]
  (->> s (parse-packets (fn [packets s-rest] (= (count packets) n)))))

(defn sum-versions [packet]
  (reduce (fn [acc packet] (+ acc (sum-versions packet)))
          (get-in packet [:header :version])
          (:packets packet)))                               ; possibly empty

(defn sol1 [input]
  (->> input parse-packet first sum-versions))

(deftest part1-examples
  (let [s (parse-hex-str "D2FE28")]
    (is (= s "110100101111111000101000"))
    (is (->> s parse-packet first :val (= 2021))))
  (is (->> (parse-hex-str "38006F45291200") parse-packet first :packets (map :val) (= '(10 20))))
  (is (->> (parse-hex-str "EE00D40C823060") parse-packet first :packets (map :val) (= '(1 2 3))))
  (is (->> (parse-hex-str "8A004A801A8002F478") sol1 (= 16)))
  (is (->> (parse-hex-str "620080001611562C8802118E34") sol1 (= 12)))
  (is (->> (parse-hex-str "C0015000016115A2E0802F182340") sol1 (= 23)))
  (is (->> (parse-hex-str "A0016C880162017C3686B18A3D4780") sol1 (= 31))))

; ===================
; Part 2
; ===================
(defn eval-packet [packet]
  (let [sub-vals (map eval-packet (:packets packet))]       ; possibly empty
    (case (get-in packet [:header :type-id])
      0 (apply + sub-vals)
      1 (apply * sub-vals)
      2 (apply min sub-vals)
      3 (apply max sub-vals)
      4 (:val packet)
      5 (if (apply > sub-vals) 1 0)
      6 (if (apply < sub-vals) 1 0)
      7 (if (apply = sub-vals) 1 0))))

(defn sol2 [input]
  (->> input parse-packet first eval-packet))

(deftest part2-examples
  (is (->> (parse-hex-str "C200B40A82") sol2 (= 3)))
  (is (->> (parse-hex-str "04005AC33890") sol2 (= 54)))
  (is (->> (parse-hex-str "880086C3E88112") sol2 (= 7)))
  (is (->> (parse-hex-str "CE00C43D881120") sol2 (= 9)))
  (is (->> (parse-hex-str "D8005AC2A8F0") sol2 (= 1)))
  (is (->> (parse-hex-str "F600BC2D8F") sol2 (= 0)))
  (is (->> (parse-hex-str "9C005AC2F8F0") sol2 (= 0)))
  (is (->> (parse-hex-str "9C0141080250320F1802104A08") sol2 (= 1))))

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