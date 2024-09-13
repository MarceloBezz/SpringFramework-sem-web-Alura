package br.com.alura.screenmatch.principal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

public class Principal {
    private Scanner scanner = new Scanner(System.in);

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=8a32db79";

    private ConsumoAPI consumoApi = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();

    public void exibeMenu() {
        System.out.println("Digite o nome da série para busca:");
        var nomeSerie = scanner.nextLine();

        // JSON com os dados da série
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        // Agrupando todas as temporadas de uma série
        List<DadosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        // Imprimindo os títulos de todos os episódios de cada temporada
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        // Agrupando os episódios em uma lista 
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        System.out.println("\nTop 10 episódios:");
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .peek(e -> System.out.println("Primeiro filtro(N/A): " + e))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .peek(e -> System.out.println("Ordenação: " + e))
                .limit(10)
                .peek(e -> System.out.println("Limite de 10: " + e))
                .map(e -> e.titulo().toUpperCase())
                .peek(e -> System.out.println("Maiúsculo: " + e))
                .forEach(System.out::println);

        //Agrupando os episódios em uma lista de classe Episódio
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d)))
                .collect(Collectors.toList());
        episodios.forEach(System.out::println);

        System.out.println("Qual episódio você deseja ver?");
        var trechoTitulo = scanner.nextLine();
        Optional<Episodio> episodioDesejado = episodios.stream()
                 .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                 .findFirst();
        if(episodioDesejado.isPresent()) {
            System.out.println("Episódio encontrado!");
            System.out.println("Temporada " + episodioDesejado.get().getTemporada() + ", episódio " + episodioDesejado.get().getNumeroEpisodio());
        } else {
            System.out.println("Episódio não encontrado");
        }

        System.out.println("A partir de que ano você deseja ver os episódios?");
        var ano = scanner.nextInt();
        scanner.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
        .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))  
        .forEach(e -> System.out.println(
            "Temporada: " + e.getTemporada() +
            "; Episódio: " + e.getTitulo() + 
            "; Data de lançamento: " + e.getDataLancamento().format(df)
        ));    
        
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                                                                .filter(e -> e.getAvaliacao() > 0.0)
                                                                .collect(Collectors.groupingBy(Episodio::getTemporada,
                                                                         Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
        .filter(e -> e.getAvaliacao() > 0.0)
        .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade : " + est.getCount());
    }
}
