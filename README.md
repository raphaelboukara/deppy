# deppy

Generates a graph of the dependency hierarchy of a set of clojure files. This
uses the [clojure.tools.namespace](https://github.com/clojure/tools.namespace)
library for namespace parsing and [Sankey google chart](https://google-developers.appspot.com/chart/interactive/docs/gallery/sankey)
for graph generation using.

## Usage

[![Clojars Project](http://clojars.org/deppy/latest-version.svg)](http://clojars.org/deppy)


    $ lein deppy
    
##Options:
Deppy may be controlled with additional options under the `:deppy`
key in the project map. The available options and their default values are:

```clojure
:deppy {
    :ignore-ns ["myproject.xxx" "myproject.yyy"]
    :root-ns "myproject"}
```

## License

MIT
