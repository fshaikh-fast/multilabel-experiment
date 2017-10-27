/**
 * Created by fshaikh on 10/26/2017.
 */
public class ExperimentExecuter {
    public static void main(String[] args) {
        // Call the main() method of MyClass
        String[] datasets = new String[] {
                "src/main/data/bibtex/bibtex.arff",
                "src/main/data/scene/scene.arff",
                "src/main/data/yeast/yeast.arff",
                "src/main/data/emotions/emotions.arff",
                "src/main/data/CAL500/CAL500.arff",
                "src/main/data/enron/enron.arff",
                "src/main/data/flags/flags.arff",
                "src/main/data/medical/medical.arff"
        };

        try {
            ExperimentExample.main(datasets);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
