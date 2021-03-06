package io.hydrosphere.spark_ml_serving.regression

import io.hydrosphere.spark_ml_serving.common.{DataUtils, _}
import org.apache.spark.ml.regression.DecisionTreeRegressionModel
import org.apache.spark.ml.tree.Node

class LocalDecisionTreeRegressionModel(override val sparkTransformer: DecisionTreeRegressionModel)
  extends LocalPredictionModel[DecisionTreeRegressionModel] {

}

object LocalDecisionTreeRegressionModel extends LocalModel[DecisionTreeRegressionModel] {
  override def load(metadata: Metadata, data: LocalData): DecisionTreeRegressionModel = {
    createTree(metadata, data)
  }

  def createTree(metadata: Metadata, data: LocalData): DecisionTreeRegressionModel = {
    val ctor = classOf[DecisionTreeRegressionModel].getDeclaredConstructor(classOf[String], classOf[Node], classOf[Int])
    ctor.setAccessible(true)
    val inst = ctor.newInstance(
      metadata.uid,
      DataUtils.createNode(0, metadata, data),
      metadata.numFeatures.getOrElse(0).asInstanceOf[java.lang.Integer]
    )
    inst
      .setFeaturesCol(metadata.paramMap("featuresCol").asInstanceOf[String])
      .setPredictionCol(metadata.paramMap("predictionCol").asInstanceOf[String])
    inst
      .set(inst.seed, metadata.paramMap("seed").toString.toLong)
      .set(inst.cacheNodeIds, metadata.paramMap("cacheNodeIds").toString.toBoolean)
      .set(inst.maxDepth, metadata.paramMap("maxDepth").toString.toInt)
      .set(inst.labelCol, metadata.paramMap("labelCol").toString)
      .set(inst.minInfoGain, metadata.paramMap("minInfoGain").toString.toDouble)
      .set(inst.checkpointInterval, metadata.paramMap("checkpointInterval").toString.toInt)
      .set(inst.minInstancesPerNode, metadata.paramMap("minInstancesPerNode").toString.toInt)
      .set(inst.maxMemoryInMB, metadata.paramMap("maxMemoryInMB").toString.toInt)
      .set(inst.maxBins, metadata.paramMap("maxBins").toString.toInt)
      .set(inst.impurity, metadata.paramMap("impurity").toString)
  }

  override implicit def getTransformer(transformer: DecisionTreeRegressionModel): LocalTransformer[DecisionTreeRegressionModel] = new LocalDecisionTreeRegressionModel(transformer)
}
