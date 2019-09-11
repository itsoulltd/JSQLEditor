package com.it.soul.lab.connect.io;

import com.it.soul.lab.sql.SQLExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ScriptRunner {

    private final String CREATE_TABLE_PREFIX = "CREATE TABLE ";
    private final String CREATE_TABLE_IF_NOT_PREFIX = "CREATE TABLE IF NOT EXISTS ";
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    public String[] commands(InputStream in){
        String read = readFrom(in);
        if (read != null && !read.isEmpty()){
            String[] split = read.split(";");
            return split;
        }
        return new String[0];
    }

    public void execute(String[] cmds, Connection connection){
        List<String> comsn = Arrays.asList(cmds);
        try (SQLExecutor executor = new SQLExecutor(connection)){
            comsn.forEach(cmd -> {
                try {
                    if (executor.executeDDLQuery(cmd)) {
                        try {
                            int endIndex = cmd.indexOf("(");
                            if (cmd.startsWith(CREATE_TABLE_IF_NOT_PREFIX))
                                log.info("Created :: " + cmd.substring(CREATE_TABLE_IF_NOT_PREFIX.length(), endIndex));
                            else
                                log.info("Created :: " + cmd.substring(CREATE_TABLE_PREFIX.length(), endIndex));
                        } catch (Exception e) {}
                    }
                }catch (SQLException e){log.warning(e.getMessage());}
            });
        }catch (Exception e) {log.warning(e.getMessage());}
    }

    private String readFrom(InputStream in){
        StringBuffer buffer = new StringBuffer();
        try (BufferedReader reader=new BufferedReader(new InputStreamReader(in))){
            String line;
            while((line=reader.readLine()) != null){
                buffer.append(line);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return buffer.toString();
    }

    public InputStream createStream(File file){
        ClassLoader loader = getClass().getClassLoader();
        InputStream in = loader.getResourceAsStream(file.getPath());
        return in;
    }
}
