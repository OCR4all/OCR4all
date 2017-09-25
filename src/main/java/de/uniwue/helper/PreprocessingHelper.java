package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import de.uniwue.config.ProjectDirConfig;

/**
 * Helper class for preprocessing pages, which also calls the ocrubus-nlbin function 
 * 
 *  */
public class PreprocessingHelper {

    /**
     * Object to access project directory configuration
     */
    private ProjectDirConfig projDirConf;

    public PreprocessingHelper(String projectDir) {
        projDirConf = new ProjectDirConfig(projectDir);
    }

    public OutputStream preprocessPage(List<String> identifier) throws ExecuteException, IOException {
        System.out.println("Preprocessing gestartet");
        OutputStream outputStream = null;

        if (!new File(projDirConf.BINR_IMG_DIR).exists())
            new File(projDirConf.BINR_IMG_DIR).mkdir();
        if (!new File(projDirConf.GRAY_IMG_DIR).exists())
            new File(projDirConf.GRAY_IMG_DIR).mkdir();
        new File(projDirConf.GRAY_IMG_DIR).mkdir();

        for (String id : identifier) {
            CommandLine cmdLine = CommandLine.parse("ocropus-nlbin " + projDirConf.ORIG_IMG_DIR + id
                    + projDirConf.IMG_EXT + " -o "+projDirConf.PREPROC_DIR);

            DefaultExecutor executor = new DefaultExecutor();
            executor.execute(cmdLine);
            executor.setStreamHandler(new PumpStreamHandler(outputStream));

            //hardcoded 0001 because of Ocrupus
            if (new File(projDirConf.PREPROC_DIR+"0001"+projDirConf.BIN_IMG_EXT).exists())
                new File(projDirConf.PREPROC_DIR+"0001"+projDirConf.BIN_IMG_EXT).renameTo(new File(projDirConf.BINR_IMG_DIR+id+projDirConf.IMG_EXT));
            if (new File(projDirConf.PREPROC_DIR+"0001"+projDirConf.GRAY_IMG_EXT).exists())
                new File(projDirConf.PREPROC_DIR+"0001"+projDirConf.GRAY_IMG_EXT).renameTo(new File(projDirConf.GRAY_IMG_DIR+id+projDirConf.IMG_EXT));

        }

        return outputStream;

    }
}
