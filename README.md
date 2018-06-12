# oneclassclassifiers
Implementations of one-class classifiers for use with MOA. Currently compatible with MOA 17.06.

+ **Autoencoder**. A neural network that attempts to reconstruct the input. The autoencoder's structure is an input layer with one neuron for each non-class attribute, a hidden layer of two neurons and an output layer with one neuron for each non-class attribute. Partially modeled on Albert Bifet's Perceptron.java, also found in the MOA project. Backpropagation implementation based on Matt Mazur's excellent tutorial on this topic [2]. Includes a dependency on the [Apache Commons Math linear algebra library](https://commons.apache.org/proper/commons-math/userguide/linear.html).

+ **Streaming Half-Space Trees**. Implements Tan et al.'s Streaming Half-Space Trees one-class classifier [3].

+ **Nearest Neighbour Description**. Implements David Tax's Nearest Neighbour Description, a one-class classifier based on the nearest neighbour method [4] - Section 3.4.2.

MOA (Massive Online Analysis) [1] is a Java-based, open source framework for data stream mining. More details can be found on its [website](http://moa.cms.waikato.ac.nz/) and it can be found on GitHub as well (https://github.com/waikato/moa).

This project has a DOI: [![DOI](https://zenodo.org/badge/134278543.svg)](https://zenodo.org/badge/latestdoi/134278543)

REFERENCES

[1] A. Bifet, G. Holmes, R. Kirkby, and B. Pfahringer, “Moa: Massive online analysis,” J. Mach. Learn. Res., vol. 11, no. May, pp. 1601–1604, 2010.

[2] M. Mazur, “A Step by Step Backpropagation Example,” Internet: https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/ 2015 \[May 21, 2018\]

[3] S. C. Tan, K. M. Ting, and T. F. Liu, “Fast anomaly detection for streaming data,” in IJCAI Proceedings-International Joint Conference on Artificial Intelligence, 2011, vol. 22, no. 1, pp. 1511–1516.

[4] D. M. J. Tax, “One-Class Classification: Concept-learning in the absence of counter-examples.” PhD Thesis, Delft University of Technology, 2001.
