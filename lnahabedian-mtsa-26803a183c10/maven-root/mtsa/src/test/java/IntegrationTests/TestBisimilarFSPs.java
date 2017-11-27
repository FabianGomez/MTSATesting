package IntegrationTests;

import MTSAClient.ac.ic.doc.mtsa.MTSCompiler;
import MTSTools.ac.ic.doc.mtstools.model.MTS;
import MTSTools.ac.ic.doc.mtstools.model.impl.LTSSimulationSemantics;
import ltsa.lts.CompositionExpression;
import ltsa.lts.FileInput;
import ltsa.lts.LTSCompiler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by mbrassesco on 6/28/17.
 */
@RunWith(Parameterized.class)
public class TestBisimilarFSPs {

    private static final String resourceFolder = "./BisimilarFSP";

    private File ltsFile;


    public TestBisimilarFSPs(File getFile) {
        ltsFile = getFile;

    }


    @Parameterized.Parameters(name = "{index}: {0}")
    public static List<File> controllerFiles() throws IOException {
        List<File> allFiles = LTSTestsUtils.getFiles(resourceFolder);
        return allFiles;
    }


    @Test(timeout = 60000)
    public void testLTSBisimilar() throws Exception {
        FileInput lts;

        MTS<Long, String> compiledA = MTSCompiler.getInstance().compileMTS("C", ltsFile);
        MTS<Long, String> compiledB = MTSCompiler.getInstance().compileMTS("ExpectedC", ltsFile);
        if (compiledA != null & compiledB != null) {
            LTSSimulationSemantics simulationSemantics = new LTSSimulationSemantics();
            boolean result = simulationSemantics.isARefinement(compiledB, compiledA);
            assertTrue(result);
        } else fail("No bisimilarity Controller Specs to synthesise, file: " + ltsFile.getName());


    }

    CompositionExpression getProcessFromCompilation(String name, LTSCompiler compiled) {
        Hashtable<String, CompositionExpression> machines = compiled.getComposites();
        for (int i = 0; i < machines.size(); i++) {
            CompositionExpression elementMain = machines.get(i);
            if (name == elementMain.getName().getName()) {
                return elementMain;
            }
        }
        return null;
    }

}
