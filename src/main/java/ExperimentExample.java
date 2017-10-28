
import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.CC;
import meka.classifiers.multilabel.MULAN;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.OptionUtils;
import meka.events.LogEvent;
import meka.events.LogListener;
import meka.experiment.DefaultExperiment;
import meka.experiment.Experiment;
import meka.experiment.datasetproviders.DatasetProvider;
import meka.experiment.datasetproviders.LocalDatasetProvider;
import meka.experiment.datasetproviders.MultiDatasetProvider;
import meka.experiment.evaluationstatistics.KeyValuePairs;
import meka.experiment.evaluators.CrossValidation;
import meka.experiment.evaluators.RepeatedRuns;
import meka.experiment.events.*;
import meka.experiment.statisticsexporters.*;
import weka.core.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates an experiment using BR and CC classifiers, evaluating them on the
 * user-supplied datasets.
 */
public class ExperimentExample {
    public static void main(String[] args) throws Exception {
        if (args.length == 0)
            throw new IllegalArgumentException("Requirement arguments: <dataset1> [<dataset2> [...]]");

        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        String tmpDir = outputDir.getCanonicalPath();
        System.out.println("Using output dir: " + tmpDir);

        Experiment exp = new DefaultExperiment();
        // classifiers
        exp.setClassifiers(new MultiLabelClassifier[]{

                new meka.classifiers.multilabel.MULAN()
        });
        // datasets
        List<File> files = new ArrayList<File>();
        for (String f : args)
            files.add(new File(f));
        LocalDatasetProvider dp1 = new LocalDatasetProvider();
        dp1.setDatasets(files.toArray(new File[files.size()]));
        LocalDatasetProvider dp2 = new LocalDatasetProvider();
        dp2.setDatasets(new File[]{
                new File("src/main/data/solar_flare.arff"),
        });
        MultiDatasetProvider mdp = new MultiDatasetProvider();
        mdp.setProviders(new DatasetProvider[]{dp1});
        exp.setDatasetProvider(mdp);
        // output of metrics
        KeyValuePairs sh = new KeyValuePairs();
        sh.setFile(new File(tmpDir + "/mekaexp.txt"));
        sh.getFile().delete();  // remove old run
        exp.setStatisticsHandler(sh);
        // evaluation
        RepeatedRuns eval = new RepeatedRuns();
        eval.setUpperRuns(1);
        eval.setEvaluator(new CrossValidation());
        exp.setEvaluator(eval);
        // stage
        exp.addExecutionStageListener(new ExecutionStageListener() {
            @Override
            public void experimentStage(ExecutionStageEvent e) {
                System.err.println("[STAGE] " + e.getStage());
            }
        });
        // iterations
        exp.addIterationNotificationListener(new IterationNotificationListener() {
            @Override
            public void nextIteration(IterationNotificationEvent e) {
                System.err.println("[ITERATION] " + Utils.toCommandLine(e.getClassifier()) + " --> " + e.getDataset().relationName());
            }
        });
        // statistics
        exp.addStatisticsNotificationListener(new StatisticsNotificationListener() {

            public void statisticsAvailable(StatisticsNotificationEvent e) {
                System.err.println("[STATISTICS] #" + e.getStatistics().size());
            }
        });
        // log events
        exp.addLogListener(new LogListener() {
            @Override
            public void logMessage(LogEvent e) {
                System.err.println("[LOG] " + e.getSource().getClass().getName() + ": " + e.getMessage());
            }
        });
        // output options
        System.out.println("Setup:\n" + OptionUtils.toCommandLine(exp) + "\n");
        // execute
        String msg = exp.initialize();
        System.out.println("initialize: " + msg);
        if (msg != null)
            return;
        msg = exp.run();
        System.out.println("run: " + msg);
        msg = exp.finish();
        System.out.println("finish: " + msg);
        // export them
        TabSeparated tabsepAgg = new TabSeparated();
        tabsepAgg.setFile(new File(tmpDir + "/mekaexp-agg.tsv"));
        SimpleAggregate aggregate = new SimpleAggregate();
        aggregate.setSuffixMean("");
        aggregate.setSuffixStdDev(" (stdev)");
        aggregate.setSkipCount(true);
        aggregate.setSkipMean(false);
        aggregate.setSkipStdDev(false);
        aggregate.setExporter(tabsepAgg);
        TabSeparated tabsepFull = new TabSeparated();
        tabsepFull.setFile(new File(tmpDir + "/mekaexp-full.tsv"));
        TabSeparatedMeasurement tabsepHL = new TabSeparatedMeasurement();
        tabsepHL.setMeasurement("Hamming loss");
        tabsepHL.setFile(new File(tmpDir + "/mekaexp-HL.tsv"));
        TabSeparatedMeasurement tabsepZOL = new TabSeparatedMeasurement();
        tabsepZOL.setMeasurement("ZeroOne loss");
        tabsepZOL.setFile(new File(tmpDir + "/mekaexp-ZOL.tsv"));
        MultiExporter multiexp = new MultiExporter();
        multiexp.setExporters(new EvaluationStatisticsExporter[]{aggregate, tabsepFull, tabsepHL, tabsepZOL});
        multiexp.addLogListener(new LogListener() {
            @Override
            public void logMessage(LogEvent e) {
                System.err.println("[EXPORT] " + e.getSource().getClass().getName() + ": " + e.getMessage());
            }
        });
        System.out.println(OptionUtils.toCommandLine(multiexp));
        msg = multiexp.export(exp.getStatistics());
        System.out.println("export: " + msg);
    }
}
