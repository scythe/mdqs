
(defn echo [x] (do (prn x) x))

(defn readmap [fn fl] 
	(with-open [r (clojure.java.io/reader fl)] (doall (map fn (line-seq r)))))

(defn zip-county-frac [s] (let [l (clojure.string/split s #",")] (list (second l) (first l) (nth l 4))))

(defn cbsa-county [s] (list (first (clojure.string/split s #"," 2)) (clojure.string/replace (or (re-find #",\d\d,\d\d\d," s) "") #"," "")))

(defn pairs-to-hash [f l] (into (hash-map) (map #(hash-map (first %1) (f (second %1))) l)))

(defn new-city [name] {:name name :counties [] :zips {}})

(def cities (dissoc (pairs-to-hash new-city (readmap #(clojure.string/split %1 #" " 2) "cbsas_of_interest.txt")) ""))

(defn add-county [cities entry]
	(let [cbsa (first entry) county (second entry)]
	(let [city (cities cbsa)]
	(if (nil? city) 
		cities
		(assoc cities cbsa (assoc city :counties (conj (city :counties) county)))
	))))

(def cities-with-counties (reduce add-county cities (readmap cbsa-county "msas_fips_counties.csv")))

(def cities-by-county (reduce (fn [res citykv] (reduce #(assoc %1 %2 (key citykv)) res ((val citykv) :counties))) {} cities-with-counties))

(defn add-zip [cities entry]
	(let [county (first entry) zip (second entry) res_frac (read-string (nth entry 2))]
	(let [cbsa (cities-by-county county)]
	(if (nil? cbsa)
		cities
	(let [city (cities cbsa)]
	(if (nil? city)
		cities
		(assoc cities cbsa (assoc city :zips (assoc (city :zips) zip (+ res_frac (or ((city :zips) zip) 0)))))
	))))))

(def cities-with-zips (reduce add-zip cities-with-counties (readmap zip-county-frac "zip_code_to_county.csv")))

;; some zip codes like 20777 cross two major metropolitan areas! how annoying!
(def cities-by-zip (reduce (fn [res citykv] (reduce #(assoc %1 %2 (conj (or (%1 %2) []) (key citykv))) res (keys ((val citykv) :zips)))) {} cities-with-zips))

(defn add-pop [cities entry]
	(let [zip (first entry) pop (read-string (second entry)) dens (read-string (nth entry 3))]
	(let [cbsas (cities-by-zip zip)]
	(if (nil? cbsas)
		cities
	(reduce (fn [cities cbsa]
			(let [frac (((cities cbsa) :zips) zip)]
			(assoc cities cbsa (assoc (cities cbsa) :zips (assoc ((cities cbsa) :zips) zip (list dens (* pop frac)))))))
		cities
		cbsas)
	)))) 

(def cities-with-zip-pops-raw (reduce add-pop cities-with-zips (readmap #(clojure.string/split %1 #",") "zipcode_pop_area.csv"))) 

;; some zip codes don't have population entries, possibly because nobody lives there
(def cities-with-zip-pops 
	(into {} (map (fn [[cbsa city]] [cbsa (assoc city :zips (into {} (filter #(list? (second %1)) (city :zips))))]) 
	              cities-with-zip-pops-raw)))

(defn set-tot-pop [city] (assoc city :tot (reduce + 0 (map #(second (second %1)) (city :zips)))))

(defn pop-fracs [dps tot]
	(reverse
	(map #(list (first %1) (/ (second %1) tot))
	     (reduce (fn [res dens_pop] (conj res (list (first dens_pop) (+ (second (first res)) (second dens_pop)))))
	             '((0 0))
                     (sort-by first < dps))
	)))

(defn quantile_fn [dfs] (fn [q] ;; uses an awkward way of traversing the list
	(let [[below above] (split-with #(> q (second %1)) dfs)]
	(let [pre (or (last below) '(0 0))
	      nxt (or (first above) '(0 1))]
	(if (= 1 (second nxt))
		(second pre)
	(let [run (- (first nxt) (first pre))
	      rise (- (second nxt) (second pre))
	      x0 (first pre)
	      y0 (second pre)]
	(+ x0 (* (- q y0) (/ run rise)))))))))

(defn set-quantiles [city] (assoc city :qfn (quantile_fn (pop-fracs (vals (city :zips)) (city :tot)))))

(def cities-with-quantiles (into {} (map (fn [[k v]] [k (set-quantiles (set-tot-pop v))]) cities-with-zip-pops)))

(doseq [[cbsa city] cities-with-quantiles]
	(prn (city :name))
	(prn (city :tot))
	(doseq [q (range 0.125 1 0.125)] (prn ((city :qfn) q))))

