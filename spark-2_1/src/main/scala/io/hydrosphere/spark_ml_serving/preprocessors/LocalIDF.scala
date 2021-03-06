package io.hydrosphere.spark_ml_serving.preprocessors

import io.hydrosphere.spark_ml_serving.common.DataUtils._
import io.hydrosphere.spark_ml_serving.common._
import org.apache.spark.ml.feature.IDFModel
import org.apache.spark.mllib.feature.{IDFModel => OldIDFModel}
import org.apache.spark.mllib.linalg.{Vector => OldVector, Vectors => OldVectors}

class LocalIDF(override val sparkTransformer: IDFModel) extends LocalTransformer[IDFModel] {
  override def transform(localData: LocalData): LocalData = {
    val idf = sparkTransformer.idf

    localData.column(sparkTransformer.getInputCol) match {
      case Some(column) =>
        val newData = column.data.mapToMlLibVectors.map { vector =>
          val n = vector.size
          val values = vector.values
          val newValues = new Array[Double](n)
          var j = 0
          while (j < n) {
            newValues(j) = values(j) * idf(j)
            j += 1
          }
          newValues.toList
        }
        localData.withColumn(LocalDataColumn(sparkTransformer.getOutputCol, newData))

      case None => localData
    }
  }
}

object LocalIDF extends LocalModel[IDFModel] {
  override def load(metadata: Metadata, data: LocalData): IDFModel = {
    val idfValues = data.column("idf").get.data.head.
      asInstanceOf[Map[String, Any]].
      getOrElse("values", List()).
      asInstanceOf[List[Double]].toArray
    val vector = OldVectors.dense(idfValues)
    val oldIDFconstructor = classOf[OldIDFModel].getDeclaredConstructor(classOf[OldVector])
    oldIDFconstructor.setAccessible(true)
    val oldIDF = oldIDFconstructor.newInstance(vector)

    val const = classOf[IDFModel].getDeclaredConstructor(classOf[String], classOf[OldIDFModel])
    val idf = const.newInstance(metadata.uid, oldIDF)
    idf
      .setInputCol(metadata.paramMap("inputCol").asInstanceOf[String])
      .setOutputCol(metadata.paramMap("outputCol").asInstanceOf[String])
      .set(idf.minDocFreq, metadata.paramMap("minDocFreq").asInstanceOf[Number].intValue())
  }

  override implicit def getTransformer(transformer: IDFModel): LocalTransformer[IDFModel] = new LocalIDF(transformer)
}
