package org.simbrain.network.smile.classifiers

import org.simbrain.network.smile.ClassificationAlgorithm
import org.simbrain.util.UserParameter
import org.simbrain.util.getOneHotMat
import smile.classification.Classifier
import smile.classification.KNN
import smile.math.matrix.Matrix
import smile.validation.metric.Accuracy

/**
 * Wrapper for Smile KNN Classifier.
 */
class KNNClassifier @JvmOverloads constructor(inputSize: Int = 4, outputSize: Int = 4): ClassificationAlgorithm(inputSize,
    outputSize) {

    @UserParameter(label = "K", order = 10)
    var k = 2

    override var model: Classifier<DoubleArray>? = null

    override val name: String = "K Nearest Neighbors"

    override fun fit(inputs: Array<DoubleArray>, targets: IntArray) {
        model = KNN.fit(inputs, targets, k)
        val pred = model?.predict(inputs)
        setAccuracyLabel(Accuracy.of(targets, pred))
    }

    override fun predict(input: DoubleArray): Int {
        return model?.predict(input) ?: -1
    }

    override fun getOutputVector(result: Int): Matrix {
        if (result > outputSize) {
            throw IllegalArgumentException("Prediction of ${result} > output size of ${outputSize}")
        }
        if (result == -1) {
            return Matrix(outputSize, 1)
        } else {
            return getOneHotMat(result-1, outputSize, 1.0)
        }
    }

    override fun copy(): ClassificationAlgorithm {
        return KNNClassifier(inputSize, outputSize).also {
            it.k = k
        }
    }

    // Kotlin hack to support "static method in superclass"
    companion object {
        @JvmStatic
        fun getTypes(): List<Class<*>> {
            return ClassificationAlgorithm.getTypes()
        }
    }

}