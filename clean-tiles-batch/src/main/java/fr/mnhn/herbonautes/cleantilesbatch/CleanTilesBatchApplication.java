package fr.mnhn.herbonautes.cleantilesbatch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableAsync
@SpringBootApplication
public class CleanTilesBatchApplication implements CommandLineRunner, AsyncConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(CleanTilesBatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		// /Users/jgs/Missions/Herbonautes-2019-05/tiles

		String tilesRootDir = "/Users/jgs/Missions/Herbonautes-2019-05/tiles";

		//Files.walk(Paths.get(tilesRootDir))
		//		.forEach(p -> {
		//			System.out.println(p);
		//		});

	}

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(2);
		executor.setThreadNamePrefix("MyExecutor-");
		executor.initialize();
		return executor;
	}

}
