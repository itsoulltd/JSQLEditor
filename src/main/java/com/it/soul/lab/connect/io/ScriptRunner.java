package com.it.soul.lab.connect.io;

import com.it.soul.lab.sql.SQLExecutor;
import com.it.soul.lab.sql.entity.Entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ScriptRunner {

    private final String USE_PREFIX = "USE ";
    private final String CREATE_TABLE_PREFIX = "CREATE TABLE ";
    private final String CREATE_TABLE_IF_NOT_PREFIX = "CREATE TABLE IF NOT EXISTS ";
    private final String DROP_TABLE_PREFIX = "DROP TABLE ";
    private final String SELECT_PREFIX= "SELECT";
    private final String INSERT_PREFIX= "INSERT";
    private final String INSERT_INTO_PREFIX= "INSERT INTO";
    private final String UPDATE_PREFIX= "UPDATE";
    private final String DELETE_PREFIX= "DELETE";
    private final String DELETE_FROM_PREFIX= "DELETE FROM";
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    private CmdExecutionTracker tracker = new CmdExecutionTracker();

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
                if (cmd.toLowerCase().startsWith(SELECT_PREFIX.toLowerCase())) return;
                try {
                    String tableName = parseTableName(cmd);
                    if (cmd.toLowerCase().startsWith(INSERT_PREFIX.toLowerCase())) {
                        if (executor.executeInsert(false, cmd) > 0){
                            tracker.incrementEffective(tableName);
                        }else {
                            tracker.incrementFailed(tableName);
                        }
                    } if (cmd.toLowerCase().startsWith(UPDATE_PREFIX.toLowerCase())) {
                        if (executor.executeUpdate(cmd) > 0){
                            tracker.incrementEffective(tableName);
                        }else {
                            tracker.incrementFailed(tableName);
                        }
                    } if (cmd.toLowerCase().startsWith(DELETE_PREFIX.toLowerCase())) {
                        if (executor.executeDelete(cmd) >= 0){
                            tracker.incrementEffective(tableName);
                        }else {
                            tracker.incrementFailed(tableName);
                        }
                    } else {
                        if (executor.executeDDLQuery(cmd)) {
                            tracker.incrementEffective(tableName);
                        }else {
                            tracker.incrementFailed(tableName);
                        }
                    }
                }catch (SQLException e){ log.warning(e.getMessage());}
            });
            printExecutionMessage(tracker.getEffectiveKeys());
        }catch (Exception e) {log.warning(e.getMessage());}
    }

    private void printExecutionMessage(List<String> keys) {
        keys.forEach(key -> log.info(key + ": " + "\n Row Effected: " + tracker.effective(key) + "\n Failed: " + tracker.failed(key)));
    }

    private void printExecutionMessage(String cmd, int rowEffected, int rowFailed) {
        try {
            if (cmd.startsWith(CREATE_TABLE_IF_NOT_PREFIX))
                log.info("Created :: " + cmd.substring(CREATE_TABLE_IF_NOT_PREFIX.length(), cmd.indexOf("(")) + "\n New Table Effected: " + rowEffected + "\n Failed: " + rowFailed);
            else if (cmd.startsWith(CREATE_TABLE_PREFIX))
                log.info("Created :: " + cmd.substring(CREATE_TABLE_PREFIX.length(), cmd.indexOf("(")) + "\n New Table Effected: " + rowEffected + "\n Failed: " + rowFailed);
            else if (cmd.startsWith(INSERT_PREFIX))
                log.info("Inserted :: " + cmd.substring(INSERT_PREFIX.length(), cmd.indexOf("(")) + "\n Row Effected: " + rowEffected + "\n Failed: " + rowFailed);
            else if (cmd.startsWith(UPDATE_PREFIX))
                log.info("Updated :: " + cmd.substring(UPDATE_PREFIX.length(), cmd.toLowerCase().indexOf("set")) + "\n Row Effected: " + rowEffected + "\n Failed: " + rowFailed);
            else if (cmd.startsWith(DELETE_PREFIX))
                log.info("Deleted :: " + cmd.substring(DELETE_PREFIX.length()) + "\n Row Effected: " + rowEffected + "\n Failed: " + rowFailed);
        } catch (Exception e) {}
    }

    private String parseTableName(String cmd) {
        try {
            if (cmd.toUpperCase().startsWith(CREATE_TABLE_IF_NOT_PREFIX))
                return cmd.substring(CREATE_TABLE_IF_NOT_PREFIX.length(), cmd.indexOf("(")).replace(" ", "");

            else if (cmd.toUpperCase().startsWith(CREATE_TABLE_PREFIX))
                return cmd.substring(CREATE_TABLE_PREFIX.length(), cmd.indexOf("(")).replace(" ", "");

            else if (cmd.toUpperCase().startsWith(INSERT_PREFIX))
                return cmd.substring(INSERT_INTO_PREFIX.length(), cmd.indexOf("(")).replace(" ", "");

            else if (cmd.toUpperCase().startsWith(UPDATE_PREFIX))
                return cmd.substring(UPDATE_PREFIX.length(), cmd.toLowerCase().indexOf("set")).replace(" ", "");

            else if (cmd.toUpperCase().startsWith(DELETE_PREFIX) && !cmd.toLowerCase().contains("where"))
                return cmd.substring(DELETE_FROM_PREFIX.length()).replace(" ", "");

            else if (cmd.toUpperCase().startsWith(DELETE_PREFIX) && cmd.toLowerCase().contains("where"))
                return cmd.substring(DELETE_FROM_PREFIX.length(), cmd.toLowerCase().indexOf("where")).replace(" ", "");

            else if (cmd.toUpperCase().startsWith(USE_PREFIX))
                return cmd.substring(USE_PREFIX.length()).replace(" ", "");

        } catch (Exception e) {}
        return "";
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

    private static class CmdExecutionTracker extends Entity {

        private Map<String, AtomicInteger> effectiveCollection = new HashMap<>();
        private Map<String, AtomicInteger> failedCollection = new HashMap<>();

        private AtomicInteger getEffectiveFor(String key){
            AtomicInteger eff = effectiveCollection.get(key);
            if (eff == null){
                eff = new AtomicInteger(0);
                effectiveCollection.put(key, eff);
            }
            return eff;
        }

        private AtomicInteger getFailedFor(String key){
            AtomicInteger failed = failedCollection.get(key);
            if (failed == null){
                failed = new AtomicInteger(0);
                failedCollection.put(key, failed);
            }
            return failed;
        }

        public int incrementEffective(String key){
            return getEffectiveFor(key).incrementAndGet();
        }

        public int effective(String key){return getEffectiveFor(key).get();}

        public int incrementFailed(String key){
            return getFailedFor(key).incrementAndGet();
        }

        public int failed(String key) {return getFailedFor(key).get();}

        public List<String> getEffectiveKeys(){
            return Arrays.asList(effectiveCollection.keySet().toArray(new String[0]));
        }

        public List<String> getFailedKeys(){
            return Arrays.asList(failedCollection.keySet().toArray(new String[0]));
        }
    }
}
