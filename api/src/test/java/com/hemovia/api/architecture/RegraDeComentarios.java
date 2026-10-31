package com.hemovia.api.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/*
Confere a regra 0.1 do guia: nada de Javadoc e nenhuma linha de comentário começando com asterisco.
Fica em src/test porque só os testes precisam dela.
*/
final class RegraDeComentarios {

    // Javadoc é uma barra e dois asteriscos no começo de um comentário, nunca dentro de texto como "/api/**"
    private static final Pattern JAVADOC = Pattern.compile("(^|\\s)/\\*\\*");

    // Linha iniciada por asterisco é o estilo proibido; a linha que só fecha o bloco ("*" e "/") é permitida
    private static final Pattern LINHA_COM_ASTERISCO = Pattern.compile("^\\s*\\*(?!/)");

    // Classe só de métodos estáticos: ninguém precisa instanciar
    private RegraDeComentarios() { }

    // Devolve uma descrição por violação do texto; lista vazia significa arquivo limpo
    static List<String> violacoes(String conteudo) {
        // Vamos juntando o que for achado para devolver tudo de uma vez
        List<String> achados = new ArrayList<>();
        // O -1 mantém as linhas vazias do fim, e assim o número da linha bate com o do editor
        String[] linhas = conteudo.split("\\R", -1);
        // Percorremos linha a linha para apontar o lugar exato do problema
        for (int i = 0; i < linhas.length; i++) {
            // Javadoc em qualquer ponto da linha é violação
            if (JAVADOC.matcher(linhas[i]).find()) achados.add("linha " + (i + 1) + ": Javadoc proibido");
            // Linha de bloco começando com asterisco também é violação
            if (LINHA_COM_ASTERISCO.matcher(linhas[i]).find()) achados.add("linha " + (i + 1) + ": linha iniciada por asterisco");
        }
        return achados;
    }

    // Varre todos os .java abaixo da pasta e devolve "arquivo - problema", pulando os legados informados
    static List<String> violacoesDoProjeto(Path pastaSrc, Set<String> legados) {
        // O Files.walk abre um fluxo de arquivos que precisa ser fechado
        try (Stream<Path> arquivos = Files.walk(pastaSrc)) {
            return arquivos
                    .filter(arquivo -> arquivo.toString().endsWith(".java"))
                    .filter(arquivo -> !legados.contains(caminhoRelativo(pastaSrc, arquivo)))
                    .flatMap(arquivo -> violacoesDoArquivo(pastaSrc, arquivo).stream())
                    .toList();
        } catch (IOException erro) {
            // Falha de leitura vira erro de teste, nunca um silêncio
            throw new UncheckedIOException(erro);
        }
    }

    // Lê um arquivo e devolve as violações dele já com o nome do arquivo na frente
    static List<String> violacoesDoArquivo(Path pastaSrc, Path arquivo) {
        try {
            return violacoes(Files.readString(arquivo)).stream()
                    .map(problema -> caminhoRelativo(pastaSrc, arquivo) + " - " + problema)
                    .toList();
        } catch (IOException erro) {
            // Mesma regra: erro de leitura aparece, não some
            throw new UncheckedIOException(erro);
        }
    }

    // Caminho relativo com barra normal, para a lista de legados valer igual no Windows e no Linux
    static String caminhoRelativo(Path pastaSrc, Path arquivo) {
        return pastaSrc.relativize(arquivo).toString().replace('\\', '/');
    }
}
