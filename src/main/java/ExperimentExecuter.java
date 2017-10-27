/**
 * Created by fshaikh on 10/26/2017.
 */
public class ExperimentExecuter {
    public static void main(String[] args) {
        // Call the main() method of MyClass
        String[] datasets = new String[] {"src/main/data/Music.arff"};

        try {
            ExperimentExample.main(datasets);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
