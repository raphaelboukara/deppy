(ns leiningen.deppy
  (:require
    [clojure.string :as string]
    [clojure.java.io :as io]
    (clojure.tools.namespace
      [dependency :as ns-dep]
      [file :as ns-file]
      [track :as ns-track]))
  (:import
    java.io.File))

(def options (atom
  {:ignore-ns []
   :root-ns ""}))

(defn- clojurescript-file? [^File file]
  (and (.isFile file)
       (.endsWith (.getName file) ".cljs")))

(defn- find-sources-in-dir [dir]
  (->>
    (io/file dir)
    file-seq
    (filter #(or (clojurescript-file? %)
                 (ns-file/clojure-file? %)))
    (sort-by #(.getAbsolutePath ^File %))))

(defn- find-sources [dirs]
  (->>
    dirs
    (filter identity)
    (map find-sources-in-dir)
    flatten))

(defn- format-file-graph [[key deps]]
  (map #(do [key %]) deps))

(defn- format-filter-graph [[key dep]]
  (and (not (nil? (re-find (re-pattern (str (:root-ns @options) "\\.")) (str key))))
       (not (nil? (re-find (re-pattern (str (:root-ns @options) "\\.")) (str dep))))
       (empty? (filter #(not (nil? (re-find (re-pattern %) (str key)))) (:ignore-ns @options)))
       (empty? (filter #(not (nil? (re-find (re-pattern %) (str dep)))) (:ignore-ns @options)))))

(defn- format-base-graph [[key deps]]
  (do [key (vec deps)]))

(defn- format-graph [graph]
  (->> 
    graph
    (map format-base-graph)
    (map format-file-graph)
    flatten
    (partition 2)
    (filter format-filter-graph)))

(defn- stringify-sub-graph [[key dep]]
  (str "['" key "', '" dep "', 1]"))

(defn- stringify-graph [graph]
  (->>
    graph 
    (map stringify-sub-graph)
    (string/join ", ")))

(defn- file-deps [files]
  (->>
    files
    (ns-file/add-files {})
    ::ns-track/deps
    :dependencies))

(defn- get-content [filename]
  (-> 
    filename 
    io/resource 
    io/file))

(def header-file (get-content "header.html"))
(def footer-file (get-content "footer.html"))

(defn- file-to-string [file]
  (slurp file))

(defn- html-graph [graph]
  (with-open [w (io/writer  "./deppy.html")]
    (.write w (str (file-to-string header-file) 
                   graph 
                   (file-to-string footer-file)))))

(defn deppy [project & args]
  (let [source-files (find-sources (concat (:source-paths project) args))]
    (println "build html...")
    (swap! options merge (:deppy project))
    (-> 
      source-files 
      file-deps 
      format-graph 
      stringify-graph 
      html-graph)))