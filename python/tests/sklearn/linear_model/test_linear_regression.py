#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import json
import os
import shutil
import unittest
import uuid

import numpy as np
from numpy.testing import assert_array_equal
from sklearn.datasets import make_regression
from sklearn.model_selection import train_test_split

from mleap.sklearn.linear_model import LinearRegression


class TestLinearRegression(unittest.TestCase):
    def setUp(self):
        self.tmp_dir = "/tmp/mleap.python.tests/{}".format(uuid.uuid1())

        if os.path.exists(self.tmp_dir):
            shutil.rmtree(self.tmp_dir)

        os.makedirs(self.tmp_dir)

    def tearDown(self):
        shutil.rmtree(self.tmp_dir)

    def test_linear_regression_fails_with_multiple_targets(self):
        linear_regression = LinearRegression()
        linear_regression.mlinit(input_features='features', prediction_column='prediction')

        X, y = make_regression(n_features=100, n_targets=2)
        X_train, X_test, y_train, y_test = train_test_split(X, y)
        linear_regression.fit(X_train, y_train)

        with self.assertRaises(ValueError):
            linear_regression.serialize_to_bundle(self.tmp_dir, linear_regression.name)

    def test_linear_regression_serializes_1d_coef(self):
        linear_regression = LinearRegression()
        linear_regression.mlinit(input_features='features', prediction_column='prediction')

        X, y = make_regression(n_features=100, n_targets=1)
        X_train, X_test, y_train, y_test = train_test_split(X, y)
        linear_regression.fit(X_train, y_train)

        self.assertEqual((100,), linear_regression.coef_.shape)
        linear_regression.serialize_to_bundle(self.tmp_dir, linear_regression.name)

        with open("{}/{}.node/model.json".format(self.tmp_dir, linear_regression.name)) as json_data:
            model = json.load(json_data)

        self.assertEqual('linear_regression', model['op'])

        # assert coefficient matrix has shape (100,)
        self.assertEqual(100, len(model['attributes']['coefficients']['double']))
        self.assertEqual(1, len(model['attributes']['coefficients']['shape']['dimensions']))
        self.assertEqual(100, model['attributes']['coefficients']['shape']['dimensions'][0]['size'])

        # assert intercept scalar exists
        self.assertTrue(model['attributes']['intercept']['double'] is not None)

    def test_linear_regression_deserializes_1d_coef(self):
        linear_regression = LinearRegression()
        linear_regression.mlinit(input_features='features', prediction_column='prediction')

        X, y = make_regression(n_features=100, n_targets=1)
        X_train, X_test, y_train, y_test = train_test_split(X, y)
        linear_regression.fit(X_train, y_train)

        self.assertEqual((100,), linear_regression.coef_.shape)
        linear_regression.serialize_to_bundle(self.tmp_dir, linear_regression.name)

        node_name = "{}.node".format(linear_regression.name)
        linear_regression_ds = LinearRegression()
        linear_regression_ds = linear_regression_ds.deserialize_from_bundle(self.tmp_dir, node_name)

        # TODO: Update deserialization code to preserve the shape of `coef_` so this flatten is unnecessary
        expected = linear_regression.predict(X_test)
        actual = linear_regression_ds.predict(X_test).flatten()

        assert_array_equal(expected, actual)

    def test_linear_regression_serializes_2d_coef(self):
        linear_regression = LinearRegression()
        linear_regression.mlinit(input_features='features', prediction_column='prediction')

        X, y = make_regression(n_features=100, n_targets=1)
        X_train, X_test, y_train, y_test = train_test_split(X, y)
        linear_regression.fit(X_train, np.reshape(y_train, (-1, 1)))

        self.assertEqual((1, 100), linear_regression.coef_.shape)
        linear_regression.serialize_to_bundle(self.tmp_dir, linear_regression.name)

        with open("{}/{}.node/model.json".format(self.tmp_dir, linear_regression.name)) as json_data:
            model = json.load(json_data)

        self.assertEqual('linear_regression', model['op'])

        # assert coefficient matrix has shape (100,)
        self.assertEqual(100, len(model['attributes']['coefficients']['double']))
        self.assertEqual(1, len(model['attributes']['coefficients']['shape']['dimensions']))
        self.assertEqual(100, (model['attributes']['coefficients']['shape']['dimensions'][0]['size']))

        # assert intercept scalar exists
        self.assertTrue(model['attributes']['intercept']['double'] is not None)

    def test_linear_regression_deserializes_2d_coef(self):
        linear_regression = LinearRegression()
        linear_regression.mlinit(input_features='features', prediction_column='prediction')

        X, y = make_regression(n_features=100, n_targets=1)
        X_train, X_test, y_train, y_test = train_test_split(X, y)
        linear_regression.fit(X_train, np.reshape(y_train, (-1, 1)))

        self.assertEqual((1, 100), linear_regression.coef_.shape)
        linear_regression.serialize_to_bundle(self.tmp_dir, linear_regression.name)

        node_name = "{}.node".format(linear_regression.name)
        linear_regression_ds = LinearRegression()
        linear_regression_ds = linear_regression_ds.deserialize_from_bundle(self.tmp_dir, node_name)

        expected = linear_regression.predict(X_test)
        actual = linear_regression_ds.predict(X_test)

        assert_array_equal(expected, actual)