package com.hemovia.api.support;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Apoio dos testes de migration da Seção 2.
Cria um schema só do teste no PostgreSQL real, aplica as migrations até a versão pedida
e oferece atalhos para inserir dados e para conferir o que o banco recusa.
Os dados reais nunca são tocados: tudo vive no schema do teste e some quando ele fecha.
*/
public final class BancoDeMigracao implements AutoCloseable {

    // Instante fixo usado nas linhas de apoio, para nenhum teste depender do relógio
    private static final String INSTANTE = "TIMESTAMPTZ '2026-08-11 12:00:00+00'";

    // Nome do schema isolado deste teste
    private final String schema = "migracao_" + UUID.randomUUID().toString().replace("-", "");

    // Dados de conexão com o PostgreSQL
    private final String url;
    private final String usuario;
    private final String senha;

    public BancoDeMigracao() throws SQLException {
        // Usamos as mesmas variáveis da aplicação, com o arquivo .env como reserva (é assim que o dev roda local)
        Properties arquivo = lerArquivoEnv();
        String host = configuracao("POSTGRES_HOST", arquivo, "localhost");
        String porta = configuracao("POSTGRES_PORT", arquivo, "5432");
        String banco = configuracao("POSTGRES_DB", arquivo, null);
        this.url = "jdbc:postgresql://" + host + ":" + porta + "/" + banco;
        this.usuario = configuracao("POSTGRES_USER", arquivo, null);
        this.senha = configuracao("POSTGRES_PASSWORD", arquivo, "");
        // Cria o schema vazio em que as migrations vão rodar
        try (Connection conexao = DriverManager.getConnection(url, usuario, senha);
             Statement comando = conexao.createStatement()) {
            comando.execute("CREATE SCHEMA " + schema);
        }
    }

    // Aplica as migrations de V1 até a versão pedida, por exemplo "8"
    public void migrarAte(String versao) {
        flyway(versao).migrate();
    }

    // Aplica todas as migrations que existem no código
    public void migrarTudo() {
        flyway(null).migrate();
    }

    // Devolve o número da última migration aplicada neste schema
    public String versaoAtual() {
        return flyway(null).info().current().getVersion().getVersion();
    }

    // Executa um comando de escrita; os parâmetros nulos viram NULL no banco
    public void executar(String sql, Object... parametros) throws SQLException {
        try (Connection conexao = conectar(); PreparedStatement comando = preparar(conexao, sql, parametros)) {
            comando.execute();
        }
    }

    // Devolve a primeira coluna da primeira linha, ou null se a consulta não achar nada
    public Object valor(String sql, Object... parametros) throws SQLException {
        try (Connection conexao = conectar();
             PreparedStatement comando = preparar(conexao, sql, parametros);
             ResultSet linhas = comando.executeQuery()) {
            return linhas.next() ? linhas.getObject(1) : null;
        }
    }

    // Atalho para consultas que devolvem um número, como count(*)
    public long contar(String sql, Object... parametros) throws SQLException {
        return ((Number) valor(sql, parametros)).longValue();
    }

    // Confere que o banco recusou a ação por causa da restrição com esse nome
    public void exigirRestricao(String restricao, ThrowingCallable acao) {
        exigirRejeicao("\"" + restricao + "\"", acao);
    }

    // Confere que o banco recusou a ação e que a mensagem do erro traz esse trecho
    public void exigirRejeicao(String trecho, ThrowingCallable acao) {
        assertThatThrownBy(acao).isInstanceOf(SQLException.class).hasMessageContaining(trecho);
    }

    // Confere que o índice existe neste schema
    public void exigirIndice(String indice) throws SQLException {
        long total = contar("SELECT count(*) FROM pg_indexes WHERE schemaname = current_schema() AND indexname = ?", indice);
        assertThat(total).as("índice " + indice).isEqualTo(1);
    }

    // Cria um hospital válido e devolve o id
    public UUID criarHospital() throws SQLException {
        UUID id = UUID.randomUUID();
        executar("INSERT INTO hospital (id, name, city, latitude, longitude, priority) "
                + "VALUES (?, 'Hospital de Teste', 'Recife', -8.05, -34.90, 3)", id);
        return id;
    }

    // Cria um hemocentro válido e devolve o id
    public UUID criarHemocentro() throws SQLException {
        UUID id = UUID.randomUUID();
        executar("INSERT INTO blood_centers (id, name, city, latitude, longitude) "
                + "VALUES (?, 'Hemocentro de Teste', 'Recife', -8.06, -34.88)", id);
        return id;
    }

    // Cria um veículo válido (código único) e devolve o id
    public UUID criarVeiculo() throws SQLException {
        UUID id = UUID.randomUUID();
        executar("INSERT INTO vehicles (id, code, capacity) VALUES (?, ?, 4)", id, "VEI-" + id);
        return id;
    }

