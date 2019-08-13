package de.uniwue.feature.pageXML;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primaresearch.dla.page.Page;
import org.primaresearch.dla.page.io.xml.PageXmlInputOutput;
import org.primaresearch.dla.page.io.xml.XmlPageReader;
import org.primaresearch.dla.page.layout.physical.ContentObject;
import org.primaresearch.dla.page.layout.physical.ContentObjectProcessor;
import org.primaresearch.dla.page.layout.physical.text.TextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.Glyph;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import org.primaresearch.dla.page.layout.physical.text.impl.Word;
import org.primaresearch.io.FormatVersion;
import org.primaresearch.io.xml.SchemaModelParser;
import org.primaresearch.io.xml.XmlFormatVersion;
import org.primaresearch.io.xml.XmlModelAndValidatorProvider;
import org.primaresearch.io.xml.XmlValidator;
import org.primaresearch.io.xml.variable.XmlVariableFileReader;
import org.primaresearch.shared.variable.VariableMap;
import org.primaresearch.text.filter.TextFilter;
import org.primaresearch.text.filter.TextFilter.TextObjectTypeFilterCallback;

public class PageConverter {

    private String gtsidToSet = null;
    private FormatVersion targetformat = null;
    private VariableMap textFilterRules = null;

    /** Map [schemaVersion, validator] */
    private Map<XmlFormatVersion, XmlValidator> validators = new HashMap<XmlFormatVersion, XmlValidator>();

    /** Map [schemaVersion, schemaFilePath] */
    private Map<XmlFormatVersion, URL> schemaSources = new HashMap<XmlFormatVersion, URL>();

    /** Map [schemaVersion, schemaFilePath] */
    private List<XmlFormatVersion> defaultSchemas = new ArrayList<XmlFormatVersion>();

    /** Map [schemaVersion, schemaParser] */
    private Map<XmlFormatVersion, SchemaModelParser> schemaParsers = new HashMap<XmlFormatVersion, SchemaModelParser>();


    private XmlFormatVersion latestSchemaVersion;

    /**
     * Main function
     * @param args Command line arguments - call with empty array to print usage help to stdout
     */
 /*   public static void main(String[] args) {
        if (args.length == 0) {
            showUsage();
            return;
        }

        PageConverter converter = new PageConverter();

        //Parse arguments
        String sourceFilename = null;
        String targetFilename = null;
        String gtsidPattern = null;
        String textFilterRuleFile = null;
        for (int i=0; i<args.length; i++) {
            if ("-source-xml".equals(args[i])) {
                i++;
                sourceFilename = args[i];
            }
            else if ("-target-xml".equals(args[i])) {
                i++;
                targetFilename = args[i];
            }
            else if ("-set-gtsid".equals(args[i])) {
                i++;
                gtsidPattern = args[i];
            }
            else if ("-convert-to".equals(args[i])) {
                i++;
                converter.setTargetSchema(args[i]);
            }
            else if ("-text-filter".equals(args[i])) {
                i++;
                textFilterRuleFile = args[i];
            }
            else {
                System.err.println("Unknown argument: "+args[i]);
            }
        }

        //Set GtsID
        if (gtsidPattern != null)
            converter.setGtsId(gtsidPattern, sourceFilename);

        //Text filter
        if (textFilterRuleFile != null) {
            XmlVariableFileReader reader = new XmlVariableFileReader();
            try {
                converter.setTextFilterRules(reader.read(new File(textFilterRuleFile).toURI().toURL()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Run conversion
        converter.run(sourceFilename, targetFilename);
    }*/

    /**
     * Print usage help to stdout
     */
    private static void showUsage() {
        System.out.println("PAGE Converter");
        System.out.println("");
        System.out.println("PRImA Research Lab, University of Salford, UK");
        System.out.println("");
        System.out.println("Arguments:");
        System.out.println("");
        System.out.println("  -source-xml <XML file>        PAGE XML file to convert.");
        System.out.println("");
        System.out.println("  -target-xml <XML file>        Output PAGE XML file.");
        System.out.println("");
        System.out.println("  -convert-to <schema version>  Target PAGE schema version. (optional)");
        System.out.println("              Available versions:");
        System.out.println("                 LATEST");
        System.out.println("                 2013-07-15");
        System.out.println("                 2010-03-19");
        System.out.println("");
        System.out.println("  -set-gtsid <ID|prefix[start,end]>   To set the the GtsId field. (optional)");
        System.out.println("         Usage:");
        System.out.println("              Use <ID> to set a specific GtsId.");
        System.out.println("              Use <prefix[start,end]> to extract the GtsId from the");
        System.out.println("              filename of -source-xml.");
        System.out.println("         Examples:");
        System.out.println("              -set-gtsid 00236178     Given ID");
        System.out.println("              -set-gtsid pc-[0,7]     Prefix + first 8 characters of filename");
        System.out.println("");
        System.out.println("  -text-filter <XML file>   Applies filter to the text content. (optional)");
        System.out.println("         For instructions on how to define filter rules see the user guide.");
    }

