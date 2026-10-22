# Desenho da revisão

A feature produz um diagnóstico do repositório. Três levantamentos independentes alimentam a consolidação final.

## Método

1. Fixar o commit de referência com git rev-parse HEAD e registrar git status --short; preservar alterações locais e registrar as relevantes.
2. Inventariar todo o projeto, incluindo arquivos ocultos versionados. Excluir caches, saídas de build e ferramentas de terceiros, explicitando exclusões.
3. Comparar objetivos documentados com implementação observada; não presumir requisitos externos nem impor refatorações antes do diagnóstico.
4. Usar evidências estáticas e verificações locais, registrando versões das ferramentas, comandos e ambiente necessário. Não manipular dados reais nem serviços de produção.
5. Consolidar achados e validar estrutura, referências e rastreabilidade dos relatórios. Testes de artefatos não comprovam correção funcional Java/C.

## Formato dos achados

Identificador; título; área; tipo (defeito confirmado, risco ou dúvida); severidade (crítica, alta, média ou baixa); arquivo e linha; comportamento esperado e sua fonte; comportamento observado; reprodução ou justificativa estática; impacto; recomendação; forma de validar a correção. Omitir valores de credenciais.

Crítica: comprometimento grave ou perda de dados demonstrável. Alta: bloqueio de fluxo central ou integridade. Média: impacto concreto em comportamento secundário ou manutenção. Baixa: documentação ou melhoria localizada. Prioridade considera impacto, confiança e dependências; ausência de evidência não é automaticamente defeito.

## Entregas

- relatorios/alinhamento.md: inventário, matriz de funcionalidades, evidências e estado por objetivo.
- relatorios/api-dados-seguranca.md: áreas inspecionadas e achados da API.
- relatorios/qualidade-operacao.md: AED, testes, infraestrutura, comandos e limitações.
- relatorio-final.md: conclusão fundamentada, pontos positivos e plano de ação.
- tests/revisao-projeto/*.test.mjs: provas dos critérios sobre os artefatos usando o runner Node configurado, sem simular aprovação de testes Maven/C.

As três primeiras tarefas podem rodar em paralelo. A consolidação deve aguardar todas; caminhos compartilhados em tasks.md preservam essa ordem no motor. O plano gerado será uma proposta sujeita à escolha de paralelismo, modelo e esforço.

## Limitações conhecidas do mecanismo

onpspec.config.json aponta para src/** e testes Node. A constituição contém também uma regra recomendada voltada a JavaScript. A revisão deve explicitar a cobertura ausente de Java/C e propor adequação rastreável; nunca apresentar aprovação documental como garantia sobre todo o produto.
