package com.pingunaut.maven.plugin.messages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class FilePostProcessor {

    public void doPostProcessing(File file) {
        try {
            File tmpFile = new File(file + ".tmp");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            PrintWriter pw = new PrintWriter(tmpFile, "UTF-8");

            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("\\n")) {
                    line = line.replace("\\n", "&#x0d;&#x0a;");
                }
                pw.write(line + "\r\n");
            }

            br.close();
            pw.close();
            file.delete();
            tmpFile.renameTo(file);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
