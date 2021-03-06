package nomad;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


@Configuration
@PropertySource(value="classpath:/application.properties", ignoreResourceNotFound=true)
public class JSONWriter {
    /**
     * Class to simplify writing out JSON files and safely handle Exceptions
     */

    @Autowired
    Environment env;

    // Get access to the console logger
    private static final Logger logger = Logger.getLogger(JSONWriter.class);

    // The directory for the fileWriter to write all files to
    private String directoryPath;

    public JSONWriter() {
        String path;
        try {
            path = env.getProperty("DataPath");
        } catch (NullPointerException e) {
            // TODO: The property get always fails for some reason, so this is a hack to get it to work for now
            path = "/home/tomek/Work/Nomad/QB_Interface/out/Data/";
        }
        this.directoryPath = path;
    }

    // main write: Convert a JSON object to a pretty-printed string and write it out to the file
    public void write(String filename, JSONObject obj) {

        // Combine the given file name with the directory
        String fullFilename = this.directoryPath + filename + ".json";

        // Perform the write, logging wither success of failure
        try (FileWriter file = new FileWriter(fullFilename)) {
            file.write(obj.toString(2));
            logger.debug("Successfully Copied JSON Object to File...");
        } catch (IOException e) {
            logger.warn("Object write FAILED!");
        }
    }

    // Convert the String to a JSONObject to ensure it will be pretty-printed by the main write
    public void write(String filename, String jsonString) {
        JSONObject obj = new JSONObject(jsonString);
        this.write(filename, obj);
    }

    // Convert the StringBuffer to a JSONObject to ensure it will be pretty-printed by the main write
    public void write(String filename, StringBuffer jsonBuffer) {
        String jsonString = new String(jsonBuffer);
        this.write(filename, jsonString);
    }

    // main datedWrite: Add the datetime to the filename before passing everything along for writing
    public void datedWrite(String filename, JSONObject json) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
        Date date = new Date();
        String datedFilename = filename + dateFormat.format(date);
        this.write(datedFilename, json);
    }

    // Convert the StringBuffer into a jsonObject before passing it on to the main datedWrite
    public void datedWrite(String filename, StringBuffer jsonBuffer) {
        JSONObject json = new JSONObject(jsonBuffer);
        this.datedWrite(filename, json);
    }
}
