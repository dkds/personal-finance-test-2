package com.dkds.util

import kotlin.properties.Delegates

data class DataPoint(val features: List<Double>, val label: Double)

class GradientBoosting(
    private val numIterations: Int,
    private val learningRate: Double
) {
    private val weakLearners = mutableListOf<DecisionStump>()

    fun train(trainingData: List<DataPoint>) {
        // Initialize predictions with zeros
        val predictions = DoubleArray(trainingData.size)

        for (iteration in 0 until numIterations) {
            // Calculate residuals (negative gradient)
            val residuals = DoubleArray(trainingData.size) { i ->
                -calculateGradient(predictions[i], trainingData[i].label)
            }

            // Train a decision stump on the residuals
            val decisionStump = DecisionStump()
            decisionStump.train(trainingData, residuals)

            // Update predictions using the decision stump's output
            updatePredictions(predictions, decisionStump)

            // Add the decision stump as a weak learner
            weakLearners.add(decisionStump)
        }
    }

    fun predict(features: List<Double>): Double {
        var prediction = 0.0

        for (learner in weakLearners) {
            prediction += learningRate * learner.predict(features)
        }

        return prediction
    }

    private fun calculateGradient(prediction: Double, actual: Double): Double {
        // Gradient for squared loss (change this for other loss functions)
        return 2.0 * (prediction - actual)
    }

    private fun updatePredictions(predictions: DoubleArray, learner: DecisionStump) {
        for (i in predictions.indices) {
            predictions[i] += learningRate * learner.predict(learner.trainingData[i].features)
        }
    }
}

class DecisionStump {
    var splitFeatureIndex by Delegates.notNull<Int>()
    var splitThreshold by Delegates.notNull<Double>()
    private val classLabels = mutableSetOf<Double>()
    lateinit var trainingData: List<DataPoint>

    fun train(data: List<DataPoint>, residuals: DoubleArray) {
        trainingData = data

        // Find the best split for a decision stump
        val numFeatures = data[0].features.size
        var bestError = Double.POSITIVE_INFINITY

        for (featureIndex in 0 until numFeatures) {
            data.forEach { dataPoint ->
                classLabels.add(dataPoint.label)
            }

            classLabels.forEach { threshold ->
                val leftLabels = mutableListOf<Double>()
                val rightLabels = mutableListOf<Double>()

                data.forEachIndexed { dataIndex, dataPoint ->
                    if (dataPoint.features[featureIndex] <= threshold) {
                        leftLabels.add(residuals[dataIndex])
                    } else {
                        rightLabels.add(residuals[dataIndex])
                    }
                }

                val error = calculateError(leftLabels) + calculateError(rightLabels)

                if (error < bestError) {
                    bestError = error
                    splitFeatureIndex = featureIndex
                    splitThreshold = threshold
                }
            }
        }
    }

    fun predict(features: List<Double>): Double {
        // Make a binary classification based on the split
        return if (features[splitFeatureIndex] <= splitThreshold) {
            -1.0
        } else {
            1.0
        }
    }

    private fun calculateError(labels: List<Double>): Double {
        // Calculate the squared error for binary classification (change for other loss functions)
        return labels.sumOf { it * it }
    }
}

fun test() {
    // Sample data
    val trainingData = listOf(
        DataPoint(listOf(1.0), -1.0),
        DataPoint(listOf(2.0), 1.0),
        DataPoint(listOf(3.0), -1.0),
        DataPoint(listOf(4.0), 1.0),
        DataPoint(listOf(5.0), -1.0)
    )

    // Create and train the Gradient Boosting model
    val gbModel = GradientBoosting(numIterations = 10, learningRate = 0.1)
    gbModel.train(trainingData)

    // Make predictions
    val testData = listOf(2.5, 4.5)
    val prediction = gbModel.predict(testData)
    println("Prediction for $testData: $prediction")
}
