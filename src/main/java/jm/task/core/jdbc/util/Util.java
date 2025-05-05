package jm.task.core.jdbc.util;

import jm.task.core.jdbc.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {
    private static final Logger logger = Logger.getLogger(Util.class.getName());

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/alex?useSSL=false&serverTimezone=UTC";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "MYUsnVoElTMqv!";

    private static volatile SessionFactory sessionFactory;

    private Util() {
        throw new UnsupportedOperationException("Это служебный класс - создание экземпляров запрещено");
    }

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
            logger.info("Успешное подключение к базе данных");
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            logger.log(Level.SEVERE, "Ошибка подключения", e);
            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            try {
                Configuration configuration = new Configuration();

                Properties settings = new Properties();
                settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
                settings.put(Environment.URL, JDBC_URL);
                settings.put(Environment.USER, JDBC_USERNAME);
                settings.put(Environment.PASS, JDBC_PASSWORD);
                settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
                settings.put(Environment.SHOW_SQL, "true");
                settings.put(Environment.HBM2DDL_AUTO, "none");
                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

                configuration.setProperties(settings);
                configuration.addAnnotatedClass(User.class);

                sessionFactory = configuration.buildSessionFactory(
                        new StandardServiceRegistryBuilder()
                                .applySettings(configuration.getProperties())
                                .build()
                );

                logger.info("Фабрика сессий Hibernate успешно создана");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Ошибка инициализации Hibernate", e);
                throw new RuntimeException("Ошибка инициализации Hibernate", e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("Фабрика сессий Hibernate успешно закрыта");
        }
    }
}