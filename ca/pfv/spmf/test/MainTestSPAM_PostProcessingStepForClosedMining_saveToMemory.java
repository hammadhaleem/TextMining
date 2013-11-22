package ca.pfv.spmf.test;

import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.AlgoClaSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreatorStandard_Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Example of how to use the algorithm SPAM but executing a 
 * postprocessing step at the end, in order to find only the closed 
 * frequent patterns. The output is saved in the main memory
 *
 * @author agomariz
 */
public class MainTestSPAM_PostProcessingStepForClosedMining_saveToMemory {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // Load a sequence database
        double support = 0.5;

        boolean keepPatterns = true;
        boolean verbose = true;
        boolean findClosedPatterns = true;
        boolean executePruningMethods = false;

        AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
        IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

        SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

        //double relativeSupport = sequenceDatabase.loadFile(fileToPath("ExampleClaSP.txt"), support);
        double relativeSupport = sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"), support);
        //double relativeSupport = sequenceDatabase.loadFile(fileToPath("gazelle.txt"), support);

        AlgoClaSP algorithm = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns, executePruningMethods);


        //System.out.println(sequenceDatabase.toString());
        System.out.println("Support: " + support);
        algorithm.runAlgorithm(sequenceDatabase, keepPatterns, verbose, null);
        System.out.println(algorithm.getNumberOfFrequentPatterns() + " patterns found.");

        if (verbose && keepPatterns) {
            System.out.println(algorithm.printStatistics());
        }

        //uncomment if we want to see the Trie graphically
        //algorithm.showTree(null);
    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = MainTestClaSP_saveToFile.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
    }
}
