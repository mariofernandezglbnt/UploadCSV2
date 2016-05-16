package com.globant.talent.utils;

/**
 * Created by drobak on 19/11/14.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class CSVDataSource {
    Iterator<String> ite;
    String[] heads;
    private boolean hasNext = true;
    private int lineNumber;
    private String currentLine = "";
    private int totalLines = 0;

    /*
     * New CSV Data Source Constructor
     * 
     * @param f file CSV file to read
     * 
     * @author: Alejandro de la Viña <a.delavina@globant.com>
     */
    /*
     * public CSVDataSource (File f) throws IOException { this.lines = FileUtils.readLines(f); ite =
     * this.lines.iterator();
     * 
     * heads = ite.next().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); }
     */
    public CSVDataSource(InputStream is) throws IOException {

        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = bis.readLine()) != null) {
            lines.add(line);
        }
        totalLines = lines.size();
        ite = lines.iterator();
        heads = ite.next().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        lineNumber = 1;

    }

    /*
     * Method that retrieves a Map with the next data structure.<p> For this implementation a map that will always bring
     * the same Keys and a set of Values per line
     * 
     * @author Alejandro de la Viña <a.delavina@globant.com>
     */
    public HashMap<String, String> getNextRecord() {
        int i;
        while (ite.hasNext()) {
            lineNumber++;
            currentLine = ite.next();
            if (StringUtils.isBlank(currentLine))
                continue;

            String[] line = currentLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            if (line.length != heads.length)
                throw new IllegalStateException("Invalid number of columns in CSV in line:" + lineNumber
                        + " - # of columns=" + line.length);
            HashMap<String, String> hm = new HashMap<String, String>();
            for (i = 0; i < line.length; i++)
                hm.put(heads[i], line[i].replaceAll("^\"|\"$", ""));

            if (!ite.hasNext())
                this.hasNext = false;
            return hm;
        }
        return null;

    }

    /*
     * Informs whether there are following records to be processed.
     * 
     * @author: Alejandro de la Viña <a.delavina@globant.com>
     */
    public boolean hasNext() {
        return this.hasNext;
    }

    public String getCurrentLine() {
        return currentLine;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public String[] getHeads() {
        return this.heads;
    }

}