    // Cria um usuário no formato da V2 (sem papel) e devolve o id
    public UUID criarUsuarioAntigo() throws SQLException {
        UUID id = UUID.randomUUID();
        executar("INSERT INTO users (id, name, email, cpf, password) VALUES (?, 'Pessoa Antiga', ?, ?, 'hash')",
                id, id + "@teste.com", id.toString().substring(0, 14));
        return id;
    }

    // Cria uma requisição de hospital no formato da V5 e devolve o id
    public UUID criarRequisicao(UUID hospital) throws SQLException {
        UUID id = UUID.randomUUID();
        executar("INSERT INTO blood_requests (id, hospital_id, blood_type, rh_factor, component, quantity, status, created_at, updated_at) "
                + "VALUES (?, ?, 'O', 'NEGATIVE', 'RED_BLOOD_CELLS', 2, 'PENDING', " + INSTANTE + ", " + INSTANTE + ")", id, hospital);
        return id;
    }

    // Monta a cadeia inteira que um transporte exige (hospital, hemocentro, requisição, alocação, rota e veículo)
    public UUID criarTransporte() throws SQLException {
        UUID hospital = criarHospital();
        UUID hemocentro = criarHemocentro();
        UUID requisicao = criarRequisicao(hospital);
        UUID alocacao = UUID.randomUUID();
        executar("INSERT INTO allocations (id, request_id, blood_center_id, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, 'ALLOCATED', " + INSTANTE + ", " + INSTANTE + ")", alocacao, requisicao, hemocentro);
        UUID rota = UUID.randomUUID();
        executar("INSERT INTO delivery_routes (id, origin_id, destination_id, distance_km, estimated_duration_seconds) "
                + "VALUES (?, ?, ?, 12.5, 1800)", rota, hemocentro, hospital);
        UUID transporte = UUID.randomUUID();
        executar("INSERT INTO transports (id, allocation_id, route_id, vehicle_id, status, started_at) "
                + "VALUES (?, ?, ?, ?, 'PLANNED', " + INSTANTE + ")", transporte, alocacao, rota, criarVeiculo());
        return transporte;
    }

    // Cria uma leitura de telemetria no formato da V6, sem posição, e devolve o id
    public UUID criarLeituraAntiga(UUID transporte) throws SQLException {
        UUID id = UUID.randomUUID();
        executar("INSERT INTO telemetry_readings (id, transport_id, sensor_id, measured_at, received_at, temperature_celsius) "
                + "VALUES (?, ?, 'SENSOR-1', " + INSTANTE + ", " + INSTANTE + ", 4.5)", id, transporte);
        return id;
    }

    @Override
    public void close() throws SQLException {
        // O nome do schema nasceu aqui dentro, nunca vem de fora, então apagar em cascata é seguro
        try (Connection conexao = DriverManager.getConnection(url, usuario, senha);
             Statement comando = conexao.createStatement()) {
            comando.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }

    // Abre uma conexão já apontada para o schema do teste
    private Connection conectar() throws SQLException {
        Connection conexao = DriverManager.getConnection(url, usuario, senha);
        conexao.setSchema(schema);
        return conexao;
    }

    // Prepara o comando e entrega cada parâmetro na ordem recebida
    private static PreparedStatement preparar(Connection conexao, String sql, Object[] parametros) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(sql);
        for (int i = 0; i < parametros.length; i++) {
            comando.setObject(i + 1, parametros[i]);
        }
        return comando;
    }

    // Configura o Flyway para rodar só no schema do teste, e opcionalmente parar numa versão
    private Flyway flyway(String alvo) {
        FluentConfiguration configuracao = Flyway.configure()
                .dataSource(url, usuario, senha)
                .schemas(schema)
                .defaultSchema(schema)
                .locations("classpath:db/migration")
                .cleanDisabled(true);
        if (alvo != null) {
            configuracao.target(alvo);
        }
        return configuracao.load();
    }

    // Lê o .env (se existir) da pasta atual ou da raiz do repositório
    private static Properties lerArquivoEnv() {
        Properties propriedades = new Properties();
        for (Path caminho : List.of(Path.of(".env"), Path.of("api/.env"))) {
            if (Files.isRegularFile(caminho)) {
                try (var leitor = Files.newBufferedReader(caminho)) {
                    propriedades.load(leitor);
                } catch (IOException erro) {
                    throw new UncheckedIOException(erro);
                }
                return propriedades;
            }
        }
        return propriedades;
    }

    // Valor da variável de ambiente; se faltar, o do .env; se faltar também, o padrão (ou erro, quando não há padrão)
    private static String configuracao(String nome, Properties arquivo, String padrao) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isBlank()) {
            valor = arquivo.getProperty(nome);
        }
        if (valor == null || valor.isBlank()) {
            if (padrao == null) {
                throw new IllegalStateException("Teste de migration precisa da configuração " + nome + " (variável de ambiente ou .env)");
            }
            return padrao;
        }
        return valor.trim();
    }
}
