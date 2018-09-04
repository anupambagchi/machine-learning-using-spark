package com.anupambagchi.emailcampaign;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class CustomerOpenResponsePredictor {
    private static final Logger logger = LogManager.getLogger(CustomerOpenResponsePredictor.class);

    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("CustomerOpenResponsePredictor")
                .getOrCreate();

        // Load training data
        Dataset<Row> allrows = spark.read().format("csv")
                .option("header", "true")
                .option("inferSchema", "true")
                .load("data/emailcampaign_dataset.csv");

        StringIndexer eventIndexer = new StringIndexer().setHandleInvalid("keep").setInputCol("event").setOutputCol("eventIndex");
        StringIndexer subsourceIndexer = new StringIndexer().setHandleInvalid("keep").setInputCol("sub_source").setOutputCol("sub_sourceIndex");
        StringIndexer camptypeIndexer = new StringIndexer().setHandleInvalid("keep").setInputCol("camptype").setOutputCol("camptypeIndex");

        Dataset<Row> indexedRows1 = eventIndexer.fit(allrows).transform(allrows);
        Dataset<Row> indexedRows2 = subsourceIndexer.fit(indexedRows1).transform(indexedRows1);
        Dataset<Row> indexedRows = camptypeIndexer.fit(indexedRows2).transform(indexedRows2);

        indexedRows.printSchema();

        Dataset<Row>[] trainingAndTest = indexedRows.randomSplit(new double[] {0.8, 0.2});

        Dataset<Row> trainingSet = trainingAndTest[0];
        Dataset<Row> testSet = trainingAndTest[1];

        // "riid", "event", "event_captured_dt", "cnts", "aq_dt", "sub_source", "tenure", "camptype", "optout"
        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{"eventIndex", "cnts", "sub_sourceIndex", "tenure", "camptypeIndex"})
                .setOutputCol("features");


        LogisticRegression lr = new LogisticRegression()
                .setMaxIter(10)
                .setRegParam(0.3)
                .setElasticNetParam(0.8)
                .setFeaturesCol("features")   // setting features column
                .setLabelCol("optout");       // setting label column

        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[] {assembler,lr});

        PipelineModel lrModel = pipeline.fit(trainingSet);

        Dataset<Row> predictions = lrModel.transform(testSet);

        // [sub_source, event_captured_dt, aq_dt, sub_sourceIndex, rawPrediction, cnts, probability, camptypeIndex,
        // riid, tenure, event, camptype, prediction, eventIndex, optout, features]
        int correctCount = 0, allCount = 0;
        for (Row r : predictions.select("prediction","optout").collectAsList()) {
            if (Math.abs((r.getDouble(0) - (double)r.getInt(1))) < 0.001)
                correctCount++;
            allCount++;
        }

        System.out.println("Percentage correct = " + 100*((double)correctCount/(double)allCount));

        spark.stop();
    }
}
