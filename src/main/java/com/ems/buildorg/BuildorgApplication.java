package com.ems.buildorg;

import com.ems.buildorg.modal.DatabaseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@SpringBootApplication
public class BuildorgApplication {
	public static void main(String[] args) {
		SpringApplication.run(BuildorgApplication.class, args);
	}
}
