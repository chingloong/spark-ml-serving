package io.hydrosphere.spark_ml_serving.preprocessors

import io.hydrosphere.spark_ml_serving.common.DataUtils._
import io.hydrosphere.spark_ml_serving.common._
import org.apache.spark.ml.feature.HashingTF
import org.apache.spark.mllib.feature.{HashingTF => HTF}

import scala.collection.mutable

class LocalHashingTF(override val sparkTransformer: HashingTF) extends LocalTransformer[HashingTF] {
  override def transform(localData: LocalData): LocalData = {
    localData.column(sparkTransformer.getInputCol) match {
      case Some(column) =>
        val htf = new HTF(sparkTransformer.getNumFeatures).setBinary(sparkTransformer.getBinary)
        val newData = column.data.map{m =>
          htf.transform(m.asInstanceOf[List[_]]).toList
        }
        localData.withColumn(LocalDataColumn(sparkTransformer.getOutputCol, newData))
      case None => localData
    }
  }
}

object LocalHashingTF extends LocalModel[HashingTF] {
  override def load(metadata: Metadata, data: LocalData): HashingTF = {
    new HashingTF(metadata.uid)
      .setInputCol(metadata.paramMap("inputCol").asInstanceOf[String])
      .setOutputCol(metadata.paramMap("outputCol").asInstanceOf[String])
      .setBinary(metadata.paramMap("binary").asInstanceOf[Boolean])
      .setNumFeatures(metadata.paramMap("numFeatures").asInstanceOf[Number].intValue())
  }

  override implicit def getTransformer(transformer: HashingTF): LocalTransformer[HashingTF] = new LocalHashingTF(transformer)
}
