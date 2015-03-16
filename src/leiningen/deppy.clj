(ns leiningen.deppy
  (:require
    [clojure.java.io :as io]
    (clojure.tools.namespace
      [dependency :as ns-dep]
      [file :as ns-file]
      [track :as ns-track]))
  (:import
    java.io.File))


(def default-options
  {:path "target/ns-deppy.html"
   :ignore-ns #{}})


(defn- clojurescript-file?
  "Returns true if the file represents a normal ClojureScript source file."
  [^File file]
  (and (.isFile file)
       (.endsWith (.getName file) ".cljs")))


(defn- find-sources-in-dir
  "Searches recursively under dir for source files (.clj and .cljs).
  Returns a sequence of File objects, in breadth-first sort order."
  [dir]
  (->>
    (io/file dir)
    file-seq
    (filter #(or (clojurescript-file? %)
                 (ns-file/clojure-file? %)))
    (sort-by #(.getAbsolutePath ^File %))))


(defn- find-sources
  "Finds a list of source files located in the given directories."
  [dirs]
  (->>
    dirs
    (filter identity)
    (map find-sources-in-dir)
    flatten))

(defn- format-graph [graph]
  (mapv #(do [(first %) (vec (second %))]) graph))

(defn- file-deps
  "Calculates the dependency graph of the namespaces in the given files."
  [files]
  (->>
    files
    (ns-file/add-files {})
    ::ns-track/deps
    :dependencies))



(defn read-file []
  (with-open [r (io/input-stream "header.html")] 
         (loop [c (.read r)] 
           (if (not= c -1)
             (do 
               (print (char c)) 
               (recur (.read r)))))))

(def header-file (-> "header.html" io/resource io/file))
(def footer-file (-> "footer.html" io/resource io/file))

(defn file-to-string [file]
  (slurp file))

(defn content-data []
  (str
    "{source: 'Microsoft', target: 'Amazon', type: 'licensing'},"
    "{source: 'Microsoft', target: 'HTC', type: 'licensing'},"
    "{source: 'Samsung', target: 'Apple', type: 'suit'},"
    "{source: 'Motorola', target: 'Apple', type: 'suit'},"
    "{source: 'Nokia', target: 'Apple', type: 'resolved'}"))

(defn write-file []
  (with-open [w (io/writer  "./deppy.html" :append true)]
    (.write w (str (file-to-string header-file) 
                   (content-data) 
                   (file-to-string footer-file)))))

(defn deppy
  "Generate a dependency graph of the namespaces in the project."
  [project & args]
  (let [source-files (find-sources (concat (:source-paths project) args))
        graph (file-deps source-files)
        format (format-graph graph)]
    #_(mapv println format)
    (println "build html...")
    (write-file)
  ))