    /**
     * Runs the conversion
     * @param sourceFilename File path of input PAGE XML
     * @param targetFilename File path to output PAGE XML
     */
    public void run(String sourceFilename, String targetFilename) {

        System.out.println("now in Converter, sourceFilename is: " + sourceFilename);
        System.out.println("and targeFilenamee : " + targetFilename);

        //Load
        Page page = null;
        try {
            page = PageXmlInputOutput.readPage(sourceFilename);
            XmlModelAndValidatorProvider validatorProvider = null;
            XmlPageReader reader = null;
        } catch (Exception e) {
            System.err.println("Could not load source PAGE XML file: "+sourceFilename);
            e.printStackTrace();
            return;
        }

        //Set GtsId
        if (gtsidToSet != null && !gtsidToSet.isEmpty()) {
            try {
                page.setGtsId(gtsidToSet);
            } catch (Exception exc) {
                System.err.println("Could not set the GtsId");
                exc.printStackTrace();
            }
        }

        //Text filter
        if (textFilterRules != null) {
            runTextFilter(textFilterRules, page);
        }

        //Convert to specified schema version
        if (targetformat != null) {
            try {
                //ConverterHub.convert(page, XmlInputOutput.getInstance().getFormatModel(targetformat));
                page.setFormatVersion(PageXmlInputOutput.getInstance().getFormatModel(targetformat));
            } catch(Exception exc) {
                System.err.println("Could not convert to target XML schema format.");
                exc.printStackTrace();
            }
        }

        //Write
        try {
            if (!PageXmlInputOutput.writePage(page, targetFilename))
                System.err.println("Error writing target PAGE XML file");
        } catch (Exception e) {
            System.err.println("Could not save target PAGE XML file: "+targetFilename);
            e.printStackTrace();
        }
    }

    /**
     * Applies a set of filter rules to all text elements of the given page.
     * The type of the target object (region, line, word, glyph) can be
     * specified per rule.
     *
     * @param textFilterRules A collection of String variables, each containing a filter rule in the variable value.
     * @param page Page object with text elements to apply the filter to.
     */
    public static void runTextFilter(VariableMap textFilterRules, Page page) {
        final TextFilter textFilter = new TextFilter(textFilterRules);
        final ContentObjectProcessor processor = new ContentObjectProcessor() {
            @Override
            public void doProcess(ContentObject contentObject) {
                if (contentObject != null) {
                    if (contentObject instanceof TextObject) {
                        TextObject textObj = (TextObject)contentObject;
                        String text = textObj.getText();
                        if (text != null)
                            textObj.setText(textFilter.filter(text));
                    }
                }
            }
        };

        //Callback for allowed text object types
        textFilter.setTextObjectTypeFilterCallback(new TextObjectTypeFilterCallback() {

            @Override
            public boolean textFilterEnabledForObjectType(String textObjectTypeFilter) {
                if (textObjectTypeFilter == null || textObjectTypeFilter.isEmpty())
                    return true;
                ContentObject currentObject = processor.getCurrentObject();
                if (currentObject == null)
                    return true;
                if (currentObject instanceof TextRegion) {
                    return textObjectTypeFilter.toLowerCase().contains("r"); //Region
                }
                if (currentObject instanceof TextLine) {
                    return textObjectTypeFilter.toLowerCase().contains("l"); //Text line
                }
                if (currentObject instanceof Word) {
                    return textObjectTypeFilter.toLowerCase().contains("w"); //Word
                }
                if (currentObject instanceof Glyph) {
                    return textObjectTypeFilter.toLowerCase().contains("g"); //Glyph
                }
                return false;
            }
        });

        //Run filter process
        try {
            processor.run(page);
        } catch(Exception e) {
            System.out.println("Error while applying text filter.");
            e.printStackTrace();
        }
    }

    /**
     * reads the specified page from source file
     * @param filePath path to source file
     * @param pageNo page to be read
     * @return PageXML page
     */
    public Page readPage(String filePath, int pageNo) {
        Page page = new Page();
        try {
            FileInputStream readStream =  new FileInputStream(filePath);
            page = PageXmlInputOutput.readPage(filePath);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }


        return page;
    }
    /**
     * Sets the GtsId that is to be added to the PAGE document.<br>
     * Note: The ID has to be conform to the XML ID convention (start with letter, ...).
     * @param pattern A specific ID or [start,end], where 'start' is the index of the first character
     * and 'end' the index of the last character within the given filename (index starts with 0).
     * @param filepath
     */
    public void setGtsId(String pattern, String filepath) {
        try {
            if (pattern.contains("[") && pattern.endsWith("]")) {
                int p = pattern.indexOf('[');
                String positionPattern =  pattern.substring(p+1, pattern.length()-1);
                String prefix = "";
                if (!pattern.startsWith("["))
                    prefix = pattern.substring(0,p);
                String[] positions = positionPattern.split(",");
                if (positions != null && positions.length == 2) {
                    int start = Integer.parseInt(positions[0]);
                    int end = Integer.parseInt(positions[1]);
                    String filename = extractFilename(filepath);
                    gtsidToSet = prefix+filename.substring(start, end + 1);
                }
            } else {
                gtsidToSet = pattern;
            }
        } catch (Exception e) {
            System.err.println("Could not extract GtsId from filename.");
            e.printStackTrace();
        }
    }

    /**
     * Sets the target PAGE XML schema version of the output file.
     * @param versionString LATEST, 2013-07-15 or 2010-03-19
     */
    public void setTargetSchema(String versionString) {
        if ("LATEST".equals(versionString))
            targetformat = PageXmlInputOutput.getLatestSchemaModel().getVersion();
        else
            targetformat = new XmlFormatVersion(versionString);
    }

    /**
     * Extracts the filename from a full file path.
     * @param filepath E.g. c:\temp\test.xml
     * @return The filename (e.g. test.xml).
     */
    private String extractFilename(String filepath) {
        if (!filepath.contains(File.separator))
            return filepath;
        return filepath.substring(filepath.lastIndexOf(File.separator)+1);
    }

    /**
     * Sets the filter rules that are to be applied to all text regions.
     * @param textFilterRules A collection of String variables, each containing a filter rule in the variable value.
     */
    public void setTextFilterRules(VariableMap textFilterRules) {
        this.textFilterRules = textFilterRules;
    }

    }

}