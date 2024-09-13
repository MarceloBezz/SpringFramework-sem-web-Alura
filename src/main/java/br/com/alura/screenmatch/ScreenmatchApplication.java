package br.com.alura.screenmatch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.com.alura.screenmatch.principal.Principal;


@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	//API key: 8a32db79
	@Override
	public void run(String... args) throws Exception {
		//Visualização dos resultados dos exercícios feitos em aula
		Principal principal = new Principal();
		principal.exibeMenu();

	}

}
