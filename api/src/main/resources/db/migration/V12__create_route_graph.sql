/* V12: malha viária (nós e arestas) sobre a qual o Dijkstra calcula o caminho mais curto. */

/* Cria os nós do grafo: um hemocentro, um hospital ou um cruzamento intermediário. */
CREATE TABLE route_nodes (
    /* Identificador do nó. */
    id UUID PRIMARY KEY,
    /* Nome para exibir no mapa. */
    name VARCHAR(255) NOT NULL,
    /* Posição do nó. */
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    /* Preenchido quando o nó é um hemocentro; cada hemocentro tem no máximo um nó. */
    blood_center_id UUID UNIQUE REFERENCES blood_centers(id),
    /* Preenchido quando o nó é um hospital; cada hospital tem no máximo um nó. */
    hospital_id UUID UNIQUE REFERENCES hospital(id),
    /* A latitude vai de -90 a 90. */
    CONSTRAINT ck_route_nodes_latitude CHECK (latitude BETWEEN -90 AND 90),
    /* A longitude vai de -180 a 180. */
    CONSTRAINT ck_route_nodes_longitude CHECK (longitude BETWEEN -180 AND 180),
    /* Um nó não pode ser hemocentro e hospital ao mesmo tempo; os dois vazios significam cruzamento. */
    CONSTRAINT ck_route_nodes_single_owner CHECK (blood_center_id IS NULL OR hospital_id IS NULL)
);

/* Cria as arestas do grafo: os trechos de via entre dois nós. */
CREATE TABLE route_edges (
    /* Identificador da aresta. */
    id UUID PRIMARY KEY,
    /* Nó em que o trecho começa. */
    from_node_id UUID NOT NULL REFERENCES route_nodes(id),
    /* Nó em que o trecho termina. */
    to_node_id UUID NOT NULL REFERENCES route_nodes(id),
    /* Tamanho do trecho em quilômetros. */
    distance_km NUMERIC(12,3) NOT NULL,
    /* Tempo para percorrer o trecho, em segundos. */
    travel_seconds BIGINT NOT NULL,
    /* Se verdadeiro, o trecho vale também no sentido contrário. */
    bidirectional BOOLEAN NOT NULL DEFAULT TRUE,
    /* Uma aresta que sai e volta ao mesmo nó não leva a lugar nenhum. */
    CONSTRAINT ck_route_edges_no_loop CHECK (from_node_id <> to_node_id),
    /* Trecho com distância zero ou negativa quebraria o cálculo do caminho mais curto. */
    CONSTRAINT ck_route_edges_distance CHECK (distance_km > 0),
    /* Tempo zero ou negativo também. */
    CONSTRAINT ck_route_edges_travel CHECK (travel_seconds > 0),
    /* Entre dois nós, no mesmo sentido, existe uma única aresta. */
    CONSTRAINT uk_route_edges_pair UNIQUE (from_node_id, to_node_id)
);

/* Acelera a busca das arestas que saem de um nó, que é o passo repetido do Dijkstra. */
CREATE INDEX ix_route_edges_from ON route_edges(from_node_id);

/* Acelera a busca das arestas que chegam a um nó, usada nos trechos de mão dupla. */
CREATE INDEX ix_route_edges_to ON route_edges(to_node_id);
