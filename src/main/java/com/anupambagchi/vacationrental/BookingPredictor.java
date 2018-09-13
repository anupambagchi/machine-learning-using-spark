package com.anupambagchi.vacationrental;

import com.anupambagchi.emailcampaign.CustomerOpenResponsePredictor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.List;

public class BookingPredictor {
    private static final Logger logger = LogManager.getLogger(BookingPredictor.class);

    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("BookingPredictor")
                .getOrCreate();

        // Load training data
        Dataset<Row> allRows = spark.read().format("csv")
                .option("header", "true")
                .option("inferSchema", "true")
                .option("delimiter", "\t")
                .load("data/vacation_rental_booking_dataset.tsv");

        allRows.printSchema();
        allRows.show(10);

        List<String> colList = new ArrayList<String>();
        colList.add();
        Seq<String> colSeq = scala.collection.JavaConverters.asScalaIteratorConverter(colList.iterator()).asScala().toSeq();
        data=data.na().fill(word, colSeq);

        /*
        // Split initial RDD into two... [60% training data, 40% testing data].
        JavaRDD<LabeledPoint>[] splits = data.randomSplit(new double[]{0.6, 0.4}, 11L);
        JavaRDD<LabeledPoint> training = splits[0].cache();
        JavaRDD<LabeledPoint> test = splits[1];

        // Run training algorithm to build the model.
        LogisticRegressionModel model = new LogisticRegressionWithLBFGS()
                .setNumClasses(2)
                .run(training.rdd());

        // Clear the prediction threshold so the model will return probabilities
        model.clearThreshold();

        // Compute raw scores on the test set.
        JavaPairRDD<Object, Object> predictionAndLabels = test.mapToPair(p ->
                new Tuple2<>(model.predict(p.features()), p.label()));

        // Get evaluation metrics.
        BinaryClassificationMetrics metrics =
                new BinaryClassificationMetrics(predictionAndLabels.rdd());

        // Precision by threshold
        JavaRDD<Tuple2<Object, Object>> precision = metrics.precisionByThreshold().toJavaRDD();
        System.out.println("Precision by threshold: " + precision.collect());

        // Recall by threshold
        JavaRDD<?> recall = metrics.recallByThreshold().toJavaRDD();
        System.out.println("Recall by threshold: " + recall.collect());

        // F Score by threshold
        JavaRDD<?> f1Score = metrics.fMeasureByThreshold().toJavaRDD();
        System.out.println("F1 Score by threshold: " + f1Score.collect());

        JavaRDD<?> f2Score = metrics.fMeasureByThreshold(2.0).toJavaRDD();
        System.out.println("F2 Score by threshold: " + f2Score.collect());

        // Precision-recall curve
        JavaRDD<?> prc = metrics.pr().toJavaRDD();
        System.out.println("Precision-recall curve: " + prc.collect());

        // Thresholds
        JavaRDD<Double> thresholds = precision.map(t -> Double.parseDouble(t._1().toString()));

        // ROC Curve
        JavaRDD<?> roc = metrics.roc().toJavaRDD();
        System.out.println("ROC curve: " + roc.collect());

        // AUPRC
        System.out.println("Area under precision-recall curve = " + metrics.areaUnderPR());

        // AUROC
        System.out.println("Area under ROC = " + metrics.areaUnderROC());

        // Save and load model
        model.save(sc, "target/tmp/LogisticRegressionModel");
        LogisticRegressionModel.load(sc, "target/tmp/LogisticRegressionModel");
        // $example off$

        sc.stop();

        /*
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
        */
    }
}
