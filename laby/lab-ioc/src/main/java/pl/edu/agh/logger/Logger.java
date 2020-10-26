package pl.edu.agh.logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.edu.agh.school.SchoolClass;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class Logger {
    protected static Logger logger;

    protected DateFormat dateFormat;

    protected Set<IMessageSerializer> registeredSerializers;

    @Inject
    public Logger() {
        init();
        this.registeredSerializers = new HashSet<IMessageSerializer>();
    }

    public Logger(Set<IMessageSerializer> registeredSerializers) {
        init();
        if (registeredSerializers == null) {
            throw new IllegalArgumentException("null argument");
        }
        this.registeredSerializers = registeredSerializers;
    }

    public static Logger getInstance() {
        if (logger == null)
            logger = new Logger();
        return logger;
    }

    public void registerSerializer(IMessageSerializer messageSerializer) {
        registeredSerializers.add(messageSerializer);
    }

    public void log(String message) {
        log(message, null);
//        saveLogsToFile(message + '\n');
    }

    public void log(String message, Throwable error) {
        for (IMessageSerializer messageSerializer : registeredSerializers) {
            String formattedMessage = dateFormat.format(new Date())
                    + ": " + message + (error != null ? error.toString() : "");
            messageSerializer.serializeMessage(formattedMessage);
        }
    }

    private void init() {
        dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    }

//    private void saveLogsToFile(String log) {
//        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("persistence.log"))) {
//
//            oos.writeObject(log);
//        } catch (FileNotFoundException e) {
//            throw new IllegalArgumentException(e);
//        } catch (IOException e) {
//            log("There was an error while saving logs", e);
//        }
//    }

}
