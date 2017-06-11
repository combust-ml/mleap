<a href="http://mleap-docs.combust.ml"><img src="logo.png" alt="MLeap Logo" width="176" height="70" /></a>

[![Gitter](https://badges.gitter.im/combust/mleap.svg)](https://gitter.im/combust/mleap?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Build Status](https://travis-ci.org/combust/mleap.svg?branch=master)](https://travis-ci.org/combust/mleap)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ml.combust.mleap/mleap-base_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ml.combust.mleap/mleap-base_2.11)

Deploying machine learning data pipelines and algorithms should not be a time-consuming or difficult task. MLeap allows data scientists and engineers to deploy machine learning pipelines from Spark and Scikit-learn to a portable format and execution engine.

## Documentation

Documentation is available at [mleap-docs.combust.ml](http://mleap-docs.combust.ml).

Read [Serializing a Spark ML Pipeline and Scoring with MLeap](https://github.com/combust-ml/mleap/wiki/Serializing-a-Spark-ML-Pipeline-and-Scoring-with-MLeap) to gain a full sense of what is possible.

## Introduction

Using the MLeap execution engine and serialization format, we provide a performant, portable and easy-to-integrate production library for machine learning data pipelines and algorithms.

For portability, we build our software on the JVM and only use serialization formats that are widely-adopted.

We also provide a high level of integration with existing technologies.

Our goals for this project are:

1. Allow Researchers/Data Scientists and Engineers to continue to build data pipelines and train algorithms with Spark and Scikit-Learn
2. Extend Spark/Scikit/TensorFlow by providing ML Pipelines serialization/deserialization to/from a common framework (Bundle.ML)
3. Use MLeap Runtime to execute your pipeline and algorithm without dependenices on Spark or Scikit (numpy, pandas, etc)

## Overview

1. Core execution engine implemented in Scala
2. [Spark](http://spark.apache.org/), PySpark and Scikit-Learn support
3. Export a model with Scikit-learn or Spark and execute it using the MLeap Runtime (without dependencies on the Spark Context, or sklearn/numpy/pandas/etc)
4. Choose from 3 portable serialization formats (JSON, Protobuf, and Mixed)
5. Implement your own custom data types and transformers for use with MLeap data frames and transformer pipelines
6. Extensive test coverage with full parity tests for Spark and MLeap pipelines
7. Optional Spark transformer extension to extend Spark's default transformer offerings

<img src="assets/images/single-runtime.jpg" alt="Unified Runtime"/>

## Requirements

MLeap is cross-compiled for Scala 2.10 and 2.11. Because we depend
heavily on Typesafe config for MLeap, we only support Java 8 at the
moment. This means the following configurations should be possible:

2. Scala 2.10 and Java 8
4. Scala 2.11 and Java 8

## Setup

### Link with Maven or SBT

MLeap is cross-compiled for Scala 2.10 and 2.11, so just replace 2.10 with 2.11 wherever you see it if you are running Scala version 2.11 and using a POM file for dependency management. Otherwise, use the `%%` operator if you are using SBT and the correct Scala version will be used.

#### SBT

```sbt
libraryDependencies += "ml.combust.mleap" %% "mleap-runtime" % "0.7.0"
```

#### Maven

```pom
<dependency>
    <groupId>ml.combust.mleap</groupId>
    <artifactId>mleap-runtime_2.10</artifactId>
    <version>0.7.0</version>
</dependency>
```

### For Spark Integration

#### SBT

```sbt
libraryDependencies += "ml.combust.mleap" %% "mleap-spark" % "0.7.0"
```

#### Maven

```pom
<dependency>
    <groupId>ml.combust.mleap</groupId>
    <artifactId>mleap-spark_2.10</artifactId>
    <version>0.7.0</version>
</dependency>
```

### Spark Packages

```bash
$ bin/spark-shell --packages ml.combust.mleap:mleap-spark_2.11:0.7.0
```

### PySpark Integration

Install MLeap from pypy
```bash
$ pip install mleap
```


## Using the Library

For more complete examples, see our other Git repository: [MLeap Demos](https://github.com/combust-ml/mleap-demo)

### Create and Export a Spark Pipeline

The first step is to create our pipeline in Spark. For our example we will manually build a simple Spark ML pipeline.


```scala
import org.apache.spark.ml.feature.{StringIndexerModel, Binarizer}

// User out-of-the-box Spark transformers like you normally would
val stringIndexer = new StringIndexerModel(uid = "si", labels = Array("hello", "MLeap")).
  setInputCol("test_string").
  setOutputCol("test_index")

val binarizer = new Binarizer(uid = "bin").
  setThreshold(0.5).
  setInputCol("test_double").
  setOutputCol("test_bin")

// Use the MLeap utility method to directly create an org.apache.spark.ml.PipelineModel

import org.apache.spark.ml.mleap.SparkUtil

// Without needing to fit an org.apache.spark.ml.Pipeline
val pipeline = SparkUtil.createPipelineModel(uid = "pipeline", Array(stringIndexer, binarizer))

import ml.combust.bundle.BundleFile
import ml.combust.mleap.spark.SparkSupport._
import resource._

// Optionally yield from here to get the try back from serializing
// The try will indicate if serialization was successful
for(modelFile <- managed(BundleFile("jar:file:/tmp/simple-spark-pipeline.zip"))) {
  pipeline.writeBundle.
    // save our pipeline to a zip file
    // we can save a file to any supported java.nio.FileSystem
    save(modelFile)
}
```

Spark pipelines are not meant to be run outside of Spark. They require a DataFrame and therefore a SparkContext to run. These are expensive data structures and libraries to include in a project. With MLeap, there is no dependency on Spark to execute a pipeline. MLeap dependencies are lightweight and we use fast data structures to execute your ML pipelines.

### PySpark Integration

Import the MLeap library in your PySpark job

```python
import mleap.pyspark
from mleap.pyspark.spark_support import SimpleSparkSerializer
```

### Create and Export a Scikit-Learn Pipeline

```python
import pandas as pd

# Load scikit-learn mleap extensions
import mleap.sklearn.pipeline
import mleap.sklearn.preprocessing.data
from mleap.sklearn.preprocessing.data import NDArrayToDataFrame

# Load the LabelEncoder from Mleap
from mleap.sklearn.preprocessing.data import FeatureExtractor, LabelEncoder, ReshapeArrayToN1

# Load scikit-learn transformers and models
from sklearn.preprocessing import OneHotEncoder
from sklearn.pipeline import Pipeline

data = pd.DataFrame(['a', 'b', 'c'], columns=['col_a'])

continuous_features = ['col_a']

feature_extractor_tf = FeatureExtractor(input_features=continuous_features, 
                                         output_vector='imputed_features', 
                                         output_vector_items=continuous_features)

# Label Encoder for x1 Label 
label_encoder_tf = LabelEncoder(input_features=feature_extractor_tf.output_vector_items,
                               output_features='{}_label_le'.format(continuous_features[0]))

# Reshape the output of the LabelEncoder to N-by-1 array
reshape_le_tf = ReshapeArrayToN1()

# Vector Assembler for x1 One Hot Encoder
one_hot_encoder_tf = OneHotEncoder(sparse=False)
one_hot_encoder_tf.mlinit(input_features = label_encoder_tf.output_features, 
                          output_features = '{}_label_one_hot_encoded'.format(continuous_features[0]))

one_hot_encoder_pipeline_x0 = Pipeline([
                                         (feature_extractor_tf.name, feature_extractor_tf),
                                         (label_encoder_tf.name, label_encoder_tf),
                                         (reshape_le_tf.name, reshape_le_tf),
                                         (one_hot_encoder_tf.name, one_hot_encoder_tf)
                                        ])

one_hot_encoder_pipeline_x0.mlinit()
one_hot_encoder_pipeline_x0.fit_transform(data)
one_hot_encoder_pipeline_x0.serialize_to_bundle('/tmp', 'mleap-scikit-test-pipeline', init=True)

# array([[ 1.,  0.,  0.],
#        [ 0.,  1.,  0.],
#        [ 0.,  0.,  1.]])
```

### Load and Transform Using MLeap

Becuase we export Spark and Scikit-learn pipelines to a standard format, we can use either our Spark-trained pipeline or our Scikit-learn pipeline from the previous steps to demonstrate usage of MLeap in this section. The choice is yours!

```scala
import ml.combust.bundle.BundleFile
import ml.combust.mleap.runtime.MleapSupport._
import resource._

// load the Spark pipeline we saved in the previous section
val bundle = (for(bundleFile <- managed(BundleFile("jar:file:/tmp/simple-spark-pipeline.zip"))) yield {
  bundleFile.loadMleapBundle().get
}).opt.get

// create a simple LeapFrame to transform
import ml.combust.mleap.runtime.{Row, LeapFrame, LocalDataset}
import ml.combust.mleap.runtime.types._
import ml.combust.mleap.tensor.Tensor


// MLeap makes extensive use of monadic types like Try
val schema = StructType(StructField("test_string", StringType()),
  StructField("test_double", TensorType(DoubleType()))).get
val data = LocalDataset(Row("hello", Tensor.denseVector(Array(0.6))),
  Row("MLeap", Tensor.denseVector(Array(0.2))))
val frame = LeapFrame(schema, data)

// transform the dataframe using our pipeline
val mleapPipeline = bundle.root
val frame2 = mleapPipeline.transform(frame).get
val data2 = frame2.dataset

// get data from the transformed rows and make some assertions
assert(data2(0).getDouble(2) == 0.0) // string indexer output
assert(data2(0).getTensor[Double](3).get(0) == 1.0) // binarizer output

// the second row
assert(data2(1).getDouble(2) == 1.0)
assert(data2(1).getTensor[Double](3).get(0) == 0.0)
```

## Documentation

For more documentation, please see our [wiki](https://github.com/combust-ml/mleap/wiki), where you can learn to:

1. implement custom transformers that will work with Spark, MLeap and Scikit-learn
2. implement custom data types to transform with Spark and MLeap pipelines
3. transform with blazing fast speeds using optimized row-based transformers
4. serialize MLeap data frames to various formats like avro, json, and a custom binary format
5. implement new serialization formats for MLeap data frames
6. work through several demonstration pipelines which use real-world data to create predictive pipelines
7. supported Spark transformers
8. supported Scikit-learn transformers
9. custom transformers provided by MLeap

## Contributing

* Write documentation.
* Write a tutorial/walkthrough for an interesting ML problem
* Contribute an Estimator/Transformer from Spark
* Use MLeap at your company and tell us what you think
* Make a feature request or report a bug in github
* Make a pull request for an existing feature request or bug report
* Join the discussion of how to get MLeap into Spark as a dependency. Talk with us on Gitter (see link at top of README.md)

## Contact Information

* Hollin Wilkins (hollin@combust.ml)
* Mikhail Semeniuk (mikhail@combust.ml)
* Ram Sriharsha (ram@databricks.com)

## License

See LICENSE and NOTICE file in this repository.

Copyright 2016 Combust, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.