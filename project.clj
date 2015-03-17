(defproject deppy "0.1.0"
  :description "Generates a graph of the dependency hierarchy of a set of clojure/clojurescript files. This uses the clojure.tools.namespace library for namespace parsing and Sankey google chart for graph generation using."
  :url "https://github.com/raphaelboukara/deppy"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths ["src/resources/"]
  :dependencies [[org.clojure/tools.namespace "0.2.7"]]
  :eval-in-leiningen true)
