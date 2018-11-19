package cn.uyun;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * Created by 吴晗 on 2018/8/21.
 */
@SpringBootApplication
@EnableFeignClients
public class StartupApplication {
	public static void main(String[] args) {
		SpringApplication.run(StartupApplication.class);
	}
}
