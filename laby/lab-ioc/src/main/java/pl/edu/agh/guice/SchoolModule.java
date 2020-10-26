package pl.edu.agh.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import pl.edu.agh.logger.Logger;
import pl.edu.agh.school.persistence.IPersistenceManager;
import pl.edu.agh.school.persistence.SerializablePersistenceManager;

public class SchoolModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IPersistenceManager.class).to(SerializablePersistenceManager.class);
        bind(String.class).annotatedWith(Names.named("TeachersFile")).toInstance("teachers-guice.dat");
        bind(String.class).annotatedWith(Names.named("ClassesFile")).toInstance("classes-guice.dat");
    }

    @Provides
    @Singleton
    public Logger provideLogger() {
        return Logger.getInstance();
    }

}
