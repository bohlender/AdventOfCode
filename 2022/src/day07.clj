(ns day07
  (:require [clojure.test :refer :all]
            [clojure.string :as string]))

(defrecord Command [name args output-lines])

(defn command-from-lines [[cmdline & output-lines]]
  (let [[name & args] (string/split cmdline #"\s")]
    (->Command name args output-lines)))

(defn parse-commands [s]
  (->> (string/split s #"\$")
       (remove empty?)
       (map (comp command-from-lines string/split-lines string/trim))))

(defn augment-tree [{:keys [tree path]} {:keys [name args output-lines]}]
  "Augments a directory tree at a given path according to the recorded command I/O."
  (case name
    "cd" (let [[dirname] args]
           (case dirname
             "/" {:tree tree
                  :path []}
             ".." {:tree tree
                   :path (pop path)}
             (let [next-path (conj path dirname)]
               {:tree (update-in tree next-path merge {})
                :path next-path})))
    "ls" {:tree (->> output-lines
                     (map #(string/split % #"\s"))
                     (reduce (fn [tree [info name]]
                               (if (= info "dir")
                                 (update-in tree (conj path name) merge {})
                                 (assoc-in tree (conj path name) (Integer/parseInt info))))
                             tree))
          :path path}))

(defn parse [s]
  (->> (parse-commands s)
       (reduce augment-tree {:tree {}, :path []})
       :tree))

; ==============================================================================
; Part 1
; ==============================================================================
(def tree? map?)

(defn size [tree]
  (->> tree
       (reduce (fn [sum [name val]] (+ sum (if (tree? val)
                                             (size val)
                                             val)))
               0)))

(defn dirs-rec [tree]
  (->> (vals tree)
       (filter tree?)
       (mapcat dirs-rec)
       (into [tree])))

(defn sol1 [tree]
  (->> (dirs-rec tree)
       (filter (fn [tree] (<= (size tree) 100000)))
       (map size)
       (reduce +)))

(deftest part1-examples
  (let [input (parse "$ cd /\n$ ls\ndir a\n14848514 b.txt\n8504156 c.dat\ndir d\n$ cd a\n$ ls\ndir e\n29116 f\n2557 g\n62596 h.lst\n$ cd e\n$ ls\n584 i\n$ cd ..\n$ cd ..\n$ cd d\n$ ls\n4060174 j\n8033020 d.log\n5626152 d.ext\n7214296 k\n")]
    (is (= (sol1 input) 95437))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn sol2 [tree]
  (let [used (size tree)
        free (- 70000000 used)
        needed (- 30000000 free)]
    (->> (dirs-rec tree)
         (map size)
         (filter #(<= needed %))
         sort
         first)))

(deftest part2-examples
  (let [input (parse "$ cd /\n$ ls\ndir a\n14848514 b.txt\n8504156 c.dat\ndir d\n$ cd a\n$ ls\ndir e\n29116 f\n2557 g\n62596 h.lst\n$ cd e\n$ ls\n584 i\n$ cd ..\n$ cd ..\n$ cd d\n$ ls\n4060174 j\n8033020 d.log\n5626152 d.ext\n7214296 k\n")]
    (is (= (sol2 input) 24933642))))

; ==============================================================================
; Main
; ==============================================================================
(defn -main [& args]
  (if (not= 1 (count args))
    (println "Invalid number of parameters. Expecting one input file.")
    (let [[filename] args
          input (parse (slurp filename))]
      (println "First:" (sol1 input))
      (println "Second:" (sol2 input)))))
