package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private SerieRepository repositorio;

    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar Series buscadas
                    4 - Buscar Serie por titulo
                    5 - Buscar Serie por ator
                    6 - Top 5 series
                    7 - Buscar series por categoria
                    8 - Busca por total de temporadas
                    9 - Buscar episodio por trecho do titulo
                    10 - Top 5 Episodios
                    11 - Buscar episodio por data

                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriePorCategoria();
                    break;
                case 8:
                    buscarSeriePorTemporada();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    buscarTop5Episodios();
                    break;
                case 11:
                    buscarEpisodioPorData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarEpisodioPorData() {
        buscarSeriePorTitulo();
        Serie serie = serieBuscada.get();
        System.out.println("Digite a data que deseja buscar o episodio: ");
        var anoLancamento = leitura.nextInt();
        leitura.nextLine();
        List<Episodio> episodiosAno = repositorio.episodioPorSerieEAno(serie , anoLancamento);
        episodiosAno.forEach(System.out::println);

    }

    private void buscarTop5Episodios() {
        buscarSeriePorTitulo();
        Serie serie = serieBuscada.get();
        List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
        topEpisodios.forEach(e ->
                System.out.printf("Nome da serie: %s Temporada: %s - Episodio: %s - Titulo do episodio %s - avaliacao do episodio: %s\n",
                        e.getSerie().getTitulo() , e.getTemporada() , e.getNumeroEpisodio() , e.getTitulo() , e.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Digite o trecho do episodio: ");
        var trecho = leitura.nextLine();
        List<Episodio> serieEncontrada = repositorio.episodiosPorTrecho(trecho);
        serieEncontrada.forEach(e ->
                System.out.printf("Nome da serie: %s Temporada: %s - Episodio: %s - %s\n",
                        e.getSerie().getTitulo() , e.getTemporada() , e.getNumeroEpisodio() , e.getTitulo()));
    }

    private void buscarSeriePorTemporada() {
        System.out.println("Digite o numero total de temporadas: ");
        var numeroTemporadas = leitura.nextInt();
        System.out.println("Digite a avaliacao: ");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesEncontradas = repositorio.seriesPorTemporadaEAvaliacao(numeroTemporadas, avaliacao);
        seriesEncontradas.forEach(System.out::println);
    }

    private void buscarSeriePorCategoria() {
        System.out.println("Digite a categoria: ");
        var genero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(genero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Series da categoria: "+genero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Digite o nome da serie que deseja buscar: ");
        var nomeSerie = leitura.nextLine();

//        Optional<Serie> serie = series.stream()
//                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
//                .findFirst();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()){

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisódios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Serie nao encontrada");
        }

    }

    private void listarSeriesBuscadas() {
        // List<Serie> series = new ArrayList<>();
        // series = dadosSeries.stream()
        //         .map(d -> new Serie(d))
        //         .collect(Collectors.toList());

        series = repositorio.findAll();

        series.stream()
        .sorted(Comparator.comparing(Serie::getGenero))
        .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Digite o nome da serie que deseja buscar: ");
        var nomeSerie = leitura.nextLine();
        serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()){
            System.out.println("Dados serie buscada: " + serieBuscada.get());
        } else {
            System.out.println("Serie não encontrada");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Digite o nome do ator: ");
        var nomeAtor = leitura.nextLine();
        System.out.println("qual avaliacao: ");
        var avaliacao = leitura.nextDouble();
        List<Serie> serieEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        serieEncontradas.forEach(s ->
                System.out.println("Series que o ator " + nomeAtor +" trabalho: "));
        serieEncontradas.forEach(s -> System.out.println("Nome da serie: "+ s.getTitulo() + " avaliacao: "+ s.getAvaliacao()));
    }

    private void buscarTop5Series() {
        List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s -> System.out.println("Nome da serie: "+ s.getTitulo() + " avaliacao: "+ s.getAvaliacao()));
    }
}