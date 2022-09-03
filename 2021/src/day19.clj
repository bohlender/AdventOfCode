(ns day19
  (:require [clojure.string :as string]
            [clojure.test :refer :all]
            [clojure.set :as set]))

(defn parse-coord [line]
  (->> (string/split line #",")
       (mapv #(Integer/parseInt %))))

(defn parse-scanner [lines]
  {:id    (->> lines first (re-find #"\d+") Integer/parseInt)
   :dists (->> lines rest (mapv parse-coord))})

(defn parse [s]
  (->> (string/split s #"\n{2,}")
       (map (comp parse-scanner string/split-lines))))

(defn parse-file [filename]
  (->> filename slurp parse))

; ==============================================================================
; Part 1
; ==============================================================================
(defn sq-distance [v1 v2]
  "Returns squared distance between v1 and v2."
  (->> (mapv (comp #(* % %) -) v2 v1)
       (reduce +)))

(defn distances [dist-fn v vs]
  "Returns set of distances between v and each element of vs according to a distance function dist-fn."
  (->> (for [other vs
             :when (not= v other)]
         (dist-fn v other))
       (into #{})))

(defn beacons [{dists :dists :as scanner}]
  "Returns a map from beacon positions to their square distances to other beacons (in the scanner's reach)."
  (->> (for [this dists]
         {this (distances sq-distance this dists)})
       (into {})))

(defn matching-positions [abs-map scanner]
  "Returns a collection of (relative position, absolute position) pairs that refer to the same position."
  (for [[rel-pos rel-sq-dists] (beacons scanner)
        [abs-pos abs-sq-dists] (:beacons abs-map)
        :when (-> (set/intersection rel-sq-dists abs-sq-dists)
                  count
                  (>= 11))]
    [rel-pos abs-pos]))

(def rotation-fns
  "A collection of all possible rotation functions in 3d space."
  [; Face +z (left-handed cartesian coords)
   (fn [[x y z]] [x y z])
   (fn [[x y z]] [y (- x) z])
   (fn [[x y z]] [(- x) (- y) z])
   (fn [[x y z]] [(- y) x z])
   ; Face -z
   (fn [[x y z]] [y x (- z)])
   (fn [[x y z]] [x (- y) (- z)])
   (fn [[x y z]] [(- y) (- x) (- z)])
   (fn [[x y z]] [(- x) y (- z)])
   ; Face +y
   (fn [[x y z]] [y z x])
   (fn [[x y z]] [x z (- y)])
   (fn [[x y z]] [(- y) z (- x)])
   (fn [[x y z]] [(- x) z y])
   ; Face -y
   (fn [[x y z]] [x (- z) y])
   (fn [[x y z]] [y (- z) (- x)])
   (fn [[x y z]] [(- x) (- z) (- y)])
   (fn [[x y z]] [(- y) (- z) x])
   ; Face +x
   (fn [[x y z]] [z x y])
   (fn [[x y z]] [z y (- x)])
   (fn [[x y z]] [z (- x) (- y)])
   (fn [[x y z]] [z (- y) x])
   ; Face -x
   (fn [[x y z]] [(- z) y x])
   (fn [[x y z]] [(- z) x (- y)])
   (fn [[x y z]] [(- z) (- y) (- x)])
   (fn [[x y z]] [(- z) (- x) y])])


(defn transform-fns [[rel-pos abs-pos]]
  "Returns a collection of functions that transform the coordinates rel-pos to abs-pos."
  (for [rot-fn rotation-fns]
    (let [delta (mapv - abs-pos (rot-fn rel-pos))]
      (fn [v] (mapv + (rot-fn v) delta)))))

(defn find-xf [matching-positions]
  "Returns the function that transforms relative to absolute positions."
  (when-not (empty? matching-positions)
    (let [candidate-xfs (transform-fns (first matching-positions))]
      ; Iteratively refine set of transform fn candidates until only the one that produces all matching coords remains
      (-> (reduce (fn [xfs [rel-pos abs-pos]]
                    (remove (fn [xf] (not= (xf rel-pos) abs-pos))
                            xfs))
                  candidate-xfs
                  (take 2 matching-positions))              ; no need to consider all matching-beacons
          first))))

(defn assemble-map [[s0 & ss :as scanners]]
  "Assembles the scanners' relative information into a map with absolutely positioned scanners & beacons."
  (loop [abs-map {:scanners #{[0 0 0]}                      ; First scanner acts as reference, i.e. it's beacon positions are absolute.
                  :beacons  (beacons s0)}
         worklist (into clojure.lang.PersistentQueue/EMPTY ss)]
    (if-let [cur-scanner (peek worklist)]
      ; if cur-scanner & abs-map can be aligned, transform cur-scanner into the absolute coordinate space and add it to abs-map
      (if-let [xf (find-xf (matching-positions abs-map cur-scanner))]
        (let [new-beacons (update-keys (beacons cur-scanner) xf)]
          (recur (-> abs-map
                     (update :scanners conj (xf [0 0 0]))
                     (update :beacons #(merge-with set/union % new-beacons)))
                 (pop worklist)))
        ; cur-scanner & abs-map may not align yet -> re-enqueue
        (recur abs-map (conj (pop worklist) cur-scanner)))
      abs-map)))

(defn sol1 [scanners]
  (->> scanners
       assemble-map
       :beacons
       count))

(deftest part1-examples
  (let [scanners (parse "--- scanner 0 ---\n404,-588,-901\n528,-643,409\n-838,591,734\n390,-675,-793\n-537,-823,-458\n-485,-357,347\n-345,-311,381\n-661,-816,-575\n-876,649,763\n-618,-824,-621\n553,345,-567\n474,580,667\n-447,-329,318\n-584,868,-557\n544,-627,-890\n564,392,-477\n455,729,728\n-892,524,684\n-689,845,-530\n423,-701,434\n7,-33,-71\n630,319,-379\n443,580,662\n-789,900,-551\n459,-707,401\n\n--- scanner 1 ---\n686,422,578\n605,423,415\n515,917,-361\n-336,658,858\n95,138,22\n-476,619,847\n-340,-569,-846\n567,-361,727\n-460,603,-452\n669,-402,600\n729,430,532\n-500,-761,534\n-322,571,750\n-466,-666,-811\n-429,-592,574\n-355,545,-477\n703,-491,-529\n-328,-685,520\n413,935,-424\n-391,539,-444\n586,-435,557\n-364,-763,-893\n807,-499,-711\n755,-354,-619\n553,889,-390\n\n--- scanner 2 ---\n649,640,665\n682,-795,504\n-784,533,-524\n-644,584,-595\n-588,-843,648\n-30,6,44\n-674,560,763\n500,723,-460\n609,671,-379\n-555,-800,653\n-675,-892,-343\n697,-426,-610\n578,704,681\n493,664,-388\n-671,-858,530\n-667,343,800\n571,-461,-707\n-138,-166,112\n-889,563,-600\n646,-828,498\n640,759,510\n-630,509,768\n-681,-892,-333\n673,-379,-804\n-742,-814,-386\n577,-820,562\n\n--- scanner 3 ---\n-589,542,597\n605,-692,669\n-500,565,-823\n-660,373,557\n-458,-679,-417\n-488,449,543\n-626,468,-788\n338,-750,-386\n528,-832,-391\n562,-778,733\n-938,-730,414\n543,643,-506\n-524,371,-870\n407,773,750\n-104,29,83\n378,-903,-323\n-778,-728,485\n426,699,580\n-438,-605,-362\n-469,-447,-387\n509,732,623\n647,635,-688\n-868,-804,481\n614,-800,639\n595,780,-596\n\n--- scanner 4 ---\n727,592,562\n-293,-554,779\n441,611,-461\n-714,465,-776\n-743,427,-804\n-660,-479,-426\n832,-632,460\n927,-485,-438\n408,393,-506\n466,436,-512\n110,16,151\n-258,-428,682\n-393,719,612\n-211,-452,876\n808,-476,-593\n-575,615,604\n-485,667,467\n-680,325,-822\n-627,-443,-432\n872,-547,-609\n833,512,582\n807,604,487\n839,-516,451\n891,-625,532\n-652,-548,-490\n30,-46,-14")]
    (is (-> (assemble-map scanners) :scanners (= #{[0 0 0] [68 -1246 -43] [-92 -2380 -20] [-20 -1133 1061] [1105 -1205 1229]})))
    (is (= (sol1 scanners) 79))))

; ==============================================================================
; Part 2
; ==============================================================================
(defn manhattan-distance [v1 v2]
  (->> (map (comp abs -) v2 v1)
       (reduce +)))

(defn sol2 [scanners]
  (let [positions (->> scanners assemble-map :scanners)]
    (->> (mapcat #(distances manhattan-distance % positions) positions)
         (apply max))))

(deftest part2-examples
  (let [scanners (parse "--- scanner 0 ---\n404,-588,-901\n528,-643,409\n-838,591,734\n390,-675,-793\n-537,-823,-458\n-485,-357,347\n-345,-311,381\n-661,-816,-575\n-876,649,763\n-618,-824,-621\n553,345,-567\n474,580,667\n-447,-329,318\n-584,868,-557\n544,-627,-890\n564,392,-477\n455,729,728\n-892,524,684\n-689,845,-530\n423,-701,434\n7,-33,-71\n630,319,-379\n443,580,662\n-789,900,-551\n459,-707,401\n\n--- scanner 1 ---\n686,422,578\n605,423,415\n515,917,-361\n-336,658,858\n95,138,22\n-476,619,847\n-340,-569,-846\n567,-361,727\n-460,603,-452\n669,-402,600\n729,430,532\n-500,-761,534\n-322,571,750\n-466,-666,-811\n-429,-592,574\n-355,545,-477\n703,-491,-529\n-328,-685,520\n413,935,-424\n-391,539,-444\n586,-435,557\n-364,-763,-893\n807,-499,-711\n755,-354,-619\n553,889,-390\n\n--- scanner 2 ---\n649,640,665\n682,-795,504\n-784,533,-524\n-644,584,-595\n-588,-843,648\n-30,6,44\n-674,560,763\n500,723,-460\n609,671,-379\n-555,-800,653\n-675,-892,-343\n697,-426,-610\n578,704,681\n493,664,-388\n-671,-858,530\n-667,343,800\n571,-461,-707\n-138,-166,112\n-889,563,-600\n646,-828,498\n640,759,510\n-630,509,768\n-681,-892,-333\n673,-379,-804\n-742,-814,-386\n577,-820,562\n\n--- scanner 3 ---\n-589,542,597\n605,-692,669\n-500,565,-823\n-660,373,557\n-458,-679,-417\n-488,449,543\n-626,468,-788\n338,-750,-386\n528,-832,-391\n562,-778,733\n-938,-730,414\n543,643,-506\n-524,371,-870\n407,773,750\n-104,29,83\n378,-903,-323\n-778,-728,485\n426,699,580\n-438,-605,-362\n-469,-447,-387\n509,732,623\n647,635,-688\n-868,-804,481\n614,-800,639\n595,780,-596\n\n--- scanner 4 ---\n727,592,562\n-293,-554,779\n441,611,-461\n-714,465,-776\n-743,427,-804\n-660,-479,-426\n832,-632,460\n927,-485,-438\n408,393,-506\n466,436,-512\n110,16,151\n-258,-428,682\n-393,719,612\n-211,-452,876\n808,-476,-593\n-575,615,604\n-485,667,467\n-680,325,-822\n-627,-443,-432\n872,-547,-609\n833,512,582\n807,604,487\n839,-516,451\n891,-625,532\n-652,-548,-490\n30,-46,-14")]
    (is (= (manhattan-distance [1105, -1205, 1229] [-92, -2380, -20]) 3621))
    (is (= (sol2 scanners) 3621))))

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