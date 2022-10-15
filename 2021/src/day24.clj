(ns day24
  (:require [clojure.string :as string]
            [clojure.test :refer :all])
  (:import (com.microsoft.z3 Context Status)))

(defn parse-op [s]
  (if (re-matches #"-?\d+" s)
    (Integer/parseInt s)
    (keyword s)))

(defn parse-instr [line]
  (let [[mnem & ops] (string/split line #"\s")]
    {:mnem (keyword mnem)
     :ops  (keep parse-op ops)}))

(defn parse [s]
  (->> (string/split-lines s)
       (map parse-instr)))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defrecord SymState [ctx path-constraint w x y z inputs])

(defn val [state op]
  (if (keyword? op)
    (get state op)
    (.mkInt (:ctx state) (.longValue op))))

(defn sym-ex-instr [{ctx :ctx :as state}, {mnem :mnem, [lhs rhs] :ops :as instr}]
  (case mnem
    :inp (let [fresh-var (.mkFreshConst ctx "var" (.mkIntSort ctx))]
           (-> state
               (update :inputs conj fresh-var)
               (update :path-constraint conj (.mkLe ctx (.mkInt ctx 1) fresh-var))
               (update :path-constraint conj (.mkLe ctx fresh-var (.mkInt ctx 9)))
               (assoc lhs fresh-var)))
    :add (assoc state lhs (.mkAdd ctx (into-array [(val state lhs) (val state rhs)])))
    :mul (assoc state lhs (.mkMul ctx (into-array [(val state lhs) (val state rhs)])))
    :div (-> state
             (update :path-constraint conj (.mkNot ctx (.mkEq ctx (val state rhs) (.mkInt ctx 0))))
             (assoc lhs (.mkDiv ctx (val state lhs) (val state rhs))))
    :mod (-> state
             (update :path-constraint conj (.mkGe ctx (val state lhs) (.mkInt ctx 0)))
             (update :path-constraint conj (.mkGt ctx (val state rhs) (.mkInt ctx 0)))
             (assoc lhs (.mkMod ctx (val state lhs) (val state rhs))))
    :eql (assoc state lhs (.mkITE ctx (.mkEq ctx (val state lhs) (val state rhs))
                                  (.mkInt ctx 1)
                                  (.mkInt ctx 0)))
    (throw (Exception. (format "Unsupported instruction: %s" instr)))))


(defn sym-ex-prog [instrs]
  (let [ctx (Context.)
        zero (.mkInt ctx 0)
        init-state (->SymState ctx [] zero zero zero zero [])]
    (reduce sym-ex-instr
            init-state
            instrs)))

(defn mk-simplex-params [ctx]
  (let [params (.mkParams ctx)]
    (.add params "smt.arith.solver" 2)                      ; simplex- instead of default lra-based solver
    params))

(defn find-model-number [instrs maximize?]
  (let [state (sym-ex-prog instrs)
        ctx (:ctx state)
        s (.mkSolver ctx)]
    (.setParameters s (mk-simplex-params ctx))

    ; Core constraints
    (.add s (into-array (:path-constraint state)))
    (.add s (into-array [(.mkEq ctx (:z state) (.mkInt ctx 0))]))

    ; Repeatedly search for a (more optimal) solution
    (.push s)
    (loop [digits nil]
      (if (= (.check s) Status/SATISFIABLE)
        (let [model (.getModel s)
              var-digits (mapv (fn [var] [var (.evaluate model var false)]) (:inputs state))
              not-current-digits (->> var-digits
                                      (map (fn [[var digit]] (.mkEq ctx var digit)))
                                      into-array
                                      (.mkAnd ctx)
                                      (.mkNot ctx))
              ; Note: This constraint works because valid model numbers don't contain zeros
              better-or-eq-digits (->> var-digits
                                       (map (fn [[var digit]] (if maximize?
                                                                (.mkGe ctx var digit)
                                                                (.mkLe ctx var digit))))
                                       into-array
                                       (.mkAnd ctx))]
          (.pop s)
          (.push s)
          (.add s (into-array [not-current-digits better-or-eq-digits]))
          (recur (mapv second var-digits)))
        (->> digits
             (mapv (fn [digit] (.getInt digit)))
             (string/join "")
             Long/parseLong)))))

(defn sol1 [instrs]
  (find-model-number instrs true))

; ==============================================================================
; Part 2
; ==============================================================================
(defn sol2 [instrs]
  (find-model-number instrs false))

